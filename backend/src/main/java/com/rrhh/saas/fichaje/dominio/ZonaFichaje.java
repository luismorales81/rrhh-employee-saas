package com.rrhh.saas.fichaje.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "zona_fichaje")
public class ZonaFichaje extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coordenadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcanceZonaFichaje alcance = AlcanceZonaFichaje.GLOBAL;

    @Column(nullable = false)
    private boolean activa = true;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public AlcanceZonaFichaje getAlcance() {
        return alcance;
    }

    public void setAlcance(AlcanceZonaFichaje alcance) {
        this.alcance = alcance;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
