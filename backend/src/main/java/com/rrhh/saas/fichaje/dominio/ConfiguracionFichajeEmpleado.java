package com.rrhh.saas.fichaje.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_fichaje_empleado")
public class ConfiguracionFichajeEmpleado extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false, unique = true)
    private Empleado empleado;

    @Column(name = "fichaje_movil_habilitado", nullable = false)
    private boolean fichajeMovilHabilitado = false;

    @Column(name = "restringir_solo_zonas_asignadas", nullable = false)
    private boolean restringirSoloZonasAsignadas = false;

    @Column(name = "consentimiento_aceptado_at")
    private LocalDateTime consentimientoAceptadoAt;

    @Column(name = "version_politica_aceptada")
    private Integer versionPoliticaAceptada;

    @Column(name = "dispositivo_registrado_id")
    private String dispositivoRegistradoId;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public boolean isFichajeMovilHabilitado() {
        return fichajeMovilHabilitado;
    }

    public void setFichajeMovilHabilitado(boolean fichajeMovilHabilitado) {
        this.fichajeMovilHabilitado = fichajeMovilHabilitado;
    }

    public boolean isRestringirSoloZonasAsignadas() {
        return restringirSoloZonasAsignadas;
    }

    public void setRestringirSoloZonasAsignadas(boolean restringirSoloZonasAsignadas) {
        this.restringirSoloZonasAsignadas = restringirSoloZonasAsignadas;
    }

    public LocalDateTime getConsentimientoAceptadoAt() {
        return consentimientoAceptadoAt;
    }

    public void setConsentimientoAceptadoAt(LocalDateTime consentimientoAceptadoAt) {
        this.consentimientoAceptadoAt = consentimientoAceptadoAt;
    }

    public Integer getVersionPoliticaAceptada() {
        return versionPoliticaAceptada;
    }

    public void setVersionPoliticaAceptada(Integer versionPoliticaAceptada) {
        this.versionPoliticaAceptada = versionPoliticaAceptada;
    }

    public String getDispositivoRegistradoId() {
        return dispositivoRegistradoId;
    }

    public void setDispositivoRegistradoId(String dispositivoRegistradoId) {
        this.dispositivoRegistradoId = dispositivoRegistradoId;
    }
}
