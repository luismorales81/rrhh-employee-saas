package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseadorRespuestaGeminiTest {

    @Test
    void parseaJsonValido() {
        String json = """
                {
                  "diagnostico": "Gripe",
                  "reposo_dias": 3,
                  "medico_nombre": "Dr. Pérez",
                  "medico_matricula": "MN-999",
                  "fecha_emision": "2026-07-01",
                  "confianza_extraccion": 0.88
                }
                """;

        ResultadoExtraccionGemini resultado = ParseadorRespuestaGemini.parsear(json);

        assertEquals("Gripe", resultado.diagnostico());
        assertEquals(3, resultado.reposoDias());
        assertEquals("Dr. Pérez", resultado.medicoNombre());
        assertEquals("MN-999", resultado.medicoMatricula());
        assertEquals(0.88f, resultado.confianzaExtraccion(), 0.001f);
        assertNull(resultado.error());
    }

    @Test
    void parseaErrorDocumentoNoReconocido() {
        ResultadoExtraccionGemini resultado = ParseadorRespuestaGemini.parsear(
                "{\"error\": \"documento_no_reconocido\"}");

        assertTrue(resultado.tieneErrorDocumento());
        assertEquals("documento_no_reconocido", resultado.error());
    }

    @Test
    void extraeJsonDesdeMarkdown() {
        String markdown = """
                ```json
                {"diagnostico":"Resfrío","reposo_dias":2,"fecha_emision":"2026-06-15","confianza_extraccion":0.7}
                ```
                """;

        ResultadoExtraccionGemini resultado = ParseadorRespuestaGemini.parsear(markdown);

        assertEquals("Resfrío", resultado.diagnostico());
        assertEquals(2, resultado.reposoDias());
    }

    @Test
    void rechazaTextoSinJson() {
        assertThrows(ExcepcionNegocio.class,
                () -> ParseadorRespuestaGemini.parsear("esto no es json"));
    }

    @Test
    void rechazaRespuestaVacia() {
        assertThrows(ExcepcionNegocio.class,
                () -> ParseadorRespuestaGemini.parsear("   "));
    }
}
