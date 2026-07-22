package com.rrhh.saas.recibo.dto;

import java.util.List;

public record RespuestaAnalisisCargaMasiva(
        List<FilaAnalisisCargaMasiva> filas,
        int totalArchivos,
        int emparejados,
        int conError
) {
}
