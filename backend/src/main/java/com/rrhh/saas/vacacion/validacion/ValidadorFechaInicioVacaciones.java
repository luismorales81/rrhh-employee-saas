package com.rrhh.saas.vacacion.validacion;

import com.rrhh.saas.vacacion.dominio.DiaSemana;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Valida que la fecha de inicio sea el día inmediato posterior al descanso semanal
 * del trabajador (Art. 151 LCT).
 *
 * <p>Si el patrón tiene varios días de descanso consecutivos (ej. sábado y domingo),
 * la fecha válida es el día siguiente al último día del bloque consecutivo.</p>
 *
 * <p>Si los días de descanso no son consecutivos (ej. martes y viernes), cada día
 * de descanso define su propio día de inicio válido (el día siguiente a ese descanso).</p>
 *
 * <p>TODO: feriados que caigan en día de inicio válido — no resuelto en esta iteración.</p>
 */
public final class ValidadorFechaInicioVacaciones {

    private ValidadorFechaInicioVacaciones() {
    }

    public static ValidacionFechaInicio validar(LocalDate fechaInicio, List<DiaSemana> diasDescanso) {
        if (diasDescanso == null || diasDescanso.isEmpty()) {
            return ValidacionFechaInicio.valida();
        }

        Set<DayOfWeek> diasInicioValidos = calcularDiasInicioValidos(diasDescanso);
        DayOfWeek diaSemanaInicio = fechaInicio.getDayOfWeek();

        if (diasInicioValidos.contains(diaSemanaInicio)) {
            return ValidacionFechaInicio.valida();
        }

        LocalDate sugerida = buscarProximaFechaValida(fechaInicio, diasInicioValidos);
        String nombreDia = diaSemanaInicio.getDisplayName(TextStyle.FULL, new Locale("es", "AR"));
        String motivo = "Según Art. 151 LCT, las vacaciones deben comenzar el día siguiente "
                + "al descanso semanal. El " + nombreDia + " no es una fecha de inicio válida "
                + "para su patrón de descanso.";
        return ValidacionFechaInicio.invalida(sugerida, motivo);
    }

    static Set<DayOfWeek> calcularDiasInicioValidos(List<DiaSemana> diasDescanso) {
        List<Integer> ordinales = diasDescanso.stream()
                .map(DiaSemana::ordinalIso)
                .distinct()
                .sorted()
                .toList();

        Set<DayOfWeek> inicios = new HashSet<>();
        List<List<Integer>> bloques = agruparBloquesConsecutivos(ordinales);

        for (List<Integer> bloque : bloques) {
            int ultimoDescanso = bloque.get(bloque.size() - 1);
            int ordinalInicio = (ultimoDescanso + 1) % 7;
            inicios.add(DiaSemana.values()[ordinalInicio].aDayOfWeek());
        }
        return inicios;
    }

    private static List<List<Integer>> agruparBloquesConsecutivos(List<Integer> ordinales) {
        if (ordinales.isEmpty()) {
            return List.of();
        }

        List<List<Integer>> bloques = new ArrayList<>();
        List<Integer> bloqueActual = new ArrayList<>();
        bloqueActual.add(ordinales.get(0));

        for (int i = 1; i < ordinales.size(); i++) {
            int anterior = ordinales.get(i - 1);
            int actual = ordinales.get(i);
            if (actual == anterior + 1) {
                bloqueActual.add(actual);
            } else {
                bloques.add(bloqueActual);
                bloqueActual = new ArrayList<>();
                bloqueActual.add(actual);
            }
        }
        bloques.add(bloqueActual);
        return bloques;
    }

    private static LocalDate buscarProximaFechaValida(LocalDate desde, Set<DayOfWeek> diasInicioValidos) {
        LocalDate candidata = desde;
        for (int i = 0; i < 14; i++) {
            if (diasInicioValidos.contains(candidata.getDayOfWeek())) {
                return candidata;
            }
            candidata = candidata.plusDays(1);
        }
        return candidata;
    }
}
