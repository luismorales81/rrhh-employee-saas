package com.rrhh.saas.recibo.dto;

public record FilaAnalisisCargaMasiva(
        String nombreArchivo,
        Long empleadoId,
        String nombreEmpleado,
        String estado,
        String mensaje
) {
}
