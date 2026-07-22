package com.rrhh.saas.documento.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SolicitudFirmarDocumento(
        @Size(max = 500) String urlArchivoFirmado,
        @Size(max = 255) String dispositivo,
        String imagenFirmaBase64
) {
}
