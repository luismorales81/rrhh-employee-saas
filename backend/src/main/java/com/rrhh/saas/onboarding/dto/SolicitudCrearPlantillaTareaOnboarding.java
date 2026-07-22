package com.rrhh.saas.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudCrearPlantillaTareaOnboarding(
        @NotBlank String nombre,
        @NotNull Integer orden,
        boolean obligatoria,
        @NotBlank String tipo
) {
}
