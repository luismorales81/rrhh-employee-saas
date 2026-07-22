package com.rrhh.saas.fichaje.dto;

import com.rrhh.saas.fichaje.dominio.EstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.MotivoEstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.TipoMarcaFichaje;

import java.time.LocalDateTime;

public record RespuestaMarcaFichaje(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        TipoMarcaFichaje tipo,
        LocalDateTime fechaHoraServidor,
        LocalDateTime fechaHoraDispositivo,
        Double latitud,
        Double longitud,
        Double precisionMetros,
        String idDispositivo,
        String modeloDispositivo,
        String sistemaOperativo,
        String versionApp,
        Long zonaFichajeId,
        String zonaFichajeNombre,
        EstadoMarcaFichaje estado,
        MotivoEstadoMarcaFichaje motivoEstado,
        String notaRevision,
        LocalDateTime revisadoAt
) {
}
