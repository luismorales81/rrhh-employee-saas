package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;
import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Funciones puras para evaluar alertas de auditoría médica sobre certificados.
 */
public final class EvaluadorAlertaCertificado {

    private static final Set<String> DIAGNOSTICOS_LEVES = Set.of(
            "resfrio", "gripe", "cefalea", "dolor de cabeza",
            "lumbalgia", "gastritis", "faringitis", "amigdalitis",
            "conjuntivitis", "alergia", "migrana", "contractura",
            "gastroenteritis", "diarrea", "vomitos", "catarro"
    );

    private EvaluadorAlertaCertificado() {
    }

    public static AlertaAuditoria evaluarAlertaCertificado(
            DatosExtraidosCertificado datos,
            ConfiguracionAuditoriaMedica config,
            List<Certificado> historialEmpleadoUltimoPeriodo) {

        if (datos == null) {
            return AlertaAuditoria.aclaracionRequerida("No hay datos extraídos del certificado");
        }

        if (tieneDatosCriticosIncompletos(datos)) {
            return AlertaAuditoria.aclaracionRequerida(
                    "Datos críticos incompletos: diagnóstico, días de reposo o fecha de emisión");
        }

        Float confianza = datos.getConfianzaExtraccion();
        if (confianza != null && confianza < ConfiguracionAuditoriaMedica.UMBRAL_CONFIANZA_BAJA) {
            return AlertaAuditoria.revisionPrioritaria(
                    "Confianza de extracción baja (" + confianza + ")");
        }

        ConfiguracionAuditoriaMedica configEfectiva = config != null ? config : new ConfiguracionAuditoriaMedica();

        if (esDiagnosticoLeveConReposoProlongado(datos, configEfectiva)) {
            return AlertaAuditoria.conAlerta(
                    "Diagnóstico leve con reposo superior a " + configEfectiva.getDiasReposoAlertaLeve() + " días");
        }

        if (excedeLicenciasCortasEnPeriodo(historialEmpleadoUltimoPeriodo, configEfectiva)) {
            return AlertaAuditoria.conAlerta(
                    "Más de " + configEfectiva.getMaxLicenciasCortasPeriodo()
                            + " licencias cortas en los últimos " + configEfectiva.getPeriodoEvaluacionDias() + " días");
        }

        return AlertaAuditoria.sinAlerta();
    }

    /**
     * Adaptador hacia {@link ResultadoAlertaCertificado} usado por el servicio de dominio.
     */
    public static ResultadoAlertaCertificado evaluar(
            DatosExtraidosCertificado datos,
            ConfiguracionAuditoriaMedica config,
            List<Certificado> historialEmpleadoUltimoPeriodo) {

        AlertaAuditoria alerta = evaluarAlertaCertificado(datos, config, historialEmpleadoUltimoPeriodo);

        if (alerta.datosIncompletos()) {
            return ResultadoAlertaCertificado.aclaracion(alerta.motivo());
        }
        if (alerta.requiereRevisionPrioritaria()) {
            return new ResultadoAlertaCertificado(
                    alerta.disparaAlerta(), false, true, List.of(alerta.motivo()));
        }
        if (alerta.disparaAlerta()) {
            return new ResultadoAlertaCertificado(true, false, false, List.of(alerta.motivo()));
        }
        return ResultadoAlertaCertificado.sinAlertas();
    }

    private static boolean tieneDatosCriticosIncompletos(DatosExtraidosCertificado datos) {
        Certificado certificado = datos.getCertificado();
        boolean esAusentismo = certificado != null
                && certificado.getTipoExamen() == TipoExamenCertificado.AUSENTISMO;

        if (esAusentismo && (datos.getDiagnostico() == null || datos.getDiagnostico().isBlank())) {
            return true;
        }
        return datos.getReposoDias() == null || datos.getFechaEmision() == null;
    }

    private static boolean esDiagnosticoLeveConReposoProlongado(
            DatosExtraidosCertificado datos, ConfiguracionAuditoriaMedica config) {

        if (datos.getDiagnostico() == null || datos.getReposoDias() == null) {
            return false;
        }
        if (datos.getReposoDias() <= config.getDiasReposoAlertaLeve()) {
            return false;
        }
        String normalizado = normalizar(datos.getDiagnostico());
        return DIAGNOSTICOS_LEVES.stream().anyMatch(normalizado::contains);
    }

    private static String normalizar(String texto) {
        String lower = texto.toLowerCase(Locale.ROOT);
        return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private static boolean excedeLicenciasCortasEnPeriodo(
            List<Certificado> historial, ConfiguracionAuditoriaMedica config) {

        if (historial == null || historial.isEmpty()) {
            return false;
        }
        int umbralCorta = config.getDiasReposoAlertaLeve();
        long licenciasCortas = historial.stream()
                .filter(c -> c.getEstado() == EstadoCertificado.VALIDADO
                        || c.getEstado() == EstadoCertificado.EN_REVISION
                        || c.getEstado() == EstadoCertificado.EXTRAIDO)
                .filter(c -> c.getDatosExtraidos() != null)
                .filter(c -> {
                    Integer reposo = c.getDatosExtraidos().getReposoDias();
                    return reposo != null && reposo > 0 && reposo <= umbralCorta;
                })
                .count();
        return licenciasCortas > config.getMaxLicenciasCortasPeriodo();
    }
}
