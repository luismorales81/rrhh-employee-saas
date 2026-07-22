package com.rrhh.saas.vacacion.validacion;

import java.time.LocalDate;

public record ValidacionFechaInicio(
        boolean esValida,
        LocalDate fechaSugerida,
        String motivo
) {
    public static ValidacionFechaInicio valida() {
        return new ValidacionFechaInicio(true, null, null);
    }

    public static ValidacionFechaInicio invalida(LocalDate fechaSugerida, String motivo) {
        return new ValidacionFechaInicio(false, fechaSugerida, motivo);
    }
}
