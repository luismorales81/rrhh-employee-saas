package com.rrhh.saas.recibo.dominio;



import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;

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



import java.math.BigDecimal;

import java.time.LocalDateTime;



@Entity

@Table(name = "recibo")

public class Recibo extends EntidadAuditable {



    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "empresa_id", nullable = false)

    private Empresa empresa;



    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "empleado_id", nullable = false)

    private Empleado empleado;



    @Column(nullable = false, length = 20)

    private String periodo;



    @Column(nullable = false)

    private String titulo;



    @Column(name = "url_archivo", length = 500)

    private String urlArchivo;



    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "archivo_id")

    private ArchivoAlmacenado archivo;



    @Column(name = "hash_pdf", length = 64)

    private String hashPdf;



    @Column(name = "monto_neto", precision = 15, scale = 2)

    private BigDecimal montoNeto;



    @Column(name = "monto_bruto", precision = 15, scale = 2)

    private BigDecimal montoBruto;



    @Column(name = "publicado_por_id")

    private Long publicadoPorId;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 50)

    private EstadoRecibo estado = EstadoRecibo.PENDIENTE;



    @Column(name = "fecha_publicacion", nullable = false)

    private LocalDateTime fechaPublicacion = LocalDateTime.now();



    @Column(name = "visto_por_empleado_en")

    private LocalDateTime vistoPorEmpleadoEn;



    @Column(name = "fecha_firma")

    private LocalDateTime fechaFirma;

    @Column(name = "requiere_republicacion", nullable = false)

    private boolean requiereRepublicacion = false;

    @Column(nullable = false)
    private int version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_anterior_id")
    private Recibo reciboAnterior;

    @Column(nullable = false)
    private boolean vigente = true;

    @Column(name = "notificado_en")
    private LocalDateTime notificadoEn;

    @Column(name = "canal_notificacion", length = 50)
    private String canalNotificacion;

    @Column(name = "motivo_republicacion", length = 1000)
    private String motivoRepublicacion;

    public Empresa getEmpresa() {

        return empresa;

    }



    public void setEmpresa(Empresa empresa) {

        this.empresa = empresa;

    }



    public Empleado getEmpleado() {

        return empleado;

    }



    public void setEmpleado(Empleado empleado) {

        this.empleado = empleado;

    }



    public String getPeriodo() {

        return periodo;

    }



    public void setPeriodo(String periodo) {

        this.periodo = periodo;

    }



    public String getTitulo() {

        return titulo;

    }



    public void setTitulo(String titulo) {

        this.titulo = titulo;

    }



    public String getUrlArchivo() {

        return urlArchivo;

    }



    public void setUrlArchivo(String urlArchivo) {

        this.urlArchivo = urlArchivo;

    }



    public ArchivoAlmacenado getArchivo() {

        return archivo;

    }



    public void setArchivo(ArchivoAlmacenado archivo) {

        this.archivo = archivo;

    }



    public String getHashPdf() {

        return hashPdf;

    }



    public void setHashPdf(String hashPdf) {

        this.hashPdf = hashPdf;

    }



    public BigDecimal getMontoNeto() {

        return montoNeto;

    }



    public void setMontoNeto(BigDecimal montoNeto) {

        this.montoNeto = montoNeto;

    }



    public BigDecimal getMontoBruto() {

        return montoBruto;

    }



    public void setMontoBruto(BigDecimal montoBruto) {

        this.montoBruto = montoBruto;

    }



    public Long getPublicadoPorId() {

        return publicadoPorId;

    }



    public void setPublicadoPorId(Long publicadoPorId) {

        this.publicadoPorId = publicadoPorId;

    }



    public EstadoRecibo getEstado() {

        return estado;

    }



    public void setEstado(EstadoRecibo estado) {

        this.estado = estado;

    }



    public LocalDateTime getFechaPublicacion() {

        return fechaPublicacion;

    }



    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {

        this.fechaPublicacion = fechaPublicacion;

    }



    public LocalDateTime getVistoPorEmpleadoEn() {

        return vistoPorEmpleadoEn;

    }



    public void setVistoPorEmpleadoEn(LocalDateTime vistoPorEmpleadoEn) {

        this.vistoPorEmpleadoEn = vistoPorEmpleadoEn;

    }



    public LocalDateTime getFechaFirma() {

        return fechaFirma;

    }



    public void setFechaFirma(LocalDateTime fechaFirma) {

        this.fechaFirma = fechaFirma;

    }

    public boolean isRequiereRepublicacion() {

        return requiereRepublicacion;

    }

    public void setRequiereRepublicacion(boolean requiereRepublicacion) {

        this.requiereRepublicacion = requiereRepublicacion;

    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Recibo getReciboAnterior() {
        return reciboAnterior;
    }

    public void setReciboAnterior(Recibo reciboAnterior) {
        this.reciboAnterior = reciboAnterior;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public LocalDateTime getNotificadoEn() {
        return notificadoEn;
    }

    public void setNotificadoEn(LocalDateTime notificadoEn) {
        this.notificadoEn = notificadoEn;
    }

    public String getCanalNotificacion() {
        return canalNotificacion;
    }

    public void setCanalNotificacion(String canalNotificacion) {
        this.canalNotificacion = canalNotificacion;
    }

    public String getMotivoRepublicacion() {
        return motivoRepublicacion;
    }

    public void setMotivoRepublicacion(String motivoRepublicacion) {
        this.motivoRepublicacion = motivoRepublicacion;
    }

}


