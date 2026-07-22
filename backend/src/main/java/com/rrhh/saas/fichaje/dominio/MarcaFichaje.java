package com.rrhh.saas.fichaje.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Usuario;
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
@Table(name = "marca_fichaje")
public class MarcaFichaje extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMarcaFichaje tipo;

    @Column(name = "fecha_hora_servidor", nullable = false)
    private LocalDateTime fechaHoraServidor;

    @Column(name = "fecha_hora_dispositivo")
    private LocalDateTime fechaHoraDispositivo;

    private Double latitud;

    private Double longitud;

    @Column(name = "precision_metros")
    private Double precisionMetros;

    @Column(name = "id_dispositivo")
    private String idDispositivo;

    @Column(name = "modelo_dispositivo")
    private String modeloDispositivo;

    @Column(name = "sistema_operativo")
    private String sistemaOperativo;

    @Column(name = "version_app")
    private String versionApp;

    @Column(name = "direccion_ip")
    private String direccionIp;

    @Column(name = "user_agent")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_fichaje_id")
    private ZonaFichaje zonaFichaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMarcaFichaje estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_estado")
    private MotivoEstadoMarcaFichaje motivoEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_usuario_id")
    private Usuario revisadoPorUsuario;

    @Column(name = "revisado_at")
    private LocalDateTime revisadoAt;

    @Column(name = "nota_revision")
    private String notaRevision;

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

    public TipoMarcaFichaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoMarcaFichaje tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaHoraServidor() {
        return fechaHoraServidor;
    }

    public void setFechaHoraServidor(LocalDateTime fechaHoraServidor) {
        this.fechaHoraServidor = fechaHoraServidor;
    }

    public LocalDateTime getFechaHoraDispositivo() {
        return fechaHoraDispositivo;
    }

    public void setFechaHoraDispositivo(LocalDateTime fechaHoraDispositivo) {
        this.fechaHoraDispositivo = fechaHoraDispositivo;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getPrecisionMetros() {
        return precisionMetros;
    }

    public void setPrecisionMetros(Double precisionMetros) {
        this.precisionMetros = precisionMetros;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public String getModeloDispositivo() {
        return modeloDispositivo;
    }

    public void setModeloDispositivo(String modeloDispositivo) {
        this.modeloDispositivo = modeloDispositivo;
    }

    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    public void setSistemaOperativo(String sistemaOperativo) {
        this.sistemaOperativo = sistemaOperativo;
    }

    public String getVersionApp() {
        return versionApp;
    }

    public void setVersionApp(String versionApp) {
        this.versionApp = versionApp;
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

    public ZonaFichaje getZonaFichaje() {
        return zonaFichaje;
    }

    public void setZonaFichaje(ZonaFichaje zonaFichaje) {
        this.zonaFichaje = zonaFichaje;
    }

    public EstadoMarcaFichaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoMarcaFichaje estado) {
        this.estado = estado;
    }

    public MotivoEstadoMarcaFichaje getMotivoEstado() {
        return motivoEstado;
    }

    public void setMotivoEstado(MotivoEstadoMarcaFichaje motivoEstado) {
        this.motivoEstado = motivoEstado;
    }

    public Usuario getRevisadoPorUsuario() {
        return revisadoPorUsuario;
    }

    public void setRevisadoPorUsuario(Usuario revisadoPorUsuario) {
        this.revisadoPorUsuario = revisadoPorUsuario;
    }

    public LocalDateTime getRevisadoAt() {
        return revisadoAt;
    }

    public void setRevisadoAt(LocalDateTime revisadoAt) {
        this.revisadoAt = revisadoAt;
    }

    public String getNotaRevision() {
        return notaRevision;
    }

    public void setNotaRevision(String notaRevision) {
        this.notaRevision = notaRevision;
    }
}
