package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.AlcanceZonaFichaje;

import java.util.List;

public record SolicitudActualizarZonaFichaje(
        String nombre,
        String descripcion,
        List<List<Double>> coordenadas,
        AlcanceZonaFichaje alcance,
        Boolean activa
) {
}
