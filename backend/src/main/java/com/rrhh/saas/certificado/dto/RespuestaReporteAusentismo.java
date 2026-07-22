package com.rrhh.saas.certificado.dto;

import java.time.LocalDate;
import java.util.List;

public record RespuestaReporteAusentismo(
        Long empresaId,
        LocalDate desde,
        LocalDate hasta,
        int cantidadEmpleadosConAusentismo,
        int diasReposoTotalesEmpresa,
        List<ResumenAusentismo> resumenesPorEmpleado
) {
}
