package com.rrhh.saas.comunicacion.dto;

import com.rrhh.saas.comunicacion.dominio.EstadoComunicacion;
import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;

import java.time.LocalDateTime;

public record RespuestaComunicacion(
        Long id,
        String titulo,
        String mensaje,
        TipoDestinatarioComunicacion tipoDestinatario,
        Long referenciaDestinatarioId,
        boolean requiereConfirmacion,
        EstadoComunicacion estado,
        LocalDateTime fechaPublicacion,
        LocalDateTime fechaPublicacionProgramada,
        long totalDestinatarios,
        long totalLeidos,
        long totalConfirmados
) {
}
