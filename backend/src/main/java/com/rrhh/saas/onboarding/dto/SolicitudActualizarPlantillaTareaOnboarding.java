package com.rrhh.saas.onboarding.dto;

public record SolicitudActualizarPlantillaTareaOnboarding(
        String nombre,
        Integer orden,
        Boolean obligatoria,
        String tipo
) {
}
