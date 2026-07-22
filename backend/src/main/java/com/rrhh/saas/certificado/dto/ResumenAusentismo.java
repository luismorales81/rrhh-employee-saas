package com.rrhh.saas.certificado.dto;

import java.time.LocalDate;

public record ResumenAusentismo(
        Long empleadoId,
        LocalDate desde,
        LocalDate hasta,
        int totalCertificadosValidados,
        int diasReposoTotales,
        int certificadosConReposo
) {
}
