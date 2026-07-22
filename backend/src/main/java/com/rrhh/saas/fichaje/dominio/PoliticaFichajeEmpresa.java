package com.rrhh.saas.fichaje.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "politica_fichaje_empresa")
public class PoliticaFichajeEmpresa extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "texto_transparencia", nullable = false, columnDefinition = "TEXT")
    private String textoTransparencia;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "permitir_fichaje_sin_gps", nullable = false)
    private boolean permitirFichajeSinGps = false;

    @Column(name = "precision_maxima_metros", nullable = false)
    private int precisionMaximaMetros = 100;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getTextoTransparencia() {
        return textoTransparencia;
    }

    public void setTextoTransparencia(String textoTransparencia) {
        this.textoTransparencia = textoTransparencia;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isPermitirFichajeSinGps() {
        return permitirFichajeSinGps;
    }

    public void setPermitirFichajeSinGps(boolean permitirFichajeSinGps) {
        this.permitirFichajeSinGps = permitirFichajeSinGps;
    }

    public int getPrecisionMaximaMetros() {
        return precisionMaximaMetros;
    }

    public void setPrecisionMaximaMetros(int precisionMaximaMetros) {
        this.precisionMaximaMetros = precisionMaximaMetros;
    }
}
