package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dto.ResumenAusentismo;
import com.rrhh.saas.empleado.dominio.Empleado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorAusentismoEmpleadoTest {

    @Test
    void sumaReposoDeCertificadosValidadosEnRango() {
        Certificado c1 = certificado(1L, EstadoCertificado.VALIDADO, LocalDate.of(2026, 6, 10), 5);
        Certificado c2 = certificado(1L, EstadoCertificado.VALIDADO, LocalDate.of(2026, 6, 20), 3);
        Certificado fuera = certificado(1L, EstadoCertificado.VALIDADO, LocalDate.of(2026, 1, 1), 10);
        Certificado noValidado = certificado(1L, EstadoCertificado.EN_REVISION, LocalDate.of(2026, 6, 15), 7);

        ResumenAusentismo resumen = CalculadorAusentismoEmpleado.calcular(
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of(c1, c2, fuera, noValidado));

        assertEquals(2, resumen.totalCertificadosValidados());
        assertEquals(8, resumen.diasReposoTotales());
        assertEquals(2, resumen.certificadosConReposo());
    }

    private Certificado certificado(Long empleadoId, EstadoCertificado estado,
                                    LocalDate emision, int reposo) {
        Empleado empleado = new Empleado();
        empleado.setId(empleadoId);
        Certificado c = new Certificado();
        c.setEmpleado(empleado);
        c.setEstado(estado);
        DatosExtraidosCertificado datos = new DatosExtraidosCertificado();
        datos.setFechaEmision(emision);
        datos.setReposoDias(reposo);
        c.setDatosExtraidos(datos);
        return c;
    }
}
