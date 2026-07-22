package com.rrhh.saas.fichaje.dto;

public record SolicitudConfiguracionFichajeEmpleado(
        boolean fichajeMovilHabilitado,
        boolean restringirSoloZonasAsignadas
) {
}
