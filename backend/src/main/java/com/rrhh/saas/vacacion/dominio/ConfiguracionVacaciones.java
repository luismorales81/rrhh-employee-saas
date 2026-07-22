package com.rrhh.saas.vacacion.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_vacaciones")
public class ConfiguracionVacaciones extends EntidadAuditable {

    public static final int PISO_LEGAL_DIAS_FRACCION = 7;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "dias_minimos_fraccion", nullable = false)
    private int diasMinimosFraccion = PISO_LEGAL_DIAS_FRACCION;

    @Column(name = "anticipacion_minima_dias", nullable = false)
    private int anticipacionMinimaDias = 45;

    @Column(name = "permite_solicitud_sin_saldo", nullable = false)
    private boolean permiteSolicitudSinSaldo = false;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public int getDiasMinimosFraccion() {
        return diasMinimosFraccion;
    }

    public void setDiasMinimosFraccion(int diasMinimosFraccion) {
        this.diasMinimosFraccion = diasMinimosFraccion;
    }

    public int getAnticipacionMinimaDias() {
        return anticipacionMinimaDias;
    }

    public void setAnticipacionMinimaDias(int anticipacionMinimaDias) {
        this.anticipacionMinimaDias = anticipacionMinimaDias;
    }

    public boolean isPermiteSolicitudSinSaldo() {
        return permiteSolicitudSinSaldo;
    }

    public void setPermiteSolicitudSinSaldo(boolean permiteSolicitudSinSaldo) {
        this.permiteSolicitudSinSaldo = permiteSolicitudSinSaldo;
    }

    /** Piso legal Ley 27.802: ningún CCT puede bajar de 7 días por fracción. */
    public int diasMinimosFraccionEfectivos() {
        return Math.max(PISO_LEGAL_DIAS_FRACCION, diasMinimosFraccion);
    }
}
