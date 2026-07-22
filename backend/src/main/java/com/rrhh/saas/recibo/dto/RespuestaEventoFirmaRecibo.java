package com.rrhh.saas.recibo.dto;



import com.rrhh.saas.recibo.dominio.TipoEventoFirmaRecibo;



import java.time.LocalDateTime;



public record RespuestaEventoFirmaRecibo(

        Long id,

        Long reciboId,

        TipoEventoFirmaRecibo tipo,

        LocalDateTime timestampEvento,

        String direccionIp,

        String userAgent,

        String sistemaOperativo,

        String hashPdfEnFirma,

        String comentario,

        Long empleadoId,

        Long usuarioId

) {

}


