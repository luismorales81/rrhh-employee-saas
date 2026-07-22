package com.rrhh.saas.comun.excepcion;

import java.time.LocalDateTime;
import java.util.List;

public record RespuestaErrorValidacionVacaciones(
        LocalDateTime timestamp,
        int codigo,
        String mensaje,
        List<String> errores,
        List<String> advertencias
) {
}
