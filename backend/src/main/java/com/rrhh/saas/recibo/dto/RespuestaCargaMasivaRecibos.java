package com.rrhh.saas.recibo.dto;

import java.util.List;

public record RespuestaCargaMasivaRecibos(
        List<ReciboCargadoMasivo> cargados,
        List<FilaResultadoCargaMasiva> omitidos,
        List<FilaResultadoCargaMasiva> errores
) {
}
