package com.rrhh.saas.onboarding.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dominio.LegajoEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioLegajoEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.onboarding.dominio.PlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dominio.TareaOnboardingEmpleado;
import com.rrhh.saas.onboarding.dto.RespuestaPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dto.RespuestaTareaOnboardingEmpleado;
import com.rrhh.saas.onboarding.dto.SolicitudActualizarPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dto.SolicitudCrearPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.repositorio.RepositorioPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.repositorio.RepositorioTareaOnboardingEmpleado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioOnboarding {

    private final RepositorioPlantillaTareaOnboarding repositorioPlantilla;
    private final RepositorioTareaOnboardingEmpleado repositorioTarea;
    private final RepositorioLegajoEmpleado repositorioLegajo;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioOnboarding(RepositorioPlantillaTareaOnboarding repositorioPlantilla,
                              RepositorioTareaOnboardingEmpleado repositorioTarea,
                              RepositorioLegajoEmpleado repositorioLegajo,
                              ServicioEmpleado servicioEmpleado,
                              ServicioEmpresa servicioEmpresa,
                              ServicioAuditoria servicioAuditoria) {
        this.repositorioPlantilla = repositorioPlantilla;
        this.repositorioTarea = repositorioTarea;
        this.repositorioLegajo = repositorioLegajo;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional(readOnly = true)
    public List<RespuestaPlantillaTareaOnboarding> listarPlantillas() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioPlantilla.findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(empresaId).stream()
                .map(this::mapearPlantilla)
                .toList();
    }

    @Transactional
    public RespuestaPlantillaTareaOnboarding crearPlantilla(SolicitudCrearPlantillaTareaOnboarding solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        PlantillaTareaOnboarding plantilla = new PlantillaTareaOnboarding();
        plantilla.setEmpresa(empresa);
        plantilla.setNombre(solicitud.nombre());
        plantilla.setOrden(solicitud.orden());
        plantilla.setObligatoria(solicitud.obligatoria());
        plantilla.setTipo(solicitud.tipo());
        PlantillaTareaOnboarding guardada = repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("CREAR", "PlantillaTareaOnboarding", guardada.getId(), null, guardada.getNombre());
        return mapearPlantilla(guardada);
    }

    @Transactional
    public RespuestaPlantillaTareaOnboarding actualizarPlantilla(Long id,
            SolicitudActualizarPlantillaTareaOnboarding solicitud) {
        PlantillaTareaOnboarding plantilla = obtenerPlantilla(id);
        if (solicitud.nombre() != null) {
            plantilla.setNombre(solicitud.nombre());
        }
        if (solicitud.orden() != null) {
            plantilla.setOrden(solicitud.orden());
        }
        if (solicitud.obligatoria() != null) {
            plantilla.setObligatoria(solicitud.obligatoria());
        }
        if (solicitud.tipo() != null) {
            plantilla.setTipo(solicitud.tipo());
        }
        PlantillaTareaOnboarding guardada = repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("ACTUALIZAR", "PlantillaTareaOnboarding", guardada.getId(), null, guardada.getNombre());
        return mapearPlantilla(guardada);
    }

    @Transactional
    public void eliminarPlantilla(Long id) {
        PlantillaTareaOnboarding plantilla = obtenerPlantilla(id);
        plantilla.setDeleted(true);
        plantilla.setDeletedAt(LocalDateTime.now());
        repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("ELIMINAR", "PlantillaTareaOnboarding", plantilla.getId(), null, plantilla.getNombre());
    }

    @Transactional
    public void generarTareasAlIniciar(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        List<PlantillaTareaOnboarding> plantillas =
                repositorioPlantilla.findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(empresaId);

        List<TareaOnboardingEmpleado> existentes =
                repositorioTarea.findByEmpleadoIdAndDeletedFalseOrderByPlantillaOrdenAsc(empleadoId);
        if (!existentes.isEmpty()) {
            return;
        }

        for (PlantillaTareaOnboarding plantilla : plantillas) {
            TareaOnboardingEmpleado tarea = new TareaOnboardingEmpleado();
            tarea.setEmpleado(empleado);
            tarea.setPlantilla(plantilla);
            repositorioTarea.save(tarea);
        }
    }

    @Transactional(readOnly = true)
    public List<RespuestaTareaOnboardingEmpleado> listarTareasEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorioTarea.findByEmpleadoIdAndDeletedFalseOrderByPlantillaOrdenAsc(empleadoId).stream()
                .map(this::mapearTarea)
                .toList();
    }

    @Transactional
    public RespuestaTareaOnboardingEmpleado completarTarea(Long empleadoId, Long tareaId, String notas) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);

        TareaOnboardingEmpleado tarea = repositorioTarea.findByIdAndEmpleadoIdAndDeletedFalse(tareaId, empleadoId)
                .orElseThrow(() -> new ExcepcionNegocio("Tarea de onboarding no encontrada", 404));

        if (tarea.isCompletada()) {
            throw new ExcepcionNegocio("La tarea ya fue completada", 400);
        }

        tarea.setCompletada(true);
        tarea.setFechaCompletado(LocalDateTime.now());
        tarea.setNotas(notas);
        TareaOnboardingEmpleado guardada = repositorioTarea.save(tarea);
        servicioAuditoria.registrar("COMPLETAR", "TareaOnboardingEmpleado", guardada.getId(), null, null);

        verificarYCompletarOnboarding(empleadoId);
        return mapearTarea(guardada);
    }

    private void verificarYCompletarOnboarding(Long empleadoId) {
        boolean pendientes = repositorioTarea.tieneObligatoriasPendientes(empleadoId);
        if (pendientes) {
            return;
        }
        repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId).ifPresent(legajo -> {
            legajo.setEstadoOnboarding("COMPLETADO");
            legajo.setFechaCompletadoOnboarding(LocalDateTime.now());
            repositorioLegajo.save(legajo);
        });
    }

    private PlantillaTareaOnboarding obtenerPlantilla(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioPlantilla.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Plantilla de onboarding no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        return servicioEmpresa.obtenerEntidad(ContextoTenant.obtenerEmpresaId());
    }

    private RespuestaPlantillaTareaOnboarding mapearPlantilla(PlantillaTareaOnboarding plantilla) {
        return new RespuestaPlantillaTareaOnboarding(
                plantilla.getId(),
                plantilla.getNombre(),
                plantilla.getOrden(),
                plantilla.isObligatoria(),
                plantilla.getTipo()
        );
    }

    private RespuestaTareaOnboardingEmpleado mapearTarea(TareaOnboardingEmpleado tarea) {
        PlantillaTareaOnboarding plantilla = tarea.getPlantilla();
        return new RespuestaTareaOnboardingEmpleado(
                tarea.getId(),
                plantilla.getId(),
                plantilla.getNombre(),
                plantilla.getOrden(),
                plantilla.isObligatoria(),
                plantilla.getTipo(),
                tarea.isCompletada(),
                tarea.getFechaCompletado(),
                tarea.getNotas()
        );
    }
}
