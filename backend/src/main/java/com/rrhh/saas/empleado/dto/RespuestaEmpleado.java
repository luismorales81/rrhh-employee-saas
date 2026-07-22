package com.rrhh.saas.empleado.dto;

import com.rrhh.saas.empleado.dominio.EstadoEmpleado;

import java.time.LocalDate;

public record RespuestaEmpleado(
        Long id,
        String nombre,
        String apellido,
        String numeroEmpleado,
        String dni,
        String cuil,
        LocalDate fechaIngreso,
        EstadoEmpleado estado,
        String emailLaboral,
        Long usuarioId,
        Long supervisorId,
        Long gerenciaId,
        Long areaId,
        Long sectorId,
        Long puestoId,
        Long legajoId
) {
}
