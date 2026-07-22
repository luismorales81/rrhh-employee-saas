package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.TipoAsignacionZonaFichaje;

public record RespuestaAsignacionZonaFichaje(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        Long gerenciaId,
        String gerenciaNombre,
        Long areaId,
        String areaNombre,
        Long sectorId,
        String sectorNombre,
        TipoAsignacionZonaFichaje tipo
) {
}
