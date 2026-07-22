package com.rrhh.saas.ausentismo.dto;

import java.time.LocalDate;

public record SolicitudActualizarAusencia(
        String motivo,
        Boolean justificada,
        String urlCertificado
) {
}
