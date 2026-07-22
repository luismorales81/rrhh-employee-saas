package com.rrhh.saas.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class UtilidadPruebasApi {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UtilidadPruebasApi() {
    }

    public static String obtenerToken(MockMvc mockMvc, String email, String password) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andReturn();
        JsonNode json = MAPPER.readTree(resultado.getResponse().getContentAsString());
        return json.get("tokenAcceso").asText();
    }
}
