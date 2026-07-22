package com.rrhh.saas.vacacion.validacion;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.vacacion.dominio.ConfiguracionVacaciones;
import com.rrhh.saas.vacacion.servicio.CalculadorVacaciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Valida fraccionamiento (Ley 27.802), saldo anual, solapamiento y anticipación.
 * La anticipación insuficiente genera advertencia, no bloqueo (protección al trabajador).
 */
public final class ValidadorSolicitudVacaciones {

    private ValidadorSolicitudVacaciones() {
    }

    public record PeriodoSolicitudExistente(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            EstadoSolicitud estado,
            Long solicitudIdExcluir
    ) {
    }

    public record DatosSolicitud(LocalDate fechaInicio, LocalDate fechaFin) {
    }

    public static ValidacionSolicitudVacaciones validar(
            DatosSolicitud solicitud,
            BigDecimal saldoDisponible,
            ConfiguracionVacaciones config,
            List<PeriodoSolicitudExistente> solicitudesExistentes,
            LocalDate hoy,
            Long solicitudIdExcluir) {

        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        if (solicitud.fechaFin().isBefore(solicitud.fechaInicio())) {
            errores.add("La fecha fin debe ser posterior o igual a la fecha inicio");
            return ValidacionSolicitudVacaciones.conErrores(errores, advertencias);
        }

        int diasCorridos = CalculadorVacaciones.calcularDiasEntreFechas(
                solicitud.fechaInicio(), solicitud.fechaFin());
        int minimoFraccion = config != null
                ? config.diasMinimosFraccionEfectivos()
                : ConfiguracionVacaciones.PISO_LEGAL_DIAS_FRACCION;

        if (diasCorridos < minimoFraccion) {
            errores.add("Cada fracción de vacaciones debe tener al menos " + minimoFraccion
                    + " días corridos (Ley 27.802)");
        }

        BigDecimal diasSolicitados = BigDecimal.valueOf(diasCorridos);
        boolean permiteSinSaldo = config != null && config.isPermiteSolicitudSinSaldo();

        if (!permiteSinSaldo && diasSolicitados.compareTo(saldoDisponible) > 0) {
            errores.add("Saldo insuficiente: disponible " + saldoDisponible.intValue()
                    + " días, solicitados " + diasCorridos);
        }

        if (solapaConExistentes(solicitud, solicitudesExistentes, solicitudIdExcluir)) {
            errores.add("El período solicitado se superpone con otra solicitud pendiente o aprobada");
        }

        if (config != null && hoy != null) {
            long diasAnticipacion = ChronoUnit.DAYS.between(hoy, solicitud.fechaInicio());
            if (diasAnticipacion < config.getAnticipacionMinimaDias()) {
                advertencias.add("La solicitud no cumple la anticipación mínima de "
                        + config.getAnticipacionMinimaDias() + " días corridos. "
                        + "El empleador puede autorizarla igualmente.");
            }
        }

        return ValidacionSolicitudVacaciones.conErrores(errores, advertencias);
    }

    private static boolean solapaConExistentes(
            DatosSolicitud solicitud,
            List<PeriodoSolicitudExistente> existentes,
            Long excluirId) {

        for (PeriodoSolicitudExistente e : existentes) {
            if (excluirId != null && excluirId.equals(e.solicitudIdExcluir())) {
                continue;
            }
            if (e.estado() != EstadoSolicitud.APROBADO
                    && e.estado() != EstadoSolicitud.EN_REVISION
                    && e.estado() != EstadoSolicitud.BORRADOR) {
                continue;
            }
            if (!solicitud.fechaFin().isBefore(e.fechaInicio())
                    && !solicitud.fechaInicio().isAfter(e.fechaFin())) {
                return true;
            }
        }
        return false;
    }
}
