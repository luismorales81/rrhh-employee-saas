package com.rrhh.saas.solicitud.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_solicitud")
public class TipoSolicitud extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flujo_trabajo_id")
    private FlujoTrabajo flujoTrabajo;

    @Column(name = "requiere_aprobacion", nullable = false)
    private boolean requiereAprobacion = true;

    @Column(name = "requiere_adjunto", nullable = false)
    private boolean requiereAdjunto = false;

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

    public FlujoTrabajo getFlujoTrabajo() {
        return flujoTrabajo;
    }

    public void setFlujoTrabajo(FlujoTrabajo flujoTrabajo) {
        this.flujoTrabajo = flujoTrabajo;
    }

    public boolean isRequiereAprobacion() {
        return requiereAprobacion;
    }

    public void setRequiereAprobacion(boolean requiereAprobacion) {
        this.requiereAprobacion = requiereAprobacion;
    }

    public boolean isRequiereAdjunto() {
        return requiereAdjunto;
    }

    public void setRequiereAdjunto(boolean requiereAdjunto) {
        this.requiereAdjunto = requiereAdjunto;
    }
}
