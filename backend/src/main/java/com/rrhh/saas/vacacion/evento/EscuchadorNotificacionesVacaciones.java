package com.rrhh.saas.vacacion.evento;

import com.rrhh.saas.notificacion.servicio.ServicioNotificacion;
import com.rrhh.saas.solicitud.repositorio.RepositorioSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioMotorWorkflow;
import com.rrhh.saas.vacacion.servicio.ServicioVacaciones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EscuchadorNotificacionesVacaciones {

    private static final Logger log = LoggerFactory.getLogger(EscuchadorNotificacionesVacaciones.class);

    private final ServicioNotificacion servicioNotificacion;
    private final RepositorioSolicitud repositorioSolicitud;

    public EscuchadorNotificacionesVacaciones(ServicioNotificacion servicioNotificacion,
                                              RepositorioSolicitud repositorioSolicitud) {
        this.servicioNotificacion = servicioNotificacion;
        this.repositorioSolicitud = repositorioSolicitud;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alSolicitarVacaciones(ServicioVacaciones.EventoVacacionesSolicitadas evento) {
        log.debug("Vacaciones solicitadas: solicitudId={}", evento.solicitudId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alAprobarVacaciones(ServicioVacaciones.EventoVacacionesAprobadas evento) {
        notificarEmpleado(evento.empresaId(), evento.empleadoId(), evento.solicitudId(),
                "Vacaciones aprobadas", "Tu solicitud de vacaciones fue aprobada", "VACACIONES_APROBADAS");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alRechazarVacaciones(ServicioVacaciones.EventoVacacionesRechazadas evento) {
        notificarEmpleado(evento.empresaId(), evento.empleadoId(), evento.solicitudId(),
                "Vacaciones rechazadas", "Tu solicitud de vacaciones fue rechazada", "VACACIONES_RECHAZADAS");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alContrapropuesta(ServicioVacaciones.EventoVacacionesContrapropuesta evento) {
        notificarEmpleado(evento.empresaId(), evento.empleadoId(), evento.solicitudId(),
                "Contrapropuesta de vacaciones",
                "El empleador propuso fechas alternativas para tus vacaciones",
                "VACACIONES_CONTRAPROPUESTA");
    }

    private void notificarEmpleado(Long empresaId, Long empleadoId, Long solicitudId,
                                   String titulo, String mensaje, String tipo) {
        repositorioSolicitud.findById(solicitudId).ifPresent(solicitud -> {
            var usuario = solicitud.getEmpleado().getUsuario();
            if (usuario == null) {
                return;
            }
            try {
                servicioNotificacion.crear(empresaId, usuario.getId(), titulo, mensaje, tipo, solicitudId);
            } catch (Exception ex) {
                log.warn("No se pudo notificar vacaciones {}: {}", tipo, ex.getMessage());
            }
        });
    }
}
