package com.rrhh.saas.fichaje.dto;

public record SolicitudActualizarPoliticaFichaje(
        String textoTransparencia,
        boolean permitirFichajeSinGps,
        int precisionMaximaMetros
) {
}
