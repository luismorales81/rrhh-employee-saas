package com.rrhh.saas.vacacion.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rango_politica_vacaciones")
public class RangoPoliticaVacaciones extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "politica_id", nullable = false)
    private PoliticaVacaciones politica;

    @Column(name = "anios_minimos", nullable = false)
    private int aniosMinimos;

    @Column(name = "anios_maximos")
    private Integer aniosMaximos;

    @Column(name = "dias_otorgados", nullable = false)
    private int diasOtorgados;

    public PoliticaVacaciones getPolitica() {
        return politica;
    }

    public void setPolitica(PoliticaVacaciones politica) {
        this.politica = politica;
    }

    public int getAniosMinimos() {
        return aniosMinimos;
    }

    public void setAniosMinimos(int aniosMinimos) {
        this.aniosMinimos = aniosMinimos;
    }

    public Integer getAniosMaximos() {
        return aniosMaximos;
    }

    public void setAniosMaximos(Integer aniosMaximos) {
        this.aniosMaximos = aniosMaximos;
    }

    public int getDiasOtorgados() {
        return diasOtorgados;
    }

    public void setDiasOtorgados(int diasOtorgados) {
        this.diasOtorgados = diasOtorgados;
    }

    public boolean aplicaParaAnios(long aniosAntiguedad) {
        if (aniosAntiguedad < aniosMinimos) {
            return false;
        }
        return aniosMaximos == null || aniosAntiguedad <= aniosMaximos;
    }
}
