package com.rrhh.saas.notificacion.dto;

import java.time.LocalDateTime;

public record RespuestaNotificacion(
        Long id,
        String titulo,
        String mensaje,
        String tipo,
        Long referenciaId,
        boolean leida,
        LocalDateTime createdAt
) {
}
