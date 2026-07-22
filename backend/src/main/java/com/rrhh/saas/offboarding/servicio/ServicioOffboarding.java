package com.rrhh.saas.offboarding.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dominio.LegajoEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioLegajoEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.offboarding.dominio.PlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dominio.TareaOffboardingEmpleado;
import com.rrhh.saas.offboarding.dto.RespuestaPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dto.RespuestaTareaOffboardingEmpleado;
import com.rrhh.saas.offboarding.dto.SolicitudActualizarPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dto.SolicitudCrearPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.repositorio.RepositorioPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.repositorio.RepositorioTareaOffboardingEmpleado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioOffboarding {

    public static final String TIPO_DEVOLVER_ACTIVO = "DEVOLVER_ACTIVO";

    private final RepositorioPlantillaTareaOffboarding repositorioPlantilla;
    private final RepositorioTareaOffboardingEmpleado repositorioTarea;
    private final RepositorioLegajoEmpleado repositorioLegajo;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioOffboarding(RepositorioPlantillaTareaOffboarding repositorioPlantilla,
                               RepositorioTareaOffboardingEmpleado repositorioTarea,
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
    public List<RespuestaPlantillaTareaOffboarding> listarPlantillas() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioPlantilla.findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(empresaId).stream()
                .map(this::mapearPlantilla)
                .toList();
    }

    @Transactional
    public RespuestaPlantillaTareaOffboarding crearPlantilla(SolicitudCrearPlantillaTareaOffboarding solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        PlantillaTareaOffboarding plantilla = new PlantillaTareaOffboarding();
        plantilla.setEmpresa(empresa);
        plantilla.setNombre(solicitud.nombre());
        plantilla.setOrden(solicitud.orden());
        plantilla.setObligatoria(solicitud.obligatoria());
        plantilla.setTipo(solicitud.tipo());
        PlantillaTareaOffboarding guardada = repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("CREAR", "PlantillaTareaOffboarding", guardada.getId(), null, guardada.getNombre());
        return mapearPlantilla(guardada);
    }

    @Transactional
    public RespuestaPlantillaTareaOffboarding actualizarPlantilla(Long id,
            SolicitudActualizarPlantillaTareaOffboarding solicitud) {
        PlantillaTareaOffboarding plantilla = obtenerPlantilla(id);
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
        PlantillaTareaOffboarding guardada = repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("ACTUALIZAR", "PlantillaTareaOffboarding", guardada.getId(), null, guardada.getNombre());
        return mapearPlantilla(guardada);
    }

    @Transactional
    public void eliminarPlantilla(Long id) {
        PlantillaTareaOffboarding plantilla = obtenerPlantilla(id);
        plantilla.setDeleted(true);
        plantilla.setDeletedAt(LocalDateTime.now());
        repositorioPlantilla.save(plantilla);
        servicioAuditoria.registrar("ELIMINAR", "PlantillaTareaOffboarding", plantilla.getId(), null, plantilla.getNombre());
    }

    @Transactional
    public void generarTareasAlDesactivar(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        List<PlantillaTareaOffboarding> plantillas =
                repositorioPlantilla.findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(empresaId);

        repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId).ifPresent(legajo -> {
            legajo.setEstadoOffboarding("EN_PROGRESO");
            repositorioLegajo.save(legajo);
        });

        for (PlantillaTareaOffboarding plantilla : plantillas) {
            TareaOffboardingEmpleado tarea = new TareaOffboardingEmpleado();
            tarea.setEmpleado(empleado);
            tarea.setPlantilla(plantilla);
            repositorioTarea.save(tarea);
        }
    }

    @Transactional
    public void completarTareaDevolverActivo(Long empleadoId) {
        repositorioTarea.findPendientePorTipo(empleadoId, TIPO_DEVOLVER_ACTIVO).ifPresent(tarea -> {
            tarea.setCompletada(true);
            tarea.setFechaCompletado(LocalDateTime.now());
            tarea.setNotas("Devolución de activo registrada automáticamente");
            repositorioTarea.save(tarea);
            servicioAuditoria.registrar("COMPLETAR", "TareaOffboardingEmpleado", tarea.getId(), null, null);
            verificarYCompletarOffboarding(empleadoId);
        });
    }

    @Transactional(readOnly = true)
    public List<RespuestaTareaOffboardingEmpleado> listarTareasEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorioTarea.findByEmpleadoIdAndDeletedFalseOrderByPlantillaOrdenAsc(empleadoId).stream()
                .map(this::mapearTarea)
                .toList();
    }

    @Transactional
    public RespuestaTareaOffboardingEmpleado completarTarea(Long empleadoId, Long tareaId, String notas) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);

        TareaOffboardingEmpleado tarea = repositorioTarea.findByIdAndEmpleadoIdAndDeletedFalse(tareaId, empleadoId)
                .orElseThrow(() -> new ExcepcionNegocio("Tarea de offboarding no encontrada", 404));

        if (tarea.isCompletada()) {
            throw new ExcepcionNegocio("La tarea ya fue completada", 400);
        }

        tarea.setCompletada(true);
        tarea.setFechaCompletado(LocalDateTime.now());
        tarea.setNotas(notas);
        TareaOffboardingEmpleado guardada = repositorioTarea.save(tarea);
        servicioAuditoria.registrar("COMPLETAR", "TareaOffboardingEmpleado", guardada.getId(), null, null);
        verificarYCompletarOffboarding(empleadoId);
        return mapearTarea(guardada);
    }

    private void verificarYCompletarOffboarding(Long empleadoId) {
        if (repositorioTarea.tieneObligatoriasPendientes(empleadoId)) {
            return;
        }
        repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleadoId).ifPresent(legajo -> {
            legajo.setEstadoOffboarding("COMPLETADO");
            legajo.setFechaCompletadoOffboarding(LocalDateTime.now());
            repositorioLegajo.save(legajo);
        });
    }

    private PlantillaTareaOffboarding obtenerPlantilla(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioPlantilla.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Plantilla de offboarding no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        return servicioEmpresa.obtenerEntidad(ContextoTenant.obtenerEmpresaId());
    }

    private RespuestaPlantillaTareaOffboarding mapearPlantilla(PlantillaTareaOffboarding plantilla) {
        return new RespuestaPlantillaTareaOffboarding(
                plantilla.getId(),
                plantilla.getNombre(),
                plantilla.getOrden(),
                plantilla.isObligatoria(),
                plantilla.getTipo()
        );
    }

    private RespuestaTareaOffboardingEmpleado mapearTarea(TareaOffboardingEmpleado tarea) {
        PlantillaTareaOffboarding plantilla = tarea.getPlantilla();
        return new RespuestaTareaOffboardingEmpleado(
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
