package com.rrhh.saas.certificado.dto;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.OrigenCargaCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista sin diagnóstico para RRHH y administración.
 */
public record CertificadoResumenDTO(
        Long id,
        Long empleadoId,
        TipoExamenCertificado tipoExamen,
        EstadoCertificado estado,
        Integer reposoDias,
        LocalDate fechaEmision,
        OrigenCargaCertificado origenCarga,
        String archivoUrl,
        LocalDateTime createdAt,
        boolean revisionPrioritaria,
        List<String> motivosAlerta
) {
}
