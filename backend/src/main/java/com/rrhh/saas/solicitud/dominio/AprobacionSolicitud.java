package com.rrhh.saas.solicitud.dominio;

import com.rrhh.saas.nucleo.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "aprobacion_solicitud")
public class AprobacionSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paso_flujo_trabajo_id")
    private PasoFlujoTrabajo pasoFlujoTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobador_id", nullable = false)
    private Usuario aprobador;

    @Column(nullable = false)
    private String estado;

    private String comentarios;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        this.createdAt = ahora;
        this.updatedAt = ahora;
    }

    @PreUpdate
    protected void alActualizar() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public PasoFlujoTrabajo getPasoFlujoTrabajo() {
        return pasoFlujoTrabajo;
    }

    public void setPasoFlujoTrabajo(PasoFlujoTrabajo pasoFlujoTrabajo) {
        this.pasoFlujoTrabajo = pasoFlujoTrabajo;
    }

    public Usuario getAprobador() {
        return aprobador;
    }

    public void setAprobador(Usuario aprobador) {
        this.aprobador = aprobador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }
}
