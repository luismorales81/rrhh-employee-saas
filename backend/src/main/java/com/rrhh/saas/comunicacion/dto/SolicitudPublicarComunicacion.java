package com.rrhh.saas.comunicacion.dto;

import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SolicitudPublicarComunicacion(
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank String mensaje,
        @NotNull TipoDestinatarioComunicacion tipoDestinatario,
        Long referenciaDestinatarioId,
        boolean requiereConfirmacion,
        LocalDateTime fechaPublicacionProgramada
) {
}
