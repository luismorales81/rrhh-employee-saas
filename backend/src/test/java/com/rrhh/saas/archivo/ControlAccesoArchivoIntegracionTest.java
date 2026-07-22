package com.rrhh.saas.archivo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControlAccesoArchivoIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void empleadoNoDescargaArchivoAjenoPorIdDirecto() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");

        MockMultipartFile archivoRrhh = new MockMultipartFile(
                "archivo", "secreto-rrhh.pdf", "application/pdf", "%PDF-1.4 secreto-rrhh".getBytes());

        MvcResult subida = mockMvc.perform(multipart("/api/archivos")
                        .file(archivoRrhh)
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isCreated())
                .andReturn();

        Long archivoIdAjeno = MAPPER.readTree(subida.getResponse().getContentAsString()).get("id").asLong();
        assertNotNull(archivoIdAjeno);

        mockMvc.perform(get("/api/archivos/" + archivoIdAjeno + "/contenido")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/archivos/" + archivoIdAjeno + "/contenido")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk());
    }

    @Test
    void empleadoDescargaArchivoQueSubioElMismo() throws Exception {
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");

        MockMultipartFile propio = new MockMultipartFile(
                "archivo", "mio.pdf", "application/pdf", "%PDF-1.4 propio".getBytes());

        MvcResult subida = mockMvc.perform(multipart("/api/archivos")
                        .file(propio)
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = MAPPER.readTree(subida.getResponse().getContentAsString());
        Long archivoId = json.get("id").asLong();
        assertTrue(archivoId > 0);

        mockMvc.perform(get("/api/archivos/" + archivoId + "/contenido")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk());
    }
}
