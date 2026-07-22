package com.rrhh.saas.activo.servicio;

import com.rrhh.saas.activo.dominio.Activo;
import com.rrhh.saas.activo.dominio.AsignacionActivo;
import com.rrhh.saas.activo.dominio.EstadoActivo;
import com.rrhh.saas.activo.dto.RespuestaActivo;
import com.rrhh.saas.activo.dto.RespuestaAsignacionActivo;
import com.rrhh.saas.activo.dto.SolicitudAsignarActivo;
import com.rrhh.saas.activo.dto.SolicitudCrearActivo;
import com.rrhh.saas.activo.dto.SolicitudDevolverActivo;
import com.rrhh.saas.activo.repositorio.RepositorioActivo;
import com.rrhh.saas.activo.repositorio.RepositorioAsignacionActivo;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.offboarding.servicio.ServicioOffboarding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioActivo {

    private final RepositorioActivo repositorioActivo;
    private final RepositorioAsignacionActivo repositorioAsignacion;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioOffboarding servicioOffboarding;

    public ServicioActivo(RepositorioActivo repositorioActivo,
                          RepositorioAsignacionActivo repositorioAsignacion,
                          ServicioEmpleado servicioEmpleado,
                          ServicioEmpresa servicioEmpresa,
                          ServicioAuditoria servicioAuditoria,
                          ServicioOffboarding servicioOffboarding) {
        this.repositorioActivo = repositorioActivo;
        this.repositorioAsignacion = repositorioAsignacion;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioOffboarding = servicioOffboarding;
    }

    @Transactional(readOnly = true)
    public List<RespuestaActivo> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioActivo.findByEmpresaIdAndDeletedFalseOrderByNombreAsc(empresaId).stream()
                .map(this::mapearActivo)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaActivo obtenerPorId(Long id) {
        Activo activo = obtenerEntidad(id);
        return mapearActivo(activo);
    }

    @Transactional
    public RespuestaActivo crear(SolicitudCrearActivo solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Activo activo = new Activo();
        activo.setEmpresa(empresa);
        activo.setNombre(solicitud.nombre());
        activo.setTipo(solicitud.tipo());
        activo.setNumeroSerie(solicitud.numeroSerie());
        activo.setEstado(EstadoActivo.DISPONIBLE);
        Activo guardado = repositorioActivo.save(activo);
        servicioAuditoria.registrar("CREAR", "Activo", guardado.getId(), null, guardado.getNombre());
        return mapearActivo(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Activo activo = obtenerEntidad(id);
        if (activo.getEstado() == EstadoActivo.ASIGNADO) {
            throw new ExcepcionNegocio("No se puede eliminar un activo asignado", 400);
        }
        activo.setDeleted(true);
        activo.setDeletedAt(java.time.LocalDateTime.now());
        repositorioActivo.save(activo);
        servicioAuditoria.registrar("ELIMINAR", "Activo", activo.getId(), null, activo.getNombre());
    }

    @Transactional
    public RespuestaAsignacionActivo asignar(Long activoId, SolicitudAsignarActivo solicitud) {
        Activo activo = obtenerEntidad(activoId);
        if (activo.getEstado() != EstadoActivo.DISPONIBLE) {
            throw new ExcepcionNegocio("El activo no está disponible para asignación", 400);
        }

        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(solicitud.empleadoId(), empresa.getId());

        AsignacionActivo asignacion = new AsignacionActivo();
        asignacion.setActivo(activo);
        asignacion.setEmpleado(empleado);
        asignacion.setFechaEntrega(solicitud.fechaEntrega());
        asignacion.setNotas(solicitud.notas());
        AsignacionActivo guardada = repositorioAsignacion.save(asignacion);

        activo.setEstado(EstadoActivo.ASIGNADO);
        repositorioActivo.save(activo);

        servicioAuditoria.registrar("ASIGNAR", "AsignacionActivo", guardada.getId(), null, activo.getNombre());
        return mapearAsignacion(guardada);
    }

    @Transactional
    public RespuestaAsignacionActivo devolver(Long activoId, SolicitudDevolverActivo solicitud) {
        Activo activo = obtenerEntidad(activoId);
        AsignacionActivo asignacion = repositorioAsignacion
                .findByActivoIdAndFechaDevolucionIsNullAndDeletedFalse(activoId)
                .orElseThrow(() -> new ExcepcionNegocio("No hay asignación activa para este activo", 404));

        LocalDate fechaDevolucion = solicitud.fechaDevolucion() != null
                ? solicitud.fechaDevolucion()
                : LocalDate.now();
        asignacion.setFechaDevolucion(fechaDevolucion);
        asignacion.setNotas(solicitud.notas());
        AsignacionActivo guardada = repositorioAsignacion.save(asignacion);

        activo.setEstado(EstadoActivo.DISPONIBLE);
        repositorioActivo.save(activo);

        servicioAuditoria.registrar("DEVOLVER", "AsignacionActivo", guardada.getId(), null, activo.getNombre());
        servicioOffboarding.completarTareaDevolverActivo(guardada.getEmpleado().getId());

        return mapearAsignacion(guardada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionActivo> listarAsignacionesActivo(Long activoId) {
        obtenerEntidad(activoId);
        return repositorioAsignacion.findByActivoIdAndDeletedFalseOrderByFechaEntregaDesc(activoId).stream()
                .map(this::mapearAsignacion)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionActivo> listarAsignacionesEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorioAsignacion.findByEmpleadoIdAndDeletedFalseOrderByFechaEntregaDesc(empleadoId).stream()
                .map(this::mapearAsignacion)
                .toList();
    }

    private Activo obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioActivo.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Activo no encontrado", 404));
    }

    private Empresa obtenerEmpresaActual() {
        return servicioEmpresa.obtenerEntidad(ContextoTenant.obtenerEmpresaId());
    }

    private RespuestaActivo mapearActivo(Activo activo) {
        return new RespuestaActivo(
                activo.getId(),
                activo.getNombre(),
                activo.getTipo(),
                activo.getNumeroSerie(),
                activo.getEstado()
        );
    }

    private RespuestaAsignacionActivo mapearAsignacion(AsignacionActivo asignacion) {
        Empleado empleado = asignacion.getEmpleado();
        Activo activo = asignacion.getActivo();
        return new RespuestaAsignacionActivo(
                asignacion.getId(),
                activo.getId(),
                activo.getNombre(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                asignacion.getFechaEntrega(),
                asignacion.getFechaDevolucion(),
                asignacion.getNotas()
        );
    }
}
