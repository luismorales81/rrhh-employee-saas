package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.AlcanceZonaFichaje;
import com.rrhh.saas.fichaje.dominio.TipoAsignacionZonaFichaje;

import java.util.List;

public record RespuestaZonaFichaje(
        Long id,
        String nombre,
        String descripcion,
        List<List<Double>> coordenadas,
        AlcanceZonaFichaje alcance,
        boolean activa,
        List<RespuestaAsignacionZonaFichaje> asignaciones
) {
}
