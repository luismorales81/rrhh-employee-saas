package com.rrhh.saas.solicitud.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paso_flujo_trabajo")
public class PasoFlujoTrabajo extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flujo_trabajo_id", nullable = false)
    private FlujoTrabajo flujoTrabajo;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_paso", nullable = false)
    private TipoPaso tipoPaso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public FlujoTrabajo getFlujoTrabajo() {
        return flujoTrabajo;
    }

    public void setFlujoTrabajo(FlujoTrabajo flujoTrabajo) {
        this.flujoTrabajo = flujoTrabajo;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoPaso getTipoPaso() {
        return tipoPaso;
    }

    public void setTipoPaso(TipoPaso tipoPaso) {
        this.tipoPaso = tipoPaso;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
