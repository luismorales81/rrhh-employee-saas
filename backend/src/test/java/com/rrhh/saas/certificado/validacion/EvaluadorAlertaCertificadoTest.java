package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;
import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluadorAlertaCertificadoTest {

    private ConfiguracionAuditoriaMedica configPorDefecto() {
        ConfiguracionAuditoriaMedica config = new ConfiguracionAuditoriaMedica();
        config.setDiasReposoAlertaLeve(5);
        config.setMaxLicenciasCortasPeriodo(3);
        config.setPeriodoEvaluacionDias(90);
        return config;
    }

    private DatosExtraidosCertificado datosCompletos(String diagnostico, int reposo, float confianza) {
        Certificado certificado = new Certificado();
        certificado.setTipoExamen(TipoExamenCertificado.AUSENTISMO);

        DatosExtraidosCertificado datos = new DatosExtraidosCertificado();
        datos.setCertificado(certificado);
        datos.setDiagnostico(diagnostico);
        datos.setReposoDias(reposo);
        datos.setFechaEmision(LocalDate.of(2026, 7, 1));
        datos.setConfianzaExtraccion(confianza);
        return datos;
    }

    @Test
    void diagnosticoLeveConReposoProlongadoDisparaAlerta() {
        var datos = datosCompletos("Resfrío común", 10, 0.9f);
        var resultado = EvaluadorAlertaCertificado.evaluarAlertaCertificado(
                datos, configPorDefecto(), List.of());
        assertTrue(resultado.disparaAlerta());
    }

    @Test
    void excedeLicenciasCortasEnPeriodoDisparaAlerta() {
        var datos = datosCompletos("Lesión menor", 3, 0.9f);
        var historial = List.of(
                certificadoHistorial(2), certificadoHistorial(3), certificadoHistorial(4),
                certificadoHistorial(1));
        var resultado = EvaluadorAlertaCertificado.evaluarAlertaCertificado(
                datos, configPorDefecto(), historial);
        assertTrue(resultado.disparaAlerta());
    }

    @Test
    void sinAlertasPasaARevisionEstandar() {
        var datos = datosCompletos("Fractura de tibia", 15, 0.95f);
        var resultado = EvaluadorAlertaCertificado.evaluarAlertaCertificado(
                datos, configPorDefecto(), List.of());
        assertFalse(resultado.disparaAlerta());
        assertFalse(resultado.datosIncompletos());
        assertFalse(resultado.requiereRevisionPrioritaria());
    }

    @Test
    void confianzaBajaFuerzaRevisionPrioritaria() {
        var datos = datosCompletos("Gripe", 2, 0.4f);
        var resultado = EvaluadorAlertaCertificado.evaluarAlertaCertificado(
                datos, configPorDefecto(), List.of());
        assertTrue(resultado.requiereRevisionPrioritaria());
    }

    @Test
    void datosIncompletosRequierenAclaracion() {
        DatosExtraidosCertificado datos = new DatosExtraidosCertificado();
        datos.setDiagnostico("Algo");
        datos.setReposoDias(null);
        datos.setFechaEmision(LocalDate.of(2026, 7, 1));

        var resultado = EvaluadorAlertaCertificado.evaluarAlertaCertificado(
                datos, configPorDefecto(), List.of());
        assertTrue(resultado.datosIncompletos());
    }

    private Certificado certificadoHistorial(int reposoDias) {
        Certificado certificado = new Certificado();
        certificado.setEstado(EstadoCertificado.VALIDADO);
        DatosExtraidosCertificado datos = new DatosExtraidosCertificado();
        datos.setReposoDias(reposoDias);
        certificado.setDatosExtraidos(datos);
        return certificado;
    }
}
