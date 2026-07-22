package com.rrhh.saas.activo.dto;

import java.time.LocalDate;

public record SolicitudDevolverActivo(
        LocalDate fechaDevolucion,
        String notas
) {
}
