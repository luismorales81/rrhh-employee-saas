package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.vacacion.dominio.DiaSemana;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SolicitudCrearPatronDescanso(
        @NotEmpty List<DiaSemana> diasDescanso,
        @NotNull LocalDate vigenciaDesde,
        LocalDate vigenciaHasta
) {
}
