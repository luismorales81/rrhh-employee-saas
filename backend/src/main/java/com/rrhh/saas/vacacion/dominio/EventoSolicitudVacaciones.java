package com.rrhh.saas.vacacion.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_solicitud_vacaciones")
public class EventoSolicitudVacaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_vacaciones_id", nullable = false)
    private Long solicitudVacacionesId;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEventoSolicitudVacaciones tipoEvento;

    @Column(name = "estado_anterior", length = 50)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 50)
    private String estadoNuevo;

    @Column(name = "timestamp_evento", nullable = false)
    private LocalDateTime timestampEvento = LocalDateTime.now();

    @Column(name = "direccion_ip", length = 100)
    private String direccionIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 2000)
    private String comentario;

    public Long getId() {
        return id;
    }

    public Long getSolicitudVacacionesId() {
        return solicitudVacacionesId;
    }

    public void setSolicitudVacacionesId(Long solicitudVacacionesId) {
        this.solicitudVacacionesId = solicitudVacacionesId;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoEventoSolicitudVacaciones getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoSolicitudVacaciones tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public LocalDateTime getTimestampEvento() {
        return timestampEvento;
    }

    public void setTimestampEvento(LocalDateTime timestampEvento) {
        this.timestampEvento = timestampEvento;
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

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
