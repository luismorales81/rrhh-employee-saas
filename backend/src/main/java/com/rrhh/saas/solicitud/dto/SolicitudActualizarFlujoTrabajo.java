package com.rrhh.saas.solicitud.dto;

import com.rrhh.saas.solicitud.dominio.TipoPaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolicitudActualizarFlujoTrabajo(
        @NotBlank String nombre,
        String descripcion,
        List<SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo> pasos
) {
}
