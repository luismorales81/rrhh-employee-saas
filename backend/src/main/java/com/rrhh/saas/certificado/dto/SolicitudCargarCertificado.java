package com.rrhh.saas.certificado.dto;

import com.rrhh.saas.certificado.dominio.OrigenCargaCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;
import jakarta.validation.constraints.NotNull;

public record SolicitudCargarCertificado(
        @NotNull Long archivoId,
        @NotNull TipoExamenCertificado tipoExamen,
        @NotNull OrigenCargaCertificado origenCarga,
        Long empleadoId
) {
}
