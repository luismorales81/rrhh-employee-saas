package com.rrhh.saas.vacacion.dto;

import java.time.LocalDate;

public record SolicitudAprobarVacaciones(
        LocalDate fechaInicioAprobada,
        LocalDate fechaFinAprobada,
        String comentarios
) {
}
