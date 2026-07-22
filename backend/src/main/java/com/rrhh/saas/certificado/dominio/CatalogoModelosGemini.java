package com.rrhh.saas.certificado.dominio;

import java.util.List;
import java.util.Set;

/**
 * Modelos Gemini soportados para extracción de certificados.
 */
public final class CatalogoModelosGemini {

    public static final String MODELO_DEFAULT = "gemini-2.0-flash";

    public static final List<String> MODELOS_DISPONIBLES = List.of(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
    );

    private static final Set<String> MODELOS_PERMITIDOS = Set.copyOf(MODELOS_DISPONIBLES);

    private CatalogoModelosGemini() {
    }

    public static boolean esModeloPermitido(String modelo) {
        return modelo != null && MODELOS_PERMITIDOS.contains(modelo);
    }
}
