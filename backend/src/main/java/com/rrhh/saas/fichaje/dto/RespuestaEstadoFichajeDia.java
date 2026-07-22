package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.TipoMarcaFichaje;

public record RespuestaEstadoFichajeDia(
        TipoMarcaFichaje siguienteTipo,
        boolean fichajeHabilitado,
        boolean requiereConsentimiento,
        String ultimaMarcaHora,
        TipoMarcaFichaje ultimaMarcaTipo
) {
}
