package com.rrhh.saas.onboarding.dto;

public record RespuestaPlantillaTareaOnboarding(
        Long id,
        String nombre,
        int orden,
        boolean obligatoria,
        String tipo
) {
}
