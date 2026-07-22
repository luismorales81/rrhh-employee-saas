package com.rrhh.saas.novedad.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.novedad.dominio.EstadoNovedadLiquidacion;
import com.rrhh.saas.novedad.dominio.NovedadLiquidacion;
import com.rrhh.saas.novedad.dto.RespuestaNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudActualizarNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudCrearNovedadLiquidacion;
import com.rrhh.saas.novedad.repositorio.RepositorioNovedadLiquidacion;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ServicioNovedadLiquidacion {

    private final RepositorioNovedadLiquidacion repositorio;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioNovedadLiquidacion(RepositorioNovedadLiquidacion repositorio,
                                      ServicioEmpleado servicioEmpleado,
                                      ServicioEmpresa servicioEmpresa,
                                      ServicioAuditoria servicioAuditoria) {
        this.repositorio = repositorio;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional(readOnly = true)
    public List<RespuestaNovedadLiquidacion> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalseOrderByFechaDesc(empresaId).stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaNovedadLiquidacion obtenerPorId(Long id) {
        return mapear(obtenerEntidad(id));
    }

    @Transactional
    public RespuestaNovedadLiquidacion crear(SolicitudCrearNovedadLiquidacion solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(solicitud.empleadoId(), empresa.getId());

        NovedadLiquidacion novedad = new NovedadLiquidacion();
        novedad.setEmpresa(empresa);
        novedad.setEmpleado(empleado);
        novedad.setPeriodo(solicitud.periodo());
        novedad.setTipo(solicitud.tipo());
        novedad.setDescripcion(solicitud.descripcion());
        novedad.setCantidad(solicitud.cantidad());
        novedad.setMonto(solicitud.monto());
        novedad.setFecha(solicitud.fecha());
        novedad.setEstado(EstadoNovedadLiquidacion.BORRADOR);

        NovedadLiquidacion guardada = repositorio.save(novedad);
        servicioAuditoria.registrar("CREAR", "NovedadLiquidacion", guardada.getId(), null, guardada.getTipo().name());
        return mapear(guardada);
    }

    @Transactional
    public RespuestaNovedadLiquidacion actualizar(Long id, SolicitudActualizarNovedadLiquidacion solicitud) {
        NovedadLiquidacion novedad = obtenerEntidad(id);
        validarEditable(novedad);
        if (solicitud.tipo() != null) {
            novedad.setTipo(solicitud.tipo());
        }
        if (solicitud.descripcion() != null) {
            novedad.setDescripcion(solicitud.descripcion());
        }
        if (solicitud.cantidad() != null) {
            novedad.setCantidad(solicitud.cantidad());
        }
        if (solicitud.monto() != null) {
            novedad.setMonto(solicitud.monto());
        }
        if (solicitud.fecha() != null) {
            novedad.setFecha(solicitud.fecha());
        }
        NovedadLiquidacion guardada = repositorio.save(novedad);
        servicioAuditoria.registrar("ACTUALIZAR", "NovedadLiquidacion", guardada.getId(), null, null);
        return mapear(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        NovedadLiquidacion novedad = obtenerEntidad(id);
        validarEditable(novedad);
        novedad.setDeleted(true);
        novedad.setDeletedAt(java.time.LocalDateTime.now());
        repositorio.save(novedad);
        servicioAuditoria.registrar("ELIMINAR", "NovedadLiquidacion", novedad.getId(), null, null);
    }

    @Transactional
    public int confirmarLote(String periodo) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        List<NovedadLiquidacion> borradores = repositorio
                .findByEmpresaIdAndPeriodoAndEstadoAndDeletedFalseOrderByFechaDesc(
                        empresaId, periodo, EstadoNovedadLiquidacion.BORRADOR);
        if (borradores.isEmpty()) {
            throw new ExcepcionNegocio("No hay novedades en borrador para el período " + periodo);
        }
        for (NovedadLiquidacion novedad : borradores) {
            novedad.setEstado(EstadoNovedadLiquidacion.CONFIRMADA);
            repositorio.save(novedad);
            servicioAuditoria.registrar("CONFIRMAR", "NovedadLiquidacion", novedad.getId(), null, periodo);
        }
        return borradores.size();
    }

    @Transactional
    public byte[] exportarCsv(String periodo) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        List<NovedadLiquidacion> novedades = repositorio
                .findByEmpresaIdAndPeriodoAndEstadoAndDeletedFalseOrderByFechaDesc(
                        empresaId, periodo, EstadoNovedadLiquidacion.CONFIRMADA);

        if (novedades.isEmpty()) {
            throw new ExcepcionNegocio("No hay novedades confirmadas para exportar en el período " + periodo);
        }

        StringBuilder csv = new StringBuilder();
        csv.append("empleado_id;numero_empleado;nombre;periodo;tipo;descripcion;cantidad;monto;fecha;estado\n");
        for (NovedadLiquidacion n : novedades) {
            Empleado e = n.getEmpleado();
            csv.append(e.getId()).append(';');
            csv.append(escaparCsv(e.getNumeroEmpleado())).append(';');
            csv.append(escaparCsv(e.getNombre() + " " + e.getApellido())).append(';');
            csv.append(n.getPeriodo()).append(';');
            csv.append(n.getTipo().name()).append(';');
            csv.append(escaparCsv(n.getDescripcion())).append(';');
            csv.append(n.getCantidad() != null ? n.getCantidad() : "").append(';');
            csv.append(n.getMonto() != null ? n.getMonto() : "").append(';');
            csv.append(n.getFecha()).append(';');
            csv.append(n.getEstado().name()).append('\n');
            n.setEstado(EstadoNovedadLiquidacion.EXPORTADA);
            repositorio.save(n);
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    private void validarEditable(NovedadLiquidacion novedad) {
        if (novedad.getEstado() != EstadoNovedadLiquidacion.BORRADOR) {
            throw new ExcepcionNegocio(
                    "Solo se pueden modificar o eliminar novedades en estado BORRADOR", 400);
        }
    }

    private NovedadLiquidacion obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Novedad no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        return servicioEmpresa.obtenerEntidad(ContextoTenant.obtenerEmpresaId());
    }

    private RespuestaNovedadLiquidacion mapear(NovedadLiquidacion novedad) {
        Empleado empleado = novedad.getEmpleado();
        return new RespuestaNovedadLiquidacion(
                novedad.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                novedad.getPeriodo(),
                novedad.getTipo(),
                novedad.getDescripcion(),
                novedad.getCantidad(),
                novedad.getMonto(),
                novedad.getFecha(),
                novedad.getEstado()
        );
    }
}
