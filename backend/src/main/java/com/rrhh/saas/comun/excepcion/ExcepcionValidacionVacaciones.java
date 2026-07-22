package com.rrhh.saas.comun.excepcion;

import java.util.List;

public class ExcepcionValidacionVacaciones extends RuntimeException {

    private final List<String> errores;
    private final List<String> advertencias;

    public ExcepcionValidacionVacaciones(List<String> errores, List<String> advertencias) {
        super(errores != null && !errores.isEmpty() ? errores.get(0) : "Validación de vacaciones fallida");
        this.errores = errores != null ? errores : List.of();
        this.advertencias = advertencias != null ? advertencias : List.of();
    }

    public List<String> getErrores() {
        return errores;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }
}
