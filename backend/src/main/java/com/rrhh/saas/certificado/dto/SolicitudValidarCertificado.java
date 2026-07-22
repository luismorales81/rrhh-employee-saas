package com.rrhh.saas.certificado.dto;

import java.time.LocalDate;

public record SolicitudValidarCertificado(
        String diagnostico,
        Integer reposoDias,
        String medicoNombre,
        String medicoMatricula,
        LocalDate fechaEmision,
        Boolean corregidoManualmente,
        String observaciones
) {
}
