package com.rrhh.saas.documento.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento")
public class Documento extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDocumento categoria;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(nullable = false)
    private String version = "1.0";

    @Column(name = "url_archivo", nullable = false)
    private String urlArchivo;

    @Column(name = "hash_archivo")
    private String hashArchivo;

    @Column(name = "requiere_firma", nullable = false)
    private boolean requiereFirma = false;

    @Column(nullable = false)
    private boolean activo = true;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public CategoriaDocumento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaDocumento categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public void setUrlArchivo(String urlArchivo) {
        this.urlArchivo = urlArchivo;
    }

    public String getHashArchivo() {
        return hashArchivo;
    }

    public void setHashArchivo(String hashArchivo) {
        this.hashArchivo = hashArchivo;
    }

    public boolean isRequiereFirma() {
        return requiereFirma;
    }

    public void setRequiereFirma(boolean requiereFirma) {
        this.requiereFirma = requiereFirma;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
