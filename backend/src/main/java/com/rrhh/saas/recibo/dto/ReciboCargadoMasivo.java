package com.rrhh.saas.recibo.dto;

public record ReciboCargadoMasivo(
        Long empleadoId,
        String nombreEmpleado,
        Long reciboId,
        String nombreArchivo
) {
}
