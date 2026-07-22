package com.rrhh.saas.documento.dto;

import java.time.LocalDateTime;

public record RespuestaEvidenciaFirma(
        Long asignacionId,
        LocalDateTime fechaFirma,
        String hashFirma,
        String urlArchivoFirmado,
        String direccionIp,
        String dispositivo
) {
}
