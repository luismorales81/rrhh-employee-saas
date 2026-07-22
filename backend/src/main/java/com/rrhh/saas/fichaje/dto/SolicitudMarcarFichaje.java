package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.TipoMarcaFichaje;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SolicitudMarcarFichaje(
        @NotNull TipoMarcaFichaje tipo,
        Double latitud,
        Double longitud,
        Double precisionMetros,
        LocalDateTime fechaHoraDispositivo,
        String idDispositivo,
        String modeloDispositivo,
        String sistemaOperativo,
        String versionApp
) {
}
