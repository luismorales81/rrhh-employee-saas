package com.rrhh.saas.activo.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.activo.dominio.EstadoActivo;
import com.rrhh.saas.activo.dto.SolicitudAsignarActivo;
import com.rrhh.saas.activo.dto.SolicitudCrearActivo;
import com.rrhh.saas.activo.dto.SolicitudDevolverActivo;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioActivoTest {

    @Autowired
    private ServicioActivo servicioActivo;

    @Autowired
    private RepositorioEmpleado repositorioEmpleado;

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
    void asignarYDevolverActivo() {
        Long empleadoId = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(
                        ContextoTenant.obtenerEmpresaId()).stream()
                .findFirst()
                .orElseThrow()
                .getId();

        var activo = servicioActivo.crear(new SolicitudCrearActivo(
                "Notebook test", "NOTEBOOK", "SN-TEST-001"
        ));
        assertEquals(EstadoActivo.DISPONIBLE, activo.estado());

        var asignacion = servicioActivo.asignar(activo.id(), new SolicitudAsignarActivo(
                empleadoId, LocalDate.now(), "Entrega test"
        ));
        assertNotNull(asignacion.id());
        assertEquals(EstadoActivo.ASIGNADO, servicioActivo.obtenerPorId(activo.id()).estado());

        var devolucion = servicioActivo.devolver(activo.id(), new SolicitudDevolverActivo(
                LocalDate.now(), "Devolución test"
        ));
        assertNotNull(devolucion.fechaDevolucion());
        assertEquals(EstadoActivo.DISPONIBLE, servicioActivo.obtenerPorId(activo.id()).estado());
    }
}
