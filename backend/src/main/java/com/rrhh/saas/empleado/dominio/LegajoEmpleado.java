package com.rrhh.saas.empleado.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "legajo_empleado")
public class LegajoEmpleado extends EntidadAuditable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false, unique = true)
    private Empleado empleado;

    @Column(name = "estado_onboarding", nullable = false, length = 50)
    private String estadoOnboarding = "PENDIENTE";

    @Column(name = "fecha_completado_onboarding")
    private LocalDateTime fechaCompletadoOnboarding;

    @Column(name = "estado_offboarding", nullable = false, length = 50)
    private String estadoOffboarding = "PENDIENTE";

    @Column(name = "fecha_completado_offboarding")
    private LocalDateTime fechaCompletadoOffboarding;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public String getEstadoOnboarding() {
        return estadoOnboarding;
    }

    public void setEstadoOnboarding(String estadoOnboarding) {
        this.estadoOnboarding = estadoOnboarding;
    }

    public LocalDateTime getFechaCompletadoOnboarding() {
        return fechaCompletadoOnboarding;
    }

    public void setFechaCompletadoOnboarding(LocalDateTime fechaCompletadoOnboarding) {
        this.fechaCompletadoOnboarding = fechaCompletadoOnboarding;
    }

    public String getEstadoOffboarding() {
        return estadoOffboarding;
    }

    public void setEstadoOffboarding(String estadoOffboarding) {
        this.estadoOffboarding = estadoOffboarding;
    }

    public LocalDateTime getFechaCompletadoOffboarding() {
        return fechaCompletadoOffboarding;
    }

    public void setFechaCompletadoOffboarding(LocalDateTime fechaCompletadoOffboarding) {
        this.fechaCompletadoOffboarding = fechaCompletadoOffboarding;
    }
}
