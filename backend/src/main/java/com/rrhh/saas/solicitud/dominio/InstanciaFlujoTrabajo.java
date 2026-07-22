package com.rrhh.saas.solicitud.dominio;

import com.rrhh.saas.nucleo.dominio.Empresa;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "instancia_flujo_trabajo")
public class InstanciaFlujoTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flujo_trabajo_id", nullable = false)
    private FlujoTrabajo flujoTrabajo;

    @Column(name = "paso_actual_orden", nullable = false)
    private int pasoActualOrden = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobador_actual_id")
    private Usuario aprobadorActual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInstanciaFlujo estado = EstadoInstanciaFlujo.EN_PROGRESO;

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

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public FlujoTrabajo getFlujoTrabajo() {
        return flujoTrabajo;
    }

    public void setFlujoTrabajo(FlujoTrabajo flujoTrabajo) {
        this.flujoTrabajo = flujoTrabajo;
    }

    public int getPasoActualOrden() {
        return pasoActualOrden;
    }

    public void setPasoActualOrden(int pasoActualOrden) {
        this.pasoActualOrden = pasoActualOrden;
    }

    public Usuario getAprobadorActual() {
        return aprobadorActual;
    }

    public void setAprobadorActual(Usuario aprobadorActual) {
        this.aprobadorActual = aprobadorActual;
    }

    public EstadoInstanciaFlujo getEstado() {
        return estado;
    }

    public void setEstado(EstadoInstanciaFlujo estado) {
        this.estado = estado;
    }
}
