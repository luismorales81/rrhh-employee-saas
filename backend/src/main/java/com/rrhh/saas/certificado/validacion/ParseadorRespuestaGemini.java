package com.rrhh.saas.certificado.validacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Parsea y valida la respuesta JSON de Gemini.
 */
public final class ParseadorRespuestaGemini {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ParseadorRespuestaGemini() {
    }

    public static ResultadoExtraccionGemini parsear(String textoRespuesta) {
        if (textoRespuesta == null || textoRespuesta.isBlank()) {
            throw new ExcepcionNegocio("Respuesta vacía del servicio de extracción");
        }

        String json = extraerJson(textoRespuesta.trim());
        try {
            JsonNode nodo = MAPPER.readTree(json);
            if (nodo.hasNonNull("error")) {
                return ResultadoExtraccionGemini.errorDocumento(nodo.get("error").asText());
            }
            return new ResultadoExtraccionGemini(
                    textoONulo(nodo, "diagnostico"),
                    enteroONulo(nodo, "reposo_dias"),
                    textoONulo(nodo, "medico_nombre"),
                    textoONulo(nodo, "medico_matricula"),
                    fechaONula(nodo, "fecha_emision"),
                    flotanteONulo(nodo, "confianza_extraccion"),
                    null
            );
        } catch (ExcepcionNegocio e) {
            throw e;
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo interpretar la respuesta de extracción IA");
        }
    }

    private static String extraerJson(String texto) {
        if (texto.startsWith("```")) {
            int inicio = texto.indexOf('{');
            int fin = texto.lastIndexOf('}');
            if (inicio >= 0 && fin > inicio) {
                return texto.substring(inicio, fin + 1);
            }
            throw new ExcepcionNegocio("Respuesta IA con formato markdown inválido");
        }
        if (!texto.startsWith("{")) {
            throw new ExcepcionNegocio("Respuesta IA no es JSON válido");
        }
        return texto;
    }

    private static String textoONulo(JsonNode nodo, String campo) {
        if (!nodo.has(campo) || nodo.get(campo).isNull()) {
            return null;
        }
        String valor = nodo.get(campo).asText();
        return valor.isBlank() ? null : valor;
    }

    private static Integer enteroONulo(JsonNode nodo, String campo) {
        if (!nodo.has(campo) || nodo.get(campo).isNull()) {
            return null;
        }
        return nodo.get(campo).asInt();
    }

    private static Float flotanteONulo(JsonNode nodo, String campo) {
        if (!nodo.has(campo) || nodo.get(campo).isNull()) {
            return null;
        }
        return (float) nodo.get(campo).asDouble();
    }

    private static LocalDate fechaONula(JsonNode nodo, String campo) {
        String valor = textoONulo(nodo, campo);
        if (valor == null) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
