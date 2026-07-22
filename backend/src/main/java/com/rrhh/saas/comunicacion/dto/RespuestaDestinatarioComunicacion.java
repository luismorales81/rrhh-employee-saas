package com.rrhh.saas.comunicacion.dto;

import java.time.LocalDateTime;

public record RespuestaDestinatarioComunicacion(
        Long id,
        Long comunicacionId,
        String tituloComunicacion,
        String mensaje,
        Long empleadoId,
        String nombreEmpleado,
        boolean requiereConfirmacion,
        LocalDateTime fechaPublicacion,
        LocalDateTime fechaLectura,
        LocalDateTime fechaConfirmacion,
        boolean leida,
        boolean confirmada
) {
}
