package com.rrhh.saas.documento.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SolicitudAsignarDocumento(
        @NotEmpty List<Long> empleadoIds,
        LocalDate fechaVencimiento
) {
}
