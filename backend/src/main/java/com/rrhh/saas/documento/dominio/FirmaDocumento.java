package com.rrhh.saas.documento.dominio;

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
@Table(name = "firma_documento")
public class FirmaDocumento extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionDocumento asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;

    @Column(name = "hash_firma", nullable = false)
    private String hashFirma;

    @Column(name = "url_archivo_firmado")
    private String urlArchivoFirmado;

    @Column(name = "direccion_ip")
    private String direccionIp;

    private String dispositivo;

    public AsignacionDocumento getAsignacion() {
        return asignacion;
    }

    public void setAsignacion(AsignacionDocumento asignacion) {
        this.asignacion = asignacion;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    public String getHashFirma() {
        return hashFirma;
    }

    public void setHashFirma(String hashFirma) {
        this.hashFirma = hashFirma;
    }

    public String getUrlArchivoFirmado() {
        return urlArchivoFirmado;
    }

    public void setUrlArchivoFirmado(String urlArchivoFirmado) {
        this.urlArchivoFirmado = urlArchivoFirmado;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }
}
