package com.rrhh.saas.vacacion.evento;

import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.solicitud.servicio.ServicioMotorWorkflow;
import com.rrhh.saas.solicitud.servicio.ServicioSolicitud;
import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioPoliticaVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioSaldoVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioVacaciones;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EscuchadorEventosVacaciones {

    private final ServicioVacaciones servicioVacaciones;
    private final ServicioSaldoVacaciones servicioSaldoVacaciones;
    private final RepositorioPoliticaVacaciones repositorioPoliticaVacaciones;
    private final ServicioEmpleado servicioEmpleado;

    public EscuchadorEventosVacaciones(ServicioVacaciones servicioVacaciones,
                                       ServicioSaldoVacaciones servicioSaldoVacaciones,
                                       RepositorioPoliticaVacaciones repositorioPoliticaVacaciones,
                                       ServicioEmpleado servicioEmpleado) {
        this.servicioVacaciones = servicioVacaciones;
        this.servicioSaldoVacaciones = servicioSaldoVacaciones;
        this.repositorioPoliticaVacaciones = repositorioPoliticaVacaciones;
        this.servicioEmpleado = servicioEmpleado;
    }

    @EventListener
    public void alAprobarSolicitud(ServicioMotorWorkflow.EventoSolicitudAprobada evento) {
        servicioVacaciones.procesarAprobacion(evento.solicitudId());
    }

    @EventListener
    public void alRechazarSolicitud(ServicioMotorWorkflow.EventoSolicitudRechazada evento) {
        servicioVacaciones.procesarRechazoOCancelacion(evento.solicitudId());
    }

    @EventListener
    public void alCancelarSolicitud(ServicioSolicitud.EventoSolicitudCancelada evento) {
        servicioVacaciones.procesarRechazoOCancelacion(evento.solicitudId());
    }

    @EventListener
    public void alCrearEmpleado(ServicioEmpleado.EventoEmpleadoCreado evento) {
        Optional<PoliticaVacaciones> politica = repositorioPoliticaVacaciones
                .findByEmpresaIdAndActivaTrueAndDeletedFalse(evento.empresaId());
        if (politica.isEmpty()) {
            return;
        }
        var empleado = servicioEmpleado.obtenerEmpleadoEntidad(evento.empleadoId(), evento.empresaId());
        servicioSaldoVacaciones.inicializarSaldo(empleado, politica.get());
    }
}
