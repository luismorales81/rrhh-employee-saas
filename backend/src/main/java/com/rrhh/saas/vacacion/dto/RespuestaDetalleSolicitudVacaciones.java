package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RespuestaDetalleSolicitudVacaciones(
        RespuestaSolicitudVacaciones solicitud,
        boolean fechaInicioValida,
        LocalDate fechaInicioSugerida,
        String motivoFechaInicio,
        List<String> erroresValidacion,
        List<String> advertenciasValidacion
) {
}
