package com.rrhh.saas.recibo.dominio;

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
@Table(name = "evento_firma_recibo")
public class EventoFirmaRecibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recibo_id", nullable = false)
    private Long reciboId;

    @Column(name = "empleado_id", nullable = false)
    private Long empleadoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoEventoFirmaRecibo tipo;

    @Column(name = "timestamp_evento", nullable = false)
    private LocalDateTime timestampEvento = LocalDateTime.now();

    @Column(name = "direccion_ip", length = 100)
    private String direccionIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "sistema_operativo", length = 100)
    private String sistemaOperativo;

    @Column(name = "hash_pdf_en_firma", nullable = false, length = 64)
    private String hashPdfEnFirma;

    @Column(length = 2000)
    private String comentario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReciboId() {
        return reciboId;
    }

    public void setReciboId(Long reciboId) {
        this.reciboId = reciboId;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
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

    public TipoEventoFirmaRecibo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEventoFirmaRecibo tipo) {
        this.tipo = tipo;
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

    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    public void setSistemaOperativo(String sistemaOperativo) {
        this.sistemaOperativo = sistemaOperativo;
    }

    public String getHashPdfEnFirma() {
        return hashPdfEnFirma;
    }

    public void setHashPdfEnFirma(String hashPdfEnFirma) {
        this.hashPdfEnFirma = hashPdfEnFirma;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
