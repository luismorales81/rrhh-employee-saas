package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.TipoAsignacionZonaFichaje;
import jakarta.validation.constraints.NotNull;

public record SolicitudAsignacionZonaFichaje(
        Long empleadoId,
        Long gerenciaId,
        Long areaId,
        Long sectorId,
        @NotNull TipoAsignacionZonaFichaje tipo
) {
}
