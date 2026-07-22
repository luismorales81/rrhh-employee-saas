package com.rrhh.saas.vacacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolicitudCrearPoliticaVacaciones(
        @NotBlank String nombre,
        String descripcion,
        List<SolicitudRangoPolitica> rangos
) {
    public record SolicitudRangoPolitica(
            @NotNull Integer aniosMinimos,
            Integer aniosMaximos,
            @NotNull Integer diasOtorgados
    ) {
    }
}
