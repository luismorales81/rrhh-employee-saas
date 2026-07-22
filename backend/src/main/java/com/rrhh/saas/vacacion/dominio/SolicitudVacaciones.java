package com.rrhh.saas.vacacion.dominio;

import com.rrhh.saas.solicitud.dominio.Solicitud;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_vacaciones")
public class SolicitudVacaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saldo_vacaciones_id")
    private SaldoVacaciones saldoVacaciones;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "dias_solicitados", nullable = false)
    private BigDecimal diasSolicitados;

    @Column(name = "fecha_inicio_contrapropuesta")
    private LocalDate fechaInicioContrapropuesta;

    @Column(name = "fecha_fin_contrapropuesta")
    private LocalDate fechaFinContrapropuesta;

    @Column(name = "motivo_contrapropuesta", length = 2000)
    private String motivoContrapropuesta;

    @Column(name = "fecha_inicio_aprobada")
    private LocalDate fechaInicioAprobada;

    @Column(name = "fecha_fin_aprobada")
    private LocalDate fechaFinAprobada;

    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

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

    public SaldoVacaciones getSaldoVacaciones() {
        return saldoVacaciones;
    }

    public void setSaldoVacaciones(SaldoVacaciones saldoVacaciones) {
        this.saldoVacaciones = saldoVacaciones;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getDiasSolicitados() {
        return diasSolicitados;
    }

    public void setDiasSolicitados(BigDecimal diasSolicitados) {
        this.diasSolicitados = diasSolicitados;
    }

    public LocalDate getFechaInicioContrapropuesta() {
        return fechaInicioContrapropuesta;
    }

    public void setFechaInicioContrapropuesta(LocalDate fechaInicioContrapropuesta) {
        this.fechaInicioContrapropuesta = fechaInicioContrapropuesta;
    }

    public LocalDate getFechaFinContrapropuesta() {
        return fechaFinContrapropuesta;
    }

    public void setFechaFinContrapropuesta(LocalDate fechaFinContrapropuesta) {
        this.fechaFinContrapropuesta = fechaFinContrapropuesta;
    }

    public String getMotivoContrapropuesta() {
        return motivoContrapropuesta;
    }

    public void setMotivoContrapropuesta(String motivoContrapropuesta) {
        this.motivoContrapropuesta = motivoContrapropuesta;
    }

    public LocalDate getFechaInicioAprobada() {
        return fechaInicioAprobada;
    }

    public void setFechaInicioAprobada(LocalDate fechaInicioAprobada) {
        this.fechaInicioAprobada = fechaInicioAprobada;
    }

    public LocalDate getFechaFinAprobada() {
        return fechaFinAprobada;
    }

    public void setFechaFinAprobada(LocalDate fechaFinAprobada) {
        this.fechaFinAprobada = fechaFinAprobada;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDate fechaInicioEfectiva() {
        if (fechaInicioAprobada != null) {
            return fechaInicioAprobada;
        }
        if (fechaInicioContrapropuesta != null && solicitud != null
                && solicitud.getEstado() == com.rrhh.saas.solicitud.dominio.EstadoSolicitud.CAMBIOS_SOLICITADOS) {
            return fechaInicioContrapropuesta;
        }
        return fechaInicio;
    }

    public LocalDate fechaFinEfectiva() {
        if (fechaFinAprobada != null) {
            return fechaFinAprobada;
        }
        if (fechaFinContrapropuesta != null && solicitud != null
                && solicitud.getEstado() == com.rrhh.saas.solicitud.dominio.EstadoSolicitud.CAMBIOS_SOLICITADOS) {
            return fechaFinContrapropuesta;
        }
        return fechaFin;
    }
}
