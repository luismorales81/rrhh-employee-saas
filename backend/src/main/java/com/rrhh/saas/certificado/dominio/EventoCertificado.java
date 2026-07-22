package com.rrhh.saas.certificado.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "evento_certificado")
public class EventoCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificado_id", nullable = false)
    private Long certificadoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoCertificado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 50)
    private EstadoCertificado estadoNuevo;

    @Column(name = "actor_usuario_id", nullable = false)
    private Long actorUsuarioId;

    @Column(name = "direccion_ip", length = 100)
    private String direccionIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "timestamp_utc", nullable = false)
    private Instant timestampUtc = Instant.now();

    @Column(length = 2000)
    private String observaciones;

    public Long getId() {
        return id;
    }

    public Long getCertificadoId() {
        return certificadoId;
    }

    public void setCertificadoId(Long certificadoId) {
        this.certificadoId = certificadoId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public EstadoCertificado getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoCertificado estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoCertificado getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoCertificado estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public Long getActorUsuarioId() {
        return actorUsuarioId;
    }

    public void setActorUsuarioId(Long actorUsuarioId) {
        this.actorUsuarioId = actorUsuarioId;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    public void setTimestampUtc(Instant timestampUtc) {
        this.timestampUtc = timestampUtc;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
