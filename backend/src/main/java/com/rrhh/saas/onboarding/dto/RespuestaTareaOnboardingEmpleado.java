package com.rrhh.saas.onboarding.dto;

import java.time.LocalDateTime;

public record RespuestaTareaOnboardingEmpleado(
        Long id,
        Long plantillaId,
        String nombre,
        int orden,
        boolean obligatoria,
        String tipo,
        boolean completada,
        LocalDateTime fechaCompletado,
        String notas
) {
}
