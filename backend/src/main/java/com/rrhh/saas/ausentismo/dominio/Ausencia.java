package com.rrhh.saas.ausentismo.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "ausencia")
public class Ausencia extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_ausencia_id", nullable = false)
    private TipoAusencia tipoAusencia;

    @Column(nullable = false)
    private LocalDate fecha;

    private String motivo;

    @Column(nullable = false)
    private boolean justificada = false;

    @Column(name = "url_certificado")
    private String urlCertificado;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public TipoAusencia getTipoAusencia() {
        return tipoAusencia;
    }

    public void setTipoAusencia(TipoAusencia tipoAusencia) {
        this.tipoAusencia = tipoAusencia;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isJustificada() {
        return justificada;
    }

    public void setJustificada(boolean justificada) {
        this.justificada = justificada;
    }

    public String getUrlCertificado() {
        return urlCertificado;
    }

    public void setUrlCertificado(String urlCertificado) {
        this.urlCertificado = urlCertificado;
    }
}
