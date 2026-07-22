package com.rrhh.saas.recibo.dto;

import com.rrhh.saas.recibo.dominio.EstadoRecibo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RespuestaRecibo(
        Long id,
        Long empleadoId,
        String nombreEmpleado,
        String periodo,
        String titulo,
        String urlDescargaPdf,
        EstadoRecibo estado,
        LocalDateTime fechaPublicacion,
        LocalDateTime vistoPorEmpleadoEn,
        LocalDateTime fechaFirma,
        BigDecimal montoNeto,
        BigDecimal montoBruto,
        String hashPdf,
        boolean requiereRepublicacion,
        int version,
        Long reciboAnteriorId,
        boolean vigente,
        LocalDateTime notificadoEn,
        String canalNotificacion,
        String comentarioDisconformidad
) {
}
