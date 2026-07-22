package com.rrhh.saas.certificado.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
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
@Table(name = "certificado")
public class Certificado extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_examen", nullable = false, length = 50)
    private TipoExamenCertificado tipoExamen;

    @Column(name = "archivo_id")
    private Long archivoId;

    @Column(name = "archivo_url", nullable = false, length = 500)
    private String archivoUrl;

    @Column(name = "archivo_hash_sha256", nullable = false, length = 64)
    private String archivoHashSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_carga", nullable = false, length = 50)
    private OrigenCargaCertificado origenCarga;

    @Column(name = "cargado_por_usuario_id", nullable = false)
    private Long cargadoPorUsuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoCertificado estado = EstadoCertificado.CARGADO;

    @Column(name = "intentos_extraccion", nullable = false)
    private int intentosExtraccion = 0;

    @Column(name = "ultimo_error_extraccion", length = 500)
    private String ultimoErrorExtraccion;

    @Column(name = "ultimo_intento_extraccion")
    private LocalDateTime ultimoIntentoExtraccion;

    /** Campo transitorio para consultas con JOIN FETCH; no persistido. */
    private transient DatosExtraidosCertificado datosExtraidos;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public TipoExamenCertificado getTipoExamen() {
        return tipoExamen;
    }

    public void setTipoExamen(TipoExamenCertificado tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public Long getArchivoId() {
        return archivoId;
    }

    public void setArchivoId(Long archivoId) {
        this.archivoId = archivoId;
    }

    public String getArchivoUrl() {
        return archivoUrl;
    }

    public void setArchivoUrl(String archivoUrl) {
        this.archivoUrl = archivoUrl;
    }

    public String getArchivoHashSha256() {
        return archivoHashSha256;
    }

    public void setArchivoHashSha256(String archivoHashSha256) {
        this.archivoHashSha256 = archivoHashSha256;
    }

    public OrigenCargaCertificado getOrigenCarga() {
        return origenCarga;
    }

    public void setOrigenCarga(OrigenCargaCertificado origenCarga) {
        this.origenCarga = origenCarga;
    }

    public Long getCargadoPorUsuarioId() {
        return cargadoPorUsuarioId;
    }

    public void setCargadoPorUsuarioId(Long cargadoPorUsuarioId) {
        this.cargadoPorUsuarioId = cargadoPorUsuarioId;
    }

    public EstadoCertificado getEstado() {
        return estado;
    }

    public void setEstado(EstadoCertificado estado) {
        this.estado = estado;
    }

    public int getIntentosExtraccion() {
        return intentosExtraccion;
    }

    public void setIntentosExtraccion(int intentosExtraccion) {
        this.intentosExtraccion = intentosExtraccion;
    }

    public String getUltimoErrorExtraccion() {
        return ultimoErrorExtraccion;
    }

    public void setUltimoErrorExtraccion(String ultimoErrorExtraccion) {
        this.ultimoErrorExtraccion = ultimoErrorExtraccion;
    }

    public LocalDateTime getUltimoIntentoExtraccion() {
        return ultimoIntentoExtraccion;
    }

    public void setUltimoIntentoExtraccion(LocalDateTime ultimoIntentoExtraccion) {
        this.ultimoIntentoExtraccion = ultimoIntentoExtraccion;
    }

    public DatosExtraidosCertificado getDatosExtraidos() {
        return datosExtraidos;
    }

    public void setDatosExtraidos(DatosExtraidosCertificado datosExtraidos) {
        this.datosExtraidos = datosExtraidos;
    }
}
