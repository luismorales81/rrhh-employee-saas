package com.rrhh.saas.comunicacion.controlador;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comunicacion.dominio.EstadoComunicacion;
import com.rrhh.saas.comunicacion.repositorio.RepositorioComunicacion;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorComunicacionCancelarTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioComunicacion repositorioComunicacion;

    @Test
    void cancelarComunicacionProgramada() throws Exception {
        String token = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");

        MvcResult creada = mockMvc.perform(post("/api/comunicaciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Comunicación a cancelar",
                                  "mensaje": "Mensaje de prueba",
                                  "tipoDestinatario": "EMPRESA",
                                  "requiereConfirmacion": false,
                                  "fechaPublicacionProgramada": "%s"
                                }
                                """.formatted(LocalDateTime.now().plusDays(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andReturn();

        Long id = MAPPER.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/comunicaciones/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        assertEquals(EstadoComunicacion.CANCELADA,
                repositorioComunicacion.findById(id).orElseThrow().getEstado());
    }
}
