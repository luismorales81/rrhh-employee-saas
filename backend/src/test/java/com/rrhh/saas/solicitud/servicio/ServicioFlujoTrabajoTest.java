package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import com.rrhh.saas.solicitud.dominio.TipoPaso;
import com.rrhh.saas.solicitud.dto.SolicitudActualizarFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudCrearFlujoTrabajo;
import com.rrhh.saas.solicitud.repositorio.RepositorioFlujoTrabajo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioFlujoTrabajoTest {

    @Autowired
    private ServicioFlujoTrabajo servicioFlujoTrabajo;

    @Autowired
    private RepositorioFlujoTrabajo repositorioFlujo;

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
    void actualizarAlternarActivoYEliminar() {
        var creado = servicioFlujoTrabajo.crear(new SolicitudCrearFlujoTrabajo(
                "Flujo test",
                "Descripción inicial",
                List.of(new SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo(
                        1, "Paso RRHH", TipoPaso.RRHH, null, null
                ))
        ));
        assertEquals(1, creado.pasos().size());
        assertTrue(creado.activo());

        var actualizado = servicioFlujoTrabajo.actualizar(creado.id(), new SolicitudActualizarFlujoTrabajo(
                "Flujo test actualizado",
                "Nueva descripción",
                List.of(
                        new SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo(
                                1, "Paso supervisor", TipoPaso.SUPERVISOR_DIRECTO, null, null
                        ),
                        new SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo(
                                2, "Paso gerencia", TipoPaso.GERENCIA, null, null
                        )
                )
        ));
        assertEquals("Flujo test actualizado", actualizado.nombre());
        assertEquals(2, actualizado.pasos().size());

        var desactivado = servicioFlujoTrabajo.alternarActivo(creado.id());
        assertFalse(desactivado.activo());

        var reactivado = servicioFlujoTrabajo.alternarActivo(creado.id());
        assertTrue(reactivado.activo());

        servicioFlujoTrabajo.eliminar(creado.id());
        assertTrue(repositorioFlujo.findById(creado.id()).orElseThrow().isDeleted());
    }
}
