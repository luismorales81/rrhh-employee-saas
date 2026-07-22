package com.rrhh.saas.notificacion.evento;

import com.rrhh.saas.comunicacion.repositorio.RepositorioComunicacion;
import com.rrhh.saas.comunicacion.repositorio.RepositorioDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.servicio.ServicioComunicacion;
import com.rrhh.saas.documento.repositorio.RepositorioAsignacionDocumento;
import com.rrhh.saas.documento.servicio.ServicioAsignacionDocumento;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.notificacion.servicio.ServicioNotificacion;
import com.rrhh.saas.solicitud.dominio.Solicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioMotorWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EscuchadorEventosNotificaciones {

    private static final Logger log = LoggerFactory.getLogger(EscuchadorEventosNotificaciones.class);

    private final ServicioNotificacion servicioNotificacion;
    private final RepositorioSolicitud repositorioSolicitud;
    private final RepositorioAsignacionDocumento repositorioAsignacionDocumento;
    private final RepositorioComunicacion repositorioComunicacion;
    private final RepositorioDestinatarioComunicacion repositorioDestinatario;
    private final ServicioEmpleado servicioEmpleado;

    public EscuchadorEventosNotificaciones(ServicioNotificacion servicioNotificacion,
                                           RepositorioSolicitud repositorioSolicitud,
                                           RepositorioAsignacionDocumento repositorioAsignacionDocumento,
                                           RepositorioComunicacion repositorioComunicacion,
                                           RepositorioDestinatarioComunicacion repositorioDestinatario,
                                           ServicioEmpleado servicioEmpleado) {
        this.servicioNotificacion = servicioNotificacion;
        this.repositorioSolicitud = repositorioSolicitud;
        this.repositorioAsignacionDocumento = repositorioAsignacionDocumento;
        this.repositorioComunicacion = repositorioComunicacion;
        this.repositorioDestinatario = repositorioDestinatario;
        this.servicioEmpleado = servicioEmpleado;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alAsignarPaso(ServicioMotorWorkflow.EventoPasoAsignado evento) {
        crearNotificacionSegura(
                evento.empresaId(),
                evento.aprobadorId(),
                "Solicitud pendiente de aprobación",
                "Tenés una solicitud pendiente de aprobar",
                "SOLICITUD",
                evento.instanciaId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alAprobarSolicitud(ServicioMotorWorkflow.EventoSolicitudAprobada evento) {
        notificarSolicitante(
                evento.solicitudId(),
                evento.empresaId(),
                "Solicitud aprobada",
                "Tu solicitud fue aprobada",
                "SOLICITUD_APROBADA"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alRechazarSolicitud(ServicioMotorWorkflow.EventoSolicitudRechazada evento) {
        notificarSolicitante(
                evento.solicitudId(),
                evento.empresaId(),
                "Solicitud rechazada",
                "Tu solicitud fue rechazada",
                "SOLICITUD_RECHAZADA"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alAsignarDocumento(ServicioAsignacionDocumento.EventoDocumentoAsignado evento) {
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(evento.empleadoId(), evento.empresaId());
        if (empleado.getUsuario() == null) {
            return;
        }
        String titulo = repositorioAsignacionDocumento.findById(evento.asignacionId())
                .map(a -> a.getDocumento().getTitulo())
                .orElse("Nuevo documento");
        crearNotificacionSegura(
                evento.empresaId(),
                empleado.getUsuario().getId(),
                "Nuevo documento asignado",
                "Tenés un documento pendiente: " + titulo,
                "DOCUMENTO",
                evento.asignacionId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alPublicarComunicacion(ServicioComunicacion.EventoComunicacionPublicada evento) {
        var comunicacion = repositorioComunicacion.findByIdAndEmpresaIdAndDeletedFalse(
                evento.comunicacionId(), evento.empresaId()).orElse(null);
        if (comunicacion == null) {
            return;
        }
        repositorioDestinatario.findByComunicacionIdAndDeletedFalse(evento.comunicacionId())
                .forEach(destinatario -> {
                    Empleado empleado = destinatario.getEmpleado();
                    if (empleado.getUsuario() == null) {
                        return;
                    }
                    crearNotificacionSegura(
                            evento.empresaId(),
                            empleado.getUsuario().getId(),
                            "Nueva comunicación",
                            comunicacion.getTitulo(),
                            "COMUNICACION",
                            evento.comunicacionId()
                    );
                });
    }

    private void notificarSolicitante(Long solicitudId, Long empresaId, String titulo, String mensaje, String tipo) {
        Solicitud solicitud = repositorioSolicitud.findByIdAndEmpresaIdAndDeletedFalse(solicitudId, empresaId)
                .orElse(null);
        if (solicitud == null) {
            return;
        }
        Empleado empleado = solicitud.getEmpleado();
        if (empleado.getUsuario() == null) {
            return;
        }
        crearNotificacionSegura(
                empresaId,
                empleado.getUsuario().getId(),
                titulo,
                mensaje,
                tipo,
                solicitudId
        );
    }

    private void crearNotificacionSegura(Long empresaId, Long usuarioId, String titulo,
                                         String mensaje, String tipo, Long referenciaId) {
        try {
            servicioNotificacion.crear(empresaId, usuarioId, titulo, mensaje, tipo, referenciaId);
        } catch (Exception ex) {
            log.warn("No se pudo crear notificación {} para usuario {}: {}", tipo, usuarioId, ex.getMessage());
        }
    }
}
