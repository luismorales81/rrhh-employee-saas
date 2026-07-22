package com.rrhh.saas.offboarding.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import com.rrhh.saas.offboarding.dto.SolicitudActualizarPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dto.SolicitudCrearPlantillaTareaOffboarding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioOffboardingPlantillasTest {

    @Autowired
    private ServicioOffboarding servicioOffboarding;

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
    void crudPlantillasOffboarding() {
        var creada = servicioOffboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOffboarding(
                "Offboarding test", 50, true, "DEVOLVER_ACTIVO"));
        assertEquals("Offboarding test", creada.nombre());

        var actualizada = servicioOffboarding.actualizarPlantilla(creada.id(),
                new SolicitudActualizarPlantillaTareaOffboarding("Offboarding actualizada", 51, false, null));
        assertEquals("Offboarding actualizada", actualizada.nombre());
        assertEquals(51, actualizada.orden());
        assertFalse(actualizada.obligatoria());

        servicioOffboarding.eliminarPlantilla(creada.id());
        assertFalse(servicioOffboarding.listarPlantillas().stream()
                .anyMatch(p -> p.id().equals(creada.id())));
    }
}
