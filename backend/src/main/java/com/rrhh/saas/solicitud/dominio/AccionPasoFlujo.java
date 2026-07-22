package com.rrhh.saas.solicitud.dominio;

import com.rrhh.saas.nucleo.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "accion_paso_flujo")
public class AccionPasoFlujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_flujo_trabajo_id", nullable = false)
    private InstanciaFlujoTrabajo instanciaFlujoTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paso_flujo_trabajo_id", nullable = false)
    private PasoFlujoTrabajo pasoFlujoTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionPaso accion;

    private String comentarios;

    @Column(name = "fecha_accion", nullable = false)
    private LocalDateTime fechaAccion = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public InstanciaFlujoTrabajo getInstanciaFlujoTrabajo() {
        return instanciaFlujoTrabajo;
    }

    public void setInstanciaFlujoTrabajo(InstanciaFlujoTrabajo instanciaFlujoTrabajo) {
        this.instanciaFlujoTrabajo = instanciaFlujoTrabajo;
    }

    public PasoFlujoTrabajo getPasoFlujoTrabajo() {
        return pasoFlujoTrabajo;
    }

    public void setPasoFlujoTrabajo(PasoFlujoTrabajo pasoFlujoTrabajo) {
        this.pasoFlujoTrabajo = pasoFlujoTrabajo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public AccionPaso getAccion() {
        return accion;
    }

    public void setAccion(AccionPaso accion) {
        this.accion = accion;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDateTime getFechaAccion() {
        return fechaAccion;
    }
}
