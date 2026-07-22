package com.rrhh.saas.certificado.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_auditoria_medica")
public class ConfiguracionAuditoriaMedica extends EntidadAuditable {

    public static final int DIAS_REPOSO_ALERTA_LEVE_DEFAULT = 5;
    public static final int MAX_LICENCIAS_CORTAS_DEFAULT = 3;
    public static final int PERIODO_EVALUACION_DIAS_DEFAULT = 90;
    public static final int MAX_INTENTOS_EXTRACCION_DEFAULT = 3;
    public static final float UMBRAL_CONFIANZA_BAJA = 0.6f;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "dias_reposo_alerta_leve", nullable = false)
    private int diasReposoAlertaLeve = DIAS_REPOSO_ALERTA_LEVE_DEFAULT;

    @Column(name = "max_licencias_cortas_periodo", nullable = false)
    private int maxLicenciasCortasPeriodo = MAX_LICENCIAS_CORTAS_DEFAULT;

    @Column(name = "periodo_evaluacion_dias", nullable = false)
    private int periodoEvaluacionDias = PERIODO_EVALUACION_DIAS_DEFAULT;

    @Column(name = "requiere_revision_manual_siempre", nullable = false)
    private boolean requiereRevisionManualSiempre = true;

    @Column(name = "proyecto_gcp", length = 100)
    private String proyectoGcp;

    @Column(name = "region_vertex", length = 50)
    private String regionVertex;

    @Column(name = "modelo_gemini", length = 100)
    private String modeloGemini;

    @Column(name = "credenciales_servicio_cifrada", columnDefinition = "TEXT")
    private String credencialesServicioCifrada;

    @Column(name = "max_intentos_extraccion", nullable = false)
    private int maxIntentosExtraccion = MAX_INTENTOS_EXTRACCION_DEFAULT;

    public boolean tieneIaConfigurada() {
        return proyectoGcp != null && !proyectoGcp.isBlank()
                && regionVertex != null && !regionVertex.isBlank()
                && modeloGemini != null && !modeloGemini.isBlank()
                && credencialesServicioCifrada != null && !credencialesServicioCifrada.isBlank();
    }

    public boolean tieneCredencialesConfiguradas() {
        return credencialesServicioCifrada != null && !credencialesServicioCifrada.isBlank();
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public int getDiasReposoAlertaLeve() {
        return diasReposoAlertaLeve;
    }

    public void setDiasReposoAlertaLeve(int diasReposoAlertaLeve) {
        this.diasReposoAlertaLeve = diasReposoAlertaLeve;
    }

    public int getMaxLicenciasCortasPeriodo() {
        return maxLicenciasCortasPeriodo;
    }

    public void setMaxLicenciasCortasPeriodo(int maxLicenciasCortasPeriodo) {
        this.maxLicenciasCortasPeriodo = maxLicenciasCortasPeriodo;
    }

    public int getPeriodoEvaluacionDias() {
        return periodoEvaluacionDias;
    }

    public void setPeriodoEvaluacionDias(int periodoEvaluacionDias) {
        this.periodoEvaluacionDias = periodoEvaluacionDias;
    }

    public boolean isRequiereRevisionManualSiempre() {
        return requiereRevisionManualSiempre;
    }

    public void setRequiereRevisionManualSiempre(boolean requiereRevisionManualSiempre) {
        this.requiereRevisionManualSiempre = requiereRevisionManualSiempre;
    }

    public String getProyectoGcp() {
        return proyectoGcp;
    }

    public void setProyectoGcp(String proyectoGcp) {
        this.proyectoGcp = proyectoGcp;
    }

    public String getRegionVertex() {
        return regionVertex;
    }

    public void setRegionVertex(String regionVertex) {
        this.regionVertex = regionVertex;
    }

    public String getModeloGemini() {
        return modeloGemini;
    }

    public void setModeloGemini(String modeloGemini) {
        this.modeloGemini = modeloGemini;
    }

    public String getCredencialesServicioCifrada() {
        return credencialesServicioCifrada;
    }

    public void setCredencialesServicioCifrada(String credencialesServicioCifrada) {
        this.credencialesServicioCifrada = credencialesServicioCifrada;
    }

    public int getMaxIntentosExtraccion() {
        return maxIntentosExtraccion;
    }

    public void setMaxIntentosExtraccion(int maxIntentosExtraccion) {
        this.maxIntentosExtraccion = maxIntentosExtraccion;
    }
}
