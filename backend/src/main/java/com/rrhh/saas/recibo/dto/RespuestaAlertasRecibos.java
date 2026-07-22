package com.rrhh.saas.recibo.dto;

import com.rrhh.saas.recibo.dominio.EstadoRecibo;

import java.time.LocalDateTime;
import java.util.List;

public record RespuestaAlertasRecibos(
        int totalPendientesFirma,
        int totalDisconformidades,
        int totalAntiguosSinFirmar,
        int diasUmbralAntiguedad,
        List<ReciboAlertaResumen> pendientesAntiguos,
        List<ReciboAlertaResumen> disconformidadesRecientes
) {
    public record ReciboAlertaResumen(
            Long id,
            Long empleadoId,
            String nombreEmpleado,
            String periodo,
            EstadoRecibo estado,
            LocalDateTime fechaPublicacion,
            String comentarioDisconformidad,
            LocalDateTime notificadoEn
    ) {
    }
}
