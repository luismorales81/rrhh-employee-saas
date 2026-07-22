package com.rrhh.saas.offboarding.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dto.SolicitudCrearEmpleado;
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
class ServicioOffboardingTest {

    @Autowired
    private ServicioOffboarding servicioOffboarding;

    @Autowired
    private ServicioEmpleado servicioEmpleado;

    @Autowired
    private RepositorioLegajoEmpleado repositorioLegajo;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @BeforeEach
    void establecerContexto() {
        Empresa empresa = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> "Demo SA".equals(e.getNombre()))
                .findFirst()
                .orElseThrow();
        ContextoTenant.establecerEmpresaId(empresa.getId());
    }

    @AfterEach
    void limpiarContexto() {
        ContextoTenant.limpiar();
    }

    @Test
    void desactivarGeneraTareasYCompletarCierraOffboarding() {
        var empleado = servicioEmpleado.crear(new SolicitudCrearEmpleado(
                "Offboarding", "Test", null, null, null,
                null, LocalDate.now(), null, null, null,
                null, null, null, null, null, null
        ));

        servicioOffboarding.generarTareasAlDesactivar(empleado.id());
        var tareas = servicioOffboarding.listarTareasEmpleado(empleado.id());
        assertFalse(tareas.isEmpty());

        var legajo = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleado.id()).orElseThrow();
        assertEquals("EN_PROGRESO", legajo.getEstadoOffboarding());

        for (var tarea : tareas) {
            if (!tarea.completada()) {
                servicioOffboarding.completarTarea(empleado.id(), tarea.id(), "Hecho en test");
            }
        }

        var legajoFinal = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleado.id()).orElseThrow();
        assertEquals("COMPLETADO", legajoFinal.getEstadoOffboarding());
        assertTrue(legajoFinal.getFechaCompletadoOffboarding() != null);
    }
}
