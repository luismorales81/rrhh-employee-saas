package com.rrhh.saas.documento.dto;

import com.rrhh.saas.documento.dominio.EstadoAsignacionDocumento;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RespuestaAsignacionDocumento(
        Long id,
        Long empleadoId,
        String nombreEmpleado,
        Long documentoId,
        String tituloDocumento,
        String urlArchivo,
        boolean requiereFirma,
        EstadoAsignacionDocumento estado,
        LocalDateTime fechaAsignacion,
        LocalDateTime fechaLectura,
        LocalDate fechaVencimiento,
        boolean firmado
) {
}
