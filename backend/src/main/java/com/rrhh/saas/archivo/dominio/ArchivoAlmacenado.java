package com.rrhh.saas.archivo.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.nucleo.dominio.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "archivo_almacenado")
public class ArchivoAlmacenado extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(name = "clave_almacenamiento", nullable = false, length = 500)
    private String claveAlmacenamiento;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(nullable = false)
    private long tamano;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getClaveAlmacenamiento() {
        return claveAlmacenamiento;
    }

    public void setClaveAlmacenamiento(String claveAlmacenamiento) {
        this.claveAlmacenamiento = claveAlmacenamiento;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public long getTamano() {
        return tamano;
    }

    public void setTamano(long tamano) {
        this.tamano = tamano;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
    }
}
