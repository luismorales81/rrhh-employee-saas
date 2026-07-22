package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.vacacion.dominio.DiaSemana;

import java.time.LocalDate;
import java.util.List;

public record RespuestaPatronDescanso(
        Long id,
        Long empleadoId,
        List<DiaSemana> diasDescanso,
        LocalDate vigenciaDesde,
        LocalDate vigenciaHasta
) {
}
