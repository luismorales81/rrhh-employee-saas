package com.rrhh.saas.onboarding.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dto.SolicitudCrearEmpleado;
import com.rrhh.saas.onboarding.dto.SolicitudActualizarPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dto.SolicitudCrearPlantillaTareaOnboarding;
import com.rrhh.saas.empleado.repositorio.RepositorioLegajoEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioOnboardingTest {

    @Autowired
    private ServicioOnboarding servicioOnboarding;

    @Autowired
    private ServicioEmpleado servicioEmpleado;

    @Autowired
    private RepositorioLegajoEmpleado repositorioLegajo;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    private Long empresaId;

    @BeforeEach
    void establecerContexto() {
        Empresa empresa = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> "Demo SA".equals(e.getNombre()))
                .findFirst()
                .orElseThrow();
        empresaId = empresa.getId();
        ContextoTenant.establecerEmpresaId(empresaId);
    }

    @AfterEach
    void limpiarContexto() {
        ContextoTenant.limpiar();
    }

    @Test
    void iniciarGeneraTareasYCompletarCierraOnboarding() {
        var empleado = servicioEmpleado.crear(new SolicitudCrearEmpleado(
                "Onboarding", "Test", null, null, null,
                null, LocalDate.now(), null, null, null,
                null, null, null, null, null, null
        ));

        servicioOnboarding.generarTareasAlIniciar(empleado.id());
        var tareas = servicioOnboarding.listarTareasEmpleado(empleado.id());
        assertFalse(tareas.isEmpty());

        var legajo = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleado.id()).orElseThrow();
        legajo.setEstadoOnboarding("EN_PROGRESO");
        repositorioLegajo.save(legajo);

        for (var tarea : tareas) {
            if (!tarea.completada()) {
                servicioOnboarding.completarTarea(empleado.id(), tarea.id(), "Hecho en test");
            }
        }

        var legajoFinal = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleado.id()).orElseThrow();
        assertEquals("COMPLETADO", legajoFinal.getEstadoOnboarding());
        assertTrue(legajoFinal.getFechaCompletadoOnboarding() != null);
        assertTrue(servicioOnboarding.listarTareasEmpleado(empleado.id()).stream().allMatch(t -> t.completada()));
    }

    @Test
    void crudPlantillasOnboarding() {
        var creada = servicioOnboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOnboarding(
                "Plantilla test", 99, true, "PERFIL"));
        assertEquals("Plantilla test", creada.nombre());

        var actualizada = servicioOnboarding.actualizarPlantilla(creada.id(),
                new SolicitudActualizarPlantillaTareaOnboarding("Plantilla actualizada", 100, false, null));
        assertEquals("Plantilla actualizada", actualizada.nombre());
        assertEquals(100, actualizada.orden());
        assertFalse(actualizada.obligatoria());

        servicioOnboarding.eliminarPlantilla(creada.id());
        assertFalse(servicioOnboarding.listarPlantillas().stream()
                .anyMatch(p -> p.id().equals(creada.id())));
    }
}
