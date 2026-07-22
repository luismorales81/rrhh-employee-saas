package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import com.rrhh.saas.solicitud.dominio.EstadoInstanciaFlujo;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.InstanciaFlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.Solicitud;
import com.rrhh.saas.solicitud.dominio.TipoSolicitud;
import com.rrhh.saas.solicitud.dto.RespuestaSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudAccionWorkflow;
import com.rrhh.saas.solicitud.dto.SolicitudCrearSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioInstanciaFlujoTrabajo;
import com.rrhh.saas.solicitud.repositorio.RepositorioSolicitud;
import com.rrhh.saas.vacacion.dominio.SolicitudVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioSolicitudVacaciones;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioSolicitud {

    private final RepositorioSolicitud repositorioSolicitud;
    private final RepositorioSolicitudVacaciones repositorioSolicitudVacaciones;
    private final RepositorioInstanciaFlujoTrabajo repositorioInstancia;
    private final ServicioTipoSolicitud servicioTipoSolicitud;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioMotorWorkflow servicioMotorWorkflow;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioSolicitud(RepositorioSolicitud repositorioSolicitud,
                             RepositorioSolicitudVacaciones repositorioSolicitudVacaciones,
                             RepositorioInstanciaFlujoTrabajo repositorioInstancia,
                             ServicioTipoSolicitud servicioTipoSolicitud,
                             ServicioEmpleado servicioEmpleado,
                             ServicioUsuario servicioUsuario,
                             ServicioEmpresa servicioEmpresa,
                             ServicioMotorWorkflow servicioMotorWorkflow,
                             ServicioAuditoria servicioAuditoria,
                             ApplicationEventPublisher publicadorEventos) {
        this.repositorioSolicitud = repositorioSolicitud;
        this.repositorioSolicitudVacaciones = repositorioSolicitudVacaciones;
        this.repositorioInstancia = repositorioInstancia;
        this.servicioTipoSolicitud = servicioTipoSolicitud;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioMotorWorkflow = servicioMotorWorkflow;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public RespuestaSolicitud crear(SolicitudCrearSolicitud solicitudDto) {
        Empresa empresa = obtenerEmpresaActual();
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);

        TipoSolicitud tipo = servicioTipoSolicitud.obtenerEntidad(solicitudDto.tipoSolicitudId(), empresa.getId());

        if ("Vacaciones".equalsIgnoreCase(tipo.getNombre())) {
            throw new ExcepcionNegocio(
                    "Las solicitudes de vacaciones deben crearse desde el módulo de Vacaciones", 400);
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setEmpresa(empresa);
        solicitud.setEmpleado(empleado);
        solicitud.setTipoSolicitud(tipo);
        solicitud.setMotivo(solicitudDto.motivo());
        solicitud.setEstado(EstadoSolicitud.BORRADOR);

        Solicitud guardada = repositorioSolicitud.save(solicitud);
        servicioAuditoria.registrar("CREAR", "Solicitud", guardada.getId(), null, tipo.getNombre());
        publicadorEventos.publishEvent(new EventoSolicitudCreada(
                guardada.getId(), empresa.getId(), empleado.getId(), tipo.getId()));

        if (tipo.isRequiereAprobacion() && tipo.getFlujoTrabajo() != null) {
            enviar(guardada.getId());
            guardada = repositorioSolicitud.findById(guardada.getId()).orElse(guardada);
        } else if (!tipo.isRequiereAprobacion()) {
            guardada.setEstado(EstadoSolicitud.APROBADO);
            repositorioSolicitud.save(guardada);
        }

        return mapear(guardada);
    }

    @Transactional
    public RespuestaSolicitud enviar(Long solicitudId) {
        Empresa empresa = obtenerEmpresaActual();
        Solicitud solicitud = obtenerEntidad(solicitudId, empresa.getId());

        if (solicitud.getEstado() != EstadoSolicitud.BORRADOR
                && solicitud.getEstado() != EstadoSolicitud.CAMBIOS_SOLICITADOS) {
            throw new ExcepcionNegocio("La solicitud no puede enviarse en su estado actual");
        }

        TipoSolicitud tipo = solicitud.getTipoSolicitud();
        if (!tipo.isRequiereAprobacion() || tipo.getFlujoTrabajo() == null) {
            solicitud.setEstado(EstadoSolicitud.APROBADO);
            repositorioSolicitud.save(solicitud);
            return mapear(solicitud);
        }

        FlujoTrabajo flujo = tipo.getFlujoTrabajo();
        servicioMotorWorkflow.iniciar(solicitud, flujo);
        solicitud.setEstado(EstadoSolicitud.EN_REVISION);
        Solicitud actualizada = repositorioSolicitud.save(solicitud);

        publicadorEventos.publishEvent(new EventoSolicitudEnviada(actualizada.getId(), empresa.getId()));
        return mapear(actualizada);
    }

    @Transactional
    public RespuestaSolicitud procesarAccion(Long solicitudId, SolicitudAccionWorkflow accionDto) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Usuario usuario = servicioUsuario.obtenerEntidad(usuarioId);
        Solicitud solicitud = servicioMotorWorkflow.procesarAccion(
                solicitudId, accionDto.accion(), accionDto.comentarios(), usuario);
        return mapear(repositorioSolicitud.save(solicitud));
    }

    @Transactional
    public RespuestaSolicitud cancelar(Long solicitudId) {
        Empresa empresa = obtenerEmpresaActual();
        Solicitud solicitud = obtenerEntidad(solicitudId, empresa.getId());

        if (solicitud.getEstado() == EstadoSolicitud.APROBADO
                || solicitud.getEstado() == EstadoSolicitud.RECHAZADO) {
            throw new ExcepcionNegocio("No se puede cancelar una solicitud finalizada");
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADO);
        repositorioInstancia.findBySolicitudId(solicitudId).ifPresent(instancia -> {
            instancia.setEstado(EstadoInstanciaFlujo.CANCELADO);
            repositorioInstancia.save(instancia);
        });

        servicioAuditoria.registrar("CANCELAR", "Solicitud", solicitudId, null, null);
        publicadorEventos.publishEvent(new EventoSolicitudCancelada(solicitudId, empresa.getId()));
        return mapear(repositorioSolicitud.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<RespuestaSolicitud> listarPropias() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return repositorioSolicitud.findByEmpleadoIdAndDeletedFalse(empleado.getId()).stream()
                .map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaSolicitud> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioSolicitud.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaSolicitud> listarPendientesAprobacion() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        return repositorioInstancia.findByAprobadorActualIdAndEstado(usuarioId, EstadoInstanciaFlujo.EN_PROGRESO)
                .stream()
                .map(InstanciaFlujoTrabajo::getSolicitud)
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaSolicitud obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return mapear(obtenerEntidad(id, empresaId));
    }

    private Solicitud obtenerEntidad(Long id, Long empresaId) {
        return repositorioSolicitud.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Solicitud no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaSolicitud mapear(Solicitud solicitud) {
        Optional<InstanciaFlujoTrabajo> instancia = repositorioInstancia.findBySolicitudId(solicitud.getId());
        Empleado empleado = solicitud.getEmpleado();
        String empleadoNombre = empleado.getNombre() + " " + empleado.getApellido();
        Optional<SolicitudVacaciones> vacaciones = repositorioSolicitudVacaciones.findBySolicitudId(solicitud.getId());
        return new RespuestaSolicitud(
                solicitud.getId(),
                empleado.getId(),
                empleadoNombre,
                solicitud.getTipoSolicitud().getId(),
                solicitud.getTipoSolicitud().getNombre(),
                solicitud.getEstado(),
                solicitud.getMotivo(),
                instancia.map(InstanciaFlujoTrabajo::getId).orElse(null),
                instancia.map(i -> i.getAprobadorActual() != null ? i.getAprobadorActual().getId() : null).orElse(null),
                vacaciones.map(SolicitudVacaciones::getFechaInicio).orElse(null),
                vacaciones.map(SolicitudVacaciones::getFechaFin).orElse(null),
                vacaciones.map(SolicitudVacaciones::getId).orElse(null),
                solicitud.getCreatedAt()
        );
    }

    public record EventoSolicitudCreada(Long solicitudId, Long empresaId, Long empleadoId, Long tipoSolicitudId) {
    }

    public record EventoSolicitudEnviada(Long solicitudId, Long empresaId) {
    }

    public record EventoSolicitudCancelada(Long solicitudId, Long empresaId) {
    }
}
