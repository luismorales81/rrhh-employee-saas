package com.rrhh.saas.nucleo.dominio;

import com.rrhh.saas.comun.entidad.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "permiso")
public class Permiso extends EntidadAuditable {

    @Column(nullable = false, unique = true)
    private String codigo;

    private String descripcion;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
