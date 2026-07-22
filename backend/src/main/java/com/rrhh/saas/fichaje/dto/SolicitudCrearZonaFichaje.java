package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.AlcanceZonaFichaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolicitudCrearZonaFichaje(
        @NotBlank String nombre,
        String descripcion,
        @NotEmpty List<List<Double>> coordenadas,
        @NotNull AlcanceZonaFichaje alcance,
        boolean activa
) {
}
