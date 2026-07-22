package com.rrhh.saas.certificado.dto;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.OrigenCargaCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista completa con diagnóstico para médico laboral, auditor médico y empleado propio.
 */
public record CertificadoCompletoDTO(
        Long id,
        Long empleadoId,
        TipoExamenCertificado tipoExamen,
        EstadoCertificado estado,
        OrigenCargaCertificado origenCarga,
        String archivoUrl,
        String archivoHashSha256,
        String diagnostico,
        Integer reposoDias,
        String medicoNombre,
        String medicoMatricula,
        LocalDate fechaEmision,
        Float confianzaExtraccion,
        boolean corregidoManualmente,
        LocalDateTime createdAt,
        boolean revisionPrioritaria,
        List<String> motivosAlerta
) {
}
