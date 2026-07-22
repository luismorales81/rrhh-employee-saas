package com.rrhh.saas.fichaje.dto;

public record RespuestaPoliticaFichaje(
        String textoTransparencia,
        int version,
        boolean permitirFichajeSinGps,
        int precisionMaximaMetros
) {
}
