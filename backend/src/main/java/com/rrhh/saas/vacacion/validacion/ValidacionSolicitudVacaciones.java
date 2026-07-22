package com.rrhh.saas.vacacion.validacion;

import java.util.ArrayList;
import java.util.List;

public record ValidacionSolicitudVacaciones(
        boolean esValida,
        List<String> errores,
        List<String> advertencias
) {
    public static ValidacionSolicitudVacaciones ok(List<String> advertencias) {
        return new ValidacionSolicitudVacaciones(true, List.of(), advertencias != null ? advertencias : List.of());
    }

    public static ValidacionSolicitudVacaciones conErrores(List<String> errores, List<String> advertencias) {
        return new ValidacionSolicitudVacaciones(
                errores == null || errores.isEmpty(),
                errores != null ? errores : List.of(),
                advertencias != null ? advertencias : List.of());
    }

    public ValidacionSolicitudVacaciones combinar(ValidacionSolicitudVacaciones otra) {
        List<String> todosErrores = new ArrayList<>(this.errores);
        todosErrores.addAll(otra.errores);
        List<String> todasAdvertencias = new ArrayList<>(this.advertencias);
        todasAdvertencias.addAll(otra.advertencias);
        return new ValidacionSolicitudVacaciones(todosErrores.isEmpty(), todosErrores, todasAdvertencias);
    }
}
