package com.rrhh.saas.vacacion.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "saldo_vacaciones")
public class SaldoVacaciones extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private int anio;

    @Column(name = "antiguedad_anios", nullable = false)
    private int antiguedadAnios;

    @Column(name = "dias_totales", nullable = false)
    private BigDecimal diasTotales = BigDecimal.ZERO;

    @Column(name = "dias_tomados", nullable = false)
    private BigDecimal diasTomados = BigDecimal.ZERO;

    @Column(name = "dias_pendientes", nullable = false)
    private BigDecimal diasPendientes = BigDecimal.ZERO;

    @Column(name = "dias_disponibles", nullable = false)
    private BigDecimal diasDisponibles = BigDecimal.ZERO;

    @Column(name = "ajuste_manual", nullable = false)
    private BigDecimal ajusteManual = BigDecimal.ZERO;

    @Column(name = "ajuste_manual_motivo", length = 500)
    private String ajusteManualMotivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "politica_id")
    private PoliticaVacaciones politica;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getAntiguedadAnios() {
        return antiguedadAnios;
    }

    public void setAntiguedadAnios(int antiguedadAnios) {
        this.antiguedadAnios = antiguedadAnios;
    }

    public BigDecimal getDiasTotales() {
        return diasTotales;
    }

    public void setDiasTotales(BigDecimal diasTotales) {
        this.diasTotales = diasTotales;
    }

    public BigDecimal getDiasTomados() {
        return diasTomados;
    }

    public void setDiasTomados(BigDecimal diasTomados) {
        this.diasTomados = diasTomados;
    }

    public BigDecimal getDiasPendientes() {
        return diasPendientes;
    }

    public void setDiasPendientes(BigDecimal diasPendientes) {
        this.diasPendientes = diasPendientes;
    }

    public BigDecimal getDiasDisponibles() {
        return diasDisponibles;
    }

    public void setDiasDisponibles(BigDecimal diasDisponibles) {
        this.diasDisponibles = diasDisponibles;
    }

    public BigDecimal getAjusteManual() {
        return ajusteManual;
    }

    public void setAjusteManual(BigDecimal ajusteManual) {
        this.ajusteManual = ajusteManual;
    }

    public String getAjusteManualMotivo() {
        return ajusteManualMotivo;
    }

    public void setAjusteManualMotivo(String ajusteManualMotivo) {
        this.ajusteManualMotivo = ajusteManualMotivo;
    }

    public PoliticaVacaciones getPolitica() {
        return politica;
    }

    public void setPolitica(PoliticaVacaciones politica) {
        this.politica = politica;
    }

    public void recalcularDisponibles() {
        this.diasDisponibles = diasTotales
                .subtract(diasTomados)
                .subtract(diasPendientes)
                .add(ajusteManual);
    }
}
