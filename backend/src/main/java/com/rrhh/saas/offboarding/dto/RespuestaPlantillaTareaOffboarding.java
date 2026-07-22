package com.rrhh.saas.offboarding.dto;

public record RespuestaPlantillaTareaOffboarding(
        Long id,
        String nombre,
        int orden,
        boolean obligatoria,
        String tipo
) {
}
