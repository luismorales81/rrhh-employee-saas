package com.rrhh.saas.ausentismo.dto;

import java.time.LocalDate;

public record RespuestaAusencia(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        Long tipoAusenciaId,
        String tipoAusenciaNombre,
        LocalDate fecha,
        String motivo,
        boolean justificada,
        String urlCertificado
) {
}
