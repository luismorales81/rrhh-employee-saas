package com.rrhh.saas.certificado.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "datos_extraidos_certificado")
public class DatosExtraidosCertificado extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificado_id", nullable = false)
    private Certificado certificado;

    @Column(name = "numero_version", nullable = false)
    private int numeroVersion = 1;

    @Column(name = "es_vigente", nullable = false)
    private boolean esVigente = true;

    @Column(length = 2000)
    private String diagnostico;

    @Column(name = "reposo_dias")
    private Integer reposoDias;

    @Column(name = "medico_nombre")
    private String medicoNombre;

    @Column(name = "medico_matricula", length = 100)
    private String medicoMatricula;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "confianza_extraccion")
    private Float confianzaExtraccion;

    @Column(name = "corregido_manualmente", nullable = false)
    private boolean corregidoManualmente = false;

    public Certificado getCertificado() {
        return certificado;
    }

    public void setCertificado(Certificado certificado) {
        this.certificado = certificado;
    }

    public int getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(int numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public boolean isEsVigente() {
        return esVigente;
    }

    public void setEsVigente(boolean esVigente) {
        this.esVigente = esVigente;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Integer getReposoDias() {
        return reposoDias;
    }

    public void setReposoDias(Integer reposoDias) {
        this.reposoDias = reposoDias;
    }

    public String getMedicoNombre() {
        return medicoNombre;
    }

    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }

    public String getMedicoMatricula() {
        return medicoMatricula;
    }

    public void setMedicoMatricula(String medicoMatricula) {
        this.medicoMatricula = medicoMatricula;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Float getConfianzaExtraccion() {
        return confianzaExtraccion;
    }

    public void setConfianzaExtraccion(Float confianzaExtraccion) {
        this.confianzaExtraccion = confianzaExtraccion;
    }

    public boolean isCorregidoManualmente() {
        return corregidoManualmente;
    }

    public void setCorregidoManualmente(boolean corregidoManualmente) {
        this.corregidoManualmente = corregidoManualmente;
    }
}
