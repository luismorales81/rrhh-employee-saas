package com.rrhh.saas.fichaje.dto;

import java.time.LocalDateTime;

public record RespuestaConfiguracionFichajeEmpleado(
        Long empleadoId,
        String empleadoNombre,
        boolean fichajeMovilHabilitado,
        boolean restringirSoloZonasAsignadas,
        LocalDateTime consentimientoAceptadoAt,
        Integer versionPoliticaAceptada,
        boolean requiereConsentimiento
) {
}
