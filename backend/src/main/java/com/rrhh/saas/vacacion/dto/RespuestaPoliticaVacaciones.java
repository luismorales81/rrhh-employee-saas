package com.rrhh.saas.vacacion.dto;

import java.util.List;

public record RespuestaPoliticaVacaciones(
        Long id,
        String nombre,
        String descripcion,
        boolean activa,
        List<RespuestaRangoPolitica> rangos
) {
    public record RespuestaRangoPolitica(
            Long id,
            int aniosMinimos,
            Integer aniosMaximos,
            int diasOtorgados
    ) {
    }
}
