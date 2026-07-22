package com.rrhh.saas.recibo.dto;

import com.rrhh.saas.recibo.dominio.TipoConfirmacionRecibo;
import jakarta.validation.constraints.NotNull;

public record SolicitudConfirmarRecepcionRecibo(
        @NotNull TipoConfirmacionRecibo tipo,
        String comentario,
        String contrasena
) {
}
