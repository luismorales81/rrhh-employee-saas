package com.rrhh.saas.comun.excepcion;

import java.time.LocalDateTime;

public record RespuestaError(
        LocalDateTime timestamp,
        int status,
        String mensaje
) {
}
