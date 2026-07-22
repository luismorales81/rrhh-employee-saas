package com.rrhh.saas.offboarding.dto;

public record SolicitudActualizarPlantillaTareaOffboarding(
        String nombre,
        Integer orden,
        Boolean obligatoria,
        String tipo
) {
}
