package com.rrhh.saas.ausentismo.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudRegistrarAusencia(
        @NotNull Long empleadoId,
        @NotNull Long tipoAusenciaId,
        @NotNull LocalDate fecha,
        String motivo,
        boolean justificada,
        String urlCertificado
) {
}
