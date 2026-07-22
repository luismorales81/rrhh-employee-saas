package com.rrhh.saas.empleado.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.documento.dominio.EstadoAsignacionDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioAsignacionDocumento;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dominio.EstadoEmpleado;
import com.rrhh.saas.empleado.dominio.LegajoEmpleado;
import com.rrhh.saas.empleado.dto.RespuestaLegajoEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioLegajoEmpleado;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioSolicitud;
import com.rrhh.saas.onboarding.servicio.ServicioOnboarding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ServicioLegajo {

    private final RepositorioLegajoEmpleado repositorioLegajo;
    private final ServicioEmpleado servicioEmpleado;
    private final RepositorioAsignacionDocumento repositorioAsignacionDocumento;
    private final RepositorioSolicitud repositorioSolicitud;
    private final ServicioOnboarding servicioOnboarding;

    public ServicioLegajo(RepositorioLegajoEmpleado repositorioLegajo,
                          ServicioEmpleado servicioEmpleado,
                          RepositorioAsignacionDocumento repositorioAsignacionDocumento,
                          RepositorioSolicitud repositorioSolicitud,
                          ServicioOnboarding servicioOnboarding) {
        this.repositorioLegajo = repositorioLegajo;
        this.servicioEmpleado = servicioEmpleado;
        this.repositorioAsignacionDocumento = repositorioAsignacionDocumento;
        this.repositorioSolicitud = repositorioSolicitud;
        this.servicioOnboarding = servicioOnboarding;
    }

    @Transactional(readOnly = true)
    public RespuestaLegajoEmpleado obtenerLegajo(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        LegajoEmpleado legajo = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .orElseThrow(() -> new ExcepcionNegocio("Legajo no encontrado", 404));

        long docsPendientes = repositorioAsignacionDocumento
                .countByEmpleadoIdAndEstadoNotAndDeletedFalse(empleadoId, EstadoAsignacionDocumento.FIRMADO);
        long solicitudesAbiertas = repositorioSolicitud.findByEmpleadoIdAndDeletedFalse(empleadoId).stream()
                .filter(s -> s.getEstado() != EstadoSolicitud.APROBADO
                        && s.getEstado() != EstadoSolicitud.RECHAZADO
                        && s.getEstado() != EstadoSolicitud.CANCELADO)
                .count();

        return new RespuestaLegajoEmpleado(
                legajo.getId(),
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getNumeroEmpleado(),
                empleado.getDni(),
                empleado.getFechaIngreso(),
                empleado.getEstado(),
                legajo.getEstadoOnboarding(),
                legajo.getFechaCompletadoOnboarding(),
                legajo.getEstadoOffboarding(),
                legajo.getFechaCompletadoOffboarding(),
                docsPendientes,
                solicitudesAbiertas
        );
    }

    @Transactional
    public RespuestaLegajoEmpleado iniciarOnboarding(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        LegajoEmpleado legajo = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .orElseThrow(() -> new ExcepcionNegocio("Legajo no encontrado", 404));

        legajo.setEstadoOnboarding("EN_PROGRESO");
        if (empleado.getEstado() == EstadoEmpleado.PENDIENTE_INGRESO) {
            empleado.setEstado(EstadoEmpleado.ACTIVO);
        }
        repositorioLegajo.save(legajo);
        servicioOnboarding.generarTareasAlIniciar(empleadoId);
        return obtenerLegajo(empleadoId);
    }

    @Transactional
    public RespuestaLegajoEmpleado completarOnboarding(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        LegajoEmpleado legajo = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .orElseThrow(() -> new ExcepcionNegocio("Legajo no encontrado", 404));
        legajo.setEstadoOnboarding("COMPLETADO");
        legajo.setFechaCompletadoOnboarding(LocalDateTime.now());
        repositorioLegajo.save(legajo);
        return obtenerLegajo(empleadoId);
    }
}
