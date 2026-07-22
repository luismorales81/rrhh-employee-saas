package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorAutenticacionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginSuperAdminExitoso() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@rrhh.local","password":"Admin123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenAcceso").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@rrhh.local"))
                .andExpect(jsonPath("$.empresaNombre").value(nullValue()));
    }

    @Test
    void loginUsuarioEmpresaIncluyeNombreEmpresa() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"rrhh@demo.local","password":"Demo123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").isNumber())
                .andExpect(jsonPath("$.empresaNombre").isNotEmpty());
    }

    @Test
    void crearEmpresaRequiereAutenticacion() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@rrhh.local","password":"Admin123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String respuesta = login.getResponse().getContentAsString();
        String token = respuesta.split("\"tokenAcceso\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Empresa Test","razonSocial":"Test SA","cuit":"30-12345678-9"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Empresa Test"));

        mockMvc.perform(get("/api/empresas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Empresa Test')]").exists());
    }
}
