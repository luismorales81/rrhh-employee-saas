package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.vacacion.dominio.DiaSemana;
import com.rrhh.saas.vacacion.dominio.PatronDescanso;
import com.rrhh.saas.vacacion.dominio.PatronDescansoDia;
import com.rrhh.saas.vacacion.dto.RespuestaPatronDescanso;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPatronDescanso;
import com.rrhh.saas.vacacion.repositorio.RepositorioPatronDescanso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioPatronDescanso {

    private final RepositorioPatronDescanso repositorio;
    private final ServicioEmpleado servicioEmpleado;

    public ServicioPatronDescanso(RepositorioPatronDescanso repositorio,
                                  ServicioEmpleado servicioEmpleado) {
        this.repositorio = repositorio;
        this.servicioEmpleado = servicioEmpleado;
    }

    @Transactional
    public RespuestaPatronDescanso crear(Long empleadoId, SolicitudCrearPatronDescanso solicitud) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        validarDias(solicitud.diasDescanso());

        PatronDescanso patron = new PatronDescanso();
        patron.setEmpleado(empleado);
        patron.setVigenciaDesde(solicitud.vigenciaDesde());
        patron.setVigenciaHasta(solicitud.vigenciaHasta());
        agregarDias(patron, solicitud.diasDescanso());

        return mapear(repositorio.save(patron));
    }

    @Transactional(readOnly = true)
    public List<RespuestaPatronDescanso> listarPorEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorio.findByEmpleadoIdAndDeletedFalse(empleadoId).stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatronDescanso obtenerPatronVigente(Long empleadoId, LocalDate fecha) {
        List<PatronDescanso> vigentes = repositorio.buscarVigentesEn(empleadoId, fecha);
        return vigentes.isEmpty() ? null : vigentes.get(0);
    }

    @Transactional(readOnly = true)
    public List<DiaSemana> obtenerDiasDescansoVigentes(Long empleadoId, LocalDate fecha) {
        PatronDescanso patron = obtenerPatronVigente(empleadoId, fecha);
        if (patron == null) {
            return List.of(DiaSemana.SABADO, DiaSemana.DOMINGO);
        }
        return patron.getDiasDescanso().stream()
                .map(PatronDescansoDia::getDiaSemana)
                .toList();
    }

    private void validarDias(List<DiaSemana> dias) {
        if (dias == null || dias.isEmpty()) {
            throw new ExcepcionNegocio("Debe indicar al menos un día de descanso");
        }
    }

    private void agregarDias(PatronDescanso patron, List<DiaSemana> dias) {
        for (DiaSemana dia : dias) {
            PatronDescansoDia entidad = new PatronDescansoDia();
            entidad.setPatron(patron);
            entidad.setDiaSemana(dia);
            patron.getDiasDescanso().add(entidad);
        }
    }

    private RespuestaPatronDescanso mapear(PatronDescanso patron) {
        List<DiaSemana> dias = patron.getDiasDescanso().stream()
                .map(PatronDescansoDia::getDiaSemana)
                .toList();
        return new RespuestaPatronDescanso(
                patron.getId(),
                patron.getEmpleado().getId(),
                dias,
                patron.getVigenciaDesde(),
                patron.getVigenciaHasta()
        );
    }
}
