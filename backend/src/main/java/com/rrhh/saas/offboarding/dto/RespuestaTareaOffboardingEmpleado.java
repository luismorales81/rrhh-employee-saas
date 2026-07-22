package com.rrhh.saas.offboarding.dto;

import java.time.LocalDateTime;

public record RespuestaTareaOffboardingEmpleado(
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
