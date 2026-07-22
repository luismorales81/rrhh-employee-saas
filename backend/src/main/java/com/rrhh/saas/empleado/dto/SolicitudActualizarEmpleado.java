package com.rrhh.saas.empleado.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record SolicitudActualizarEmpleado(
        @NotBlank String nombre,
        @NotBlank String apellido,
        String numeroEmpleado,
        String dni,
        String cuil,
        LocalDate fechaNacimiento,
        LocalDate fechaIngreso,
        String telefono,
        String emailPersonal,
        String emailLaboral,
        Long supervisorId,
        Long gerenciaId,
        Long areaId,
        Long sectorId,
        Long puestoId,
        Long usuarioId
) {
}
