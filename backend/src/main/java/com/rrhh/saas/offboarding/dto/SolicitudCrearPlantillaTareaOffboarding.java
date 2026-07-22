package com.rrhh.saas.offboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudCrearPlantillaTareaOffboarding(
        @NotBlank String nombre,
        @NotNull Integer orden,
        boolean obligatoria,
        @NotBlank String tipo
) {
}
