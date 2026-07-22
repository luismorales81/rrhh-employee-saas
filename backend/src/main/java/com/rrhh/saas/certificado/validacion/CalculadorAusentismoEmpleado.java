package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dto.ResumenAusentismo;

import java.time.LocalDate;
import java.util.List;

/**
 * Calcula impacto en ausentismo a partir de certificados validados. Función pura.
 */
public final class CalculadorAusentismoEmpleado {

    private CalculadorAusentismoEmpleado() {
    }

    public static ResumenAusentismo calcular(
            Long empleadoId,
            LocalDate desde,
            LocalDate hasta,
            List<Certificado> certificadosValidados) {

        int totalCertificados = 0;
        int diasReposoTotales = 0;
        int certificadosConReposo = 0;

        if (certificadosValidados != null) {
            for (Certificado certificado : certificadosValidados) {
                if (certificado.getEstado() != EstadoCertificado.VALIDADO) {
                    continue;
                }
                DatosExtraidosCertificado datos = certificado.getDatosExtraidos();
                if (datos == null || datos.getFechaEmision() == null) {
                    continue;
                }
                if (datos.getFechaEmision().isBefore(desde) || datos.getFechaEmision().isAfter(hasta)) {
                    continue;
                }
                totalCertificados++;
                if (datos.getReposoDias() != null && datos.getReposoDias() > 0) {
                    diasReposoTotales += datos.getReposoDias();
                    certificadosConReposo++;
                }
            }
        }

        return new ResumenAusentismo(
                empleadoId,
                desde,
                hasta,
                totalCertificados,
                diasReposoTotales,
                certificadosConReposo
        );
    }
}
