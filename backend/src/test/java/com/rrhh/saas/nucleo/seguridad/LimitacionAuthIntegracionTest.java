package com.rrhh.saas.nucleo.seguridad;

import com.rrhh.saas.RrhhSaasApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LimitacionAuthIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioLimitacionIntentosAuth servicioLimitacionIntentosAuth;

    @Test
    void loginFallaConMensajeGenericoYRateLimit() throws Exception {
        String emailUnico = "ataque-rate-" + System.nanoTime() + "@demo.local";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"clave-incorrecta"}
                                """.formatted(emailUnico)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.50");
                                return request;
                            })
                            .content("""
                                    {"email":"%s","password":"clave-incorrecta"}
                                    """.formatted(emailUnico)))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.50");
                            return request;
                        })
                        .content("""
                                {"email":"%s","password":"clave-incorrecta"}
                                """.formatted(emailUnico)))
                .andExpect(status().isTooManyRequests());

        // Evitar contaminación entre tests
        servicioLimitacionIntentosAuth.registrarExitoLogin("203.0.113.50", emailUnico);
    }

    @Test
    void usuarioInactivoNoSeEnumera() throws Exception {
        // Si el email no existe o la cuenta está inactiva, siempre 401 genérico
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"no-existe-enum@demo.local","password":"Demo123!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
    }
}
