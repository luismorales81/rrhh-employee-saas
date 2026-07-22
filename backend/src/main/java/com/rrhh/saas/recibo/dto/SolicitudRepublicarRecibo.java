package com.rrhh.saas.recibo.dto;

import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRepublicarRecibo(
        @NotNull Long archivoId,
        @NotBlank @Size(min = 10, max = 1000) String motivo
) {
}
