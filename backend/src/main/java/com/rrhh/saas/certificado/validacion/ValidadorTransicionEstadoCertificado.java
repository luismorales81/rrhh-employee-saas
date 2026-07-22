package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados para certificados médicos.
 */
public final class ValidadorTransicionEstadoCertificado {

    private static final Map<EstadoCertificado, Set<EstadoCertificado>> TRANSICIONES_PERMITIDAS;

    static {
        TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoCertificado.class);
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.CARGADO,
                EnumSet.of(EstadoCertificado.EN_PROCESAMIENTO, EstadoCertificado.EN_REVISION));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.EN_PROCESAMIENTO,
                EnumSet.of(EstadoCertificado.EXTRAIDO, EstadoCertificado.REQUIERE_ACLARACION, EstadoCertificado.EN_REVISION));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.EXTRAIDO,
                EnumSet.of(EstadoCertificado.EN_REVISION, EstadoCertificado.REQUIERE_ACLARACION,
                        EstadoCertificado.EN_PROCESAMIENTO));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.EN_REVISION,
                EnumSet.of(EstadoCertificado.VALIDADO, EstadoCertificado.RECHAZADO,
                        EstadoCertificado.REQUIERE_ACLARACION, EstadoCertificado.EN_PROCESAMIENTO));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.REQUIERE_ACLARACION,
                EnumSet.of(EstadoCertificado.EN_REVISION, EstadoCertificado.CARGADO,
                        EstadoCertificado.EN_PROCESAMIENTO));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.VALIDADO, EnumSet.noneOf(EstadoCertificado.class));
        TRANSICIONES_PERMITIDAS.put(EstadoCertificado.RECHAZADO, EnumSet.noneOf(EstadoCertificado.class));
    }

    private ValidadorTransicionEstadoCertificado() {
    }

    public static boolean validarTransicionEstado(EstadoCertificado actual, EstadoCertificado nuevo) {
        if (actual == null || nuevo == null) {
            return false;
        }
        if (actual == nuevo) {
            return true;
        }
        Set<EstadoCertificado> destinos = TRANSICIONES_PERMITIDAS.get(actual);
        return destinos != null && destinos.contains(nuevo);
    }
}
