package com.rrhh.saas.certificado.ia;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.rrhh.saas.certificado.validacion.ParseadorRespuestaGemini;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "app.certificados.ia.simulada", havingValue = "false", matchIfMissing = true)
public class ServicioExtraccionVertexGemini implements GeminiExtraccionService {

    private static final Logger log = LoggerFactory.getLogger(ServicioExtraccionVertexGemini.class);

    private static final String PROMPT_EXTRACCION = """
            Sos un asistente de extracción de datos médicos. Vas a recibir la imagen o PDF de un certificado médico laboral.

            Extraé ÚNICAMENTE estos campos, en JSON, sin texto adicional:

            {
              "diagnostico": string,
              "reposo_dias": number,
              "medico_nombre": string,
              "medico_matricula": string,
              "fecha_emision": string (formato YYYY-MM-DD),
              "confianza_extraccion": number (0 a 1, tu propia estimación de qué tan legible/claro era el documento)
            }

            Reglas:
            - Si un campo no es legible o no está presente, devolvé null en ese campo — NUNCA inventes un valor.
            - Si el documento no parece ser un certificado médico, devolvé {"error": "documento_no_reconocido"}.
            - No agregues explicaciones, comentarios ni markdown. Solo el JSON.
            """;

    @Override
    public ResultadoExtraccionGemini extraer(byte[] archivo, String tipoMime, ConfiguracionVertexTenant config) {
        if (config == null || config.credencialesServicioJson() == null) {
            throw new ExcepcionNegocio("Configuración Vertex AI incompleta para el tenant");
        }

        try {
            GoogleCredentials credenciales = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(
                            config.credencialesServicioJson().getBytes(StandardCharsets.UTF_8)))
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            try (Client cliente = Client.builder()
                    .project(config.proyectoGcp())
                    .location(config.regionVertex())
                    .vertexAI(true)
                    .credentials(credenciales)
                    .build()) {

                Part parteArchivo = Part.fromBytes(archivo, tipoMime);
                Part partePrompt = Part.fromText(PROMPT_EXTRACCION);
                Content contenido = Content.fromParts(partePrompt, parteArchivo);

                GenerateContentConfig configuracion = GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .build();

                GenerateContentResponse respuesta = cliente.models.generateContent(
                        config.modeloGemini(),
                        contenido,
                        configuracion);

                String texto = respuesta.text();
                if (texto == null || texto.isBlank()) {
                    throw new ExcepcionNegocio("Vertex AI no devolvió contenido de extracción");
                }
                return ParseadorRespuestaGemini.parsear(texto);
            }
        } catch (ExcepcionNegocio e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al llamar Vertex AI: {}", e.getMessage());
            throw new ExcepcionNegocio("Error al extraer datos con Vertex AI: " + e.getMessage());
        }
    }
}
