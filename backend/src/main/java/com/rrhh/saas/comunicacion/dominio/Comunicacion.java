package com.rrhh.saas.comunicacion.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "comunicacion")
public class Comunicacion extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destinatario", nullable = false)
    private TipoDestinatarioComunicacion tipoDestinatario;

    @Column(name = "referencia_destinatario_id")
    private Long referenciaDestinatarioId;

    @Column(name = "requiere_confirmacion", nullable = false)
    private boolean requiereConfirmacion = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoComunicacion estado = EstadoComunicacion.PUBLICADA;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_publicacion_programada")
    private LocalDateTime fechaPublicacionProgramada;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public TipoDestinatarioComunicacion getTipoDestinatario() {
        return tipoDestinatario;
    }

    public void setTipoDestinatario(TipoDestinatarioComunicacion tipoDestinatario) {
        this.tipoDestinatario = tipoDestinatario;
    }

    public Long getReferenciaDestinatarioId() {
        return referenciaDestinatarioId;
    }

    public void setReferenciaDestinatarioId(Long referenciaDestinatarioId) {
        this.referenciaDestinatarioId = referenciaDestinatarioId;
    }

    public boolean isRequiereConfirmacion() {
        return requiereConfirmacion;
    }

    public void setRequiereConfirmacion(boolean requiereConfirmacion) {
        this.requiereConfirmacion = requiereConfirmacion;
    }

    public EstadoComunicacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoComunicacion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaPublicacionProgramada() {
        return fechaPublicacionProgramada;
    }

    public void setFechaPublicacionProgramada(LocalDateTime fechaPublicacionProgramada) {
        this.fechaPublicacionProgramada = fechaPublicacionProgramada;
    }
}
