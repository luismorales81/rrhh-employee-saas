package com.rrhh.saas.certificado.ia;

/**
 * Configuración Vertex AI resuelta por tenant para una extracción.
 */
public record ConfiguracionVertexTenant(
        String proyectoGcp,
        String regionVertex,
        String modeloGemini,
        String credencialesServicioJson
) {
}
