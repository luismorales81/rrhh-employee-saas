package com.rrhh.saas.ausentismo.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudRegistrarMisAusencias(
        @NotNull Long tipoAusenciaId,
        @NotNull LocalDate fecha,
        String motivo,
        boolean justificada,
        String urlCertificado
) {
}
