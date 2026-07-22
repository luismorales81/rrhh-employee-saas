package com.rrhh.saas.ausentismo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.ausentismo.dominio.Ausencia;
import com.rrhh.saas.ausentismo.dominio.TipoAusencia;
import com.rrhh.saas.ausentismo.dto.RespuestaAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudActualizarAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudRegistrarMisAusencias;
import com.rrhh.saas.ausentismo.dto.SolicitudRegistrarAusencia;
import com.rrhh.saas.ausentismo.repositorio.RepositorioAusencia;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioAusencia {

    private final RepositorioAusencia repositorio;
    private final ServicioTipoAusencia servicioTipoAusencia;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioAusencia(RepositorioAusencia repositorio,
                            ServicioTipoAusencia servicioTipoAusencia,
                            ServicioEmpleado servicioEmpleado,
                            ServicioEmpresa servicioEmpresa,
                            ServicioAuditoria servicioAuditoria,
                            ApplicationEventPublisher publicadorEventos) {
        this.repositorio = repositorio;
        this.servicioTipoAusencia = servicioTipoAusencia;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public RespuestaAusencia registrarPropia(SolicitudRegistrarMisAusencias solicitud) {
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        return registrar(new SolicitudRegistrarAusencia(
                empleado.getId(),
                solicitud.tipoAusenciaId(),
                solicitud.fecha(),
                solicitud.motivo(),
                solicitud.justificada(),
                solicitud.urlCertificado()
        ));
    }

    @Transactional
    public RespuestaAusencia justificarPropia(Long id, String urlCertificado) {
        Empresa empresa = obtenerEmpresaActual();
        Ausencia ausencia = obtenerEntidad(id, empresa.getId());
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        if (!ausencia.getEmpleado().getId().equals(empleado.getId())) {
            throw new ExcepcionNegocio("Solo puede justificar sus propias ausencias", 403);
        }
        ausencia.setJustificada(true);
        ausencia.setUrlCertificado(urlCertificado);
        validarCertificado(ausencia.getTipoAusencia(), true, urlCertificado);
        return mapear(repositorio.save(ausencia));
    }

    @Transactional(readOnly = true)
    public List<RespuestaAusencia> listarPropias() {
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        return listarPorEmpleado(empleado.getId());
    }

    @Transactional
    public RespuestaAusencia registrar(SolicitudRegistrarAusencia solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(solicitud.empleadoId(), empresa.getId());
        TipoAusencia tipo = servicioTipoAusencia.obtenerEntidad(solicitud.tipoAusenciaId(), empresa.getId());

        validarCertificado(tipo, solicitud.justificada(), solicitud.urlCertificado());

        Ausencia ausencia = new Ausencia();
        ausencia.setEmpresa(empresa);
        ausencia.setEmpleado(empleado);
        ausencia.setTipoAusencia(tipo);
        ausencia.setFecha(solicitud.fecha());
        ausencia.setMotivo(solicitud.motivo());
        ausencia.setJustificada(solicitud.justificada());
        ausencia.setUrlCertificado(solicitud.urlCertificado());

        Ausencia guardada = repositorio.save(ausencia);
        servicioAuditoria.registrar("REGISTRAR", "Ausencia", guardada.getId(), null,
                tipo.getNombre() + " - " + solicitud.fecha());
        publicadorEventos.publishEvent(new EventoAusenciaRegistrada(
                guardada.getId(), empleado.getId(), empresa.getId()));
        return mapear(guardada);
    }

    @Transactional
    public RespuestaAusencia actualizar(Long id, SolicitudActualizarAusencia solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Ausencia ausencia = obtenerEntidad(id, empresa.getId());

        if (solicitud.motivo() != null) {
            ausencia.setMotivo(solicitud.motivo());
        }
        if (solicitud.justificada() != null) {
            ausencia.setJustificada(solicitud.justificada());
        }
        if (solicitud.urlCertificado() != null) {
            ausencia.setUrlCertificado(solicitud.urlCertificado());
        }

        validarCertificado(ausencia.getTipoAusencia(), ausencia.isJustificada(), ausencia.getUrlCertificado());

        Ausencia actualizada = repositorio.save(ausencia);
        servicioAuditoria.registrar("ACTUALIZAR", "Ausencia", actualizada.getId(), null, null);
        return mapear(actualizada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaAusencia> listarPorEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaId(empresaId).stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaAusencia> listarPorEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorio.findByEmpleadoIdAndDeletedFalseOrderByFechaDesc(empleadoId).stream()
                .map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaAusencia> listarPorRango(LocalDate desde, LocalDate hasta) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (hasta.isBefore(desde)) {
            throw new ExcepcionNegocio("La fecha hasta debe ser posterior a la fecha desde");
        }
        return repositorio.findByEmpresaIdAndRangoFechas(empresaId, desde, hasta).stream()
                .map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaAusencia obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return mapear(obtenerEntidad(id, empresaId));
    }

    private Ausencia obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Ausencia no encontrada", 404));
    }

    private void validarCertificado(TipoAusencia tipo, boolean justificada, String urlCertificado) {
        if (tipo.isRequiereCertificado() && justificada
                && (urlCertificado == null || urlCertificado.isBlank())) {
            throw new ExcepcionNegocio(
                    "El tipo '" + tipo.getNombre() + "' requiere certificado adjunto cuando está justificada");
        }
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaAusencia mapear(Ausencia ausencia) {
        Empleado empleado = ausencia.getEmpleado();
        String nombreEmpleado = empleado.getNombre() + " " + empleado.getApellido();
        return new RespuestaAusencia(
                ausencia.getId(),
                empleado.getId(),
                nombreEmpleado,
                ausencia.getTipoAusencia().getId(),
                ausencia.getTipoAusencia().getNombre(),
                ausencia.getFecha(),
                ausencia.getMotivo(),
                ausencia.isJustificada(),
                ausencia.getUrlCertificado()
        );
    }

    public record EventoAusenciaRegistrada(Long ausenciaId, Long empleadoId, Long empresaId) {
    }
}
