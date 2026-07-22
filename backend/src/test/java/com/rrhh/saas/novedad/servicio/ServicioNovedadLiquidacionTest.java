package com.rrhh.saas.novedad.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.novedad.dominio.TipoNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudActualizarNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudCrearNovedadLiquidacion;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioNovedadLiquidacionTest {

    @Autowired
    private ServicioNovedadLiquidacion servicioNovedad;

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
    void exportarCsvIncluyeNovedadCreada() {
        Long empleadoId = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(
                        ContextoTenant.obtenerEmpresaId()).stream()
                .findFirst()
                .orElseThrow()
                .getId();

        String periodo = "2026-06";
        servicioNovedad.crear(new SolicitudCrearNovedadLiquidacion(
                empleadoId,
                periodo,
                TipoNovedadLiquidacion.HORAS_EXTRAS,
                "Horas extra junio",
                new BigDecimal("8"),
                new BigDecimal("15000.50"),
                LocalDate.of(2026, 6, 15)
        ));

        servicioNovedad.confirmarLote(periodo);

        String csv = new String(servicioNovedad.exportarCsv(periodo), StandardCharsets.UTF_8);
        assertTrue(csv.contains("empleado_id;numero_empleado;nombre;periodo;tipo;descripcion;cantidad;monto;fecha;estado"));
        assertTrue(csv.contains("HORAS_EXTRAS"));
        assertTrue(csv.contains(periodo));
        assertTrue(csv.contains("Horas extra junio"));
        assertTrue(csv.contains("CONFIRMADA"));
    }

    @Test
    void noPermiteEditarNovedadConfirmada() {
        Long empleadoId = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(
                        ContextoTenant.obtenerEmpresaId()).stream()
                .findFirst()
                .orElseThrow()
                .getId();

        String periodo = "2026-07";
        var creada = servicioNovedad.crear(new SolicitudCrearNovedadLiquidacion(
                empleadoId,
                periodo,
                TipoNovedadLiquidacion.BONIFICACION,
                "Bono julio",
                null,
                new BigDecimal("5000"),
                LocalDate.of(2026, 7, 1)
        ));

        servicioNovedad.confirmarLote(periodo);

        assertThrows(ExcepcionNegocio.class, () -> servicioNovedad.actualizar(
                creada.id(),
                new SolicitudActualizarNovedadLiquidacion(
                        null, "Intento de edición", null, null, null
                )
        ));
    }
}
