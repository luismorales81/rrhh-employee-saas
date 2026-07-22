package com.rrhh.saas.fichaje.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.organizacion.dominio.Area;
import com.rrhh.saas.organizacion.dominio.Gerencia;
import com.rrhh.saas.organizacion.dominio.Sector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignacion_zona_fichaje")
public class AsignacionZonaFichaje extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_fichaje_id", nullable = false)
    private ZonaFichaje zonaFichaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerencia_id")
    private Gerencia gerencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAsignacionZonaFichaje tipo = TipoAsignacionZonaFichaje.INCLUYE;

    public ZonaFichaje getZonaFichaje() {
        return zonaFichaje;
    }

    public void setZonaFichaje(ZonaFichaje zonaFichaje) {
        this.zonaFichaje = zonaFichaje;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Gerencia getGerencia() {
        return gerencia;
    }

    public void setGerencia(Gerencia gerencia) {
        this.gerencia = gerencia;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public TipoAsignacionZonaFichaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoAsignacionZonaFichaje tipo) {
        this.tipo = tipo;
    }
}
