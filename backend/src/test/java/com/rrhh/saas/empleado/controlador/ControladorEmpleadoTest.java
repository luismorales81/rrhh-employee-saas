package com.rrhh.saas.empleado.controlador;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorEmpleadoTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actualizarYDesactivarEmpleado() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");

        MvcResult creadoResult = mockMvc.perform(post("/api/empleados")
                        .header("Authorization", "Bearer " + tokenRrhh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Test",
                                  "apellido": "Empleado",
                                  "fechaIngreso": "2024-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Long idEmpleado = MAPPER.readTree(creadoResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/empleados/" + idEmpleado)
                        .header("Authorization", "Bearer " + tokenRrhh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Test",
                                  "apellido": "Actualizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellido").value("Actualizado"));

        mockMvc.perform(delete("/api/empleados/" + idEmpleado)
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/empleados/" + idEmpleado)
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESVINCULADO"));

        mockMvc.perform(get("/api/empleados/" + idEmpleado + "/legajo")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoOffboarding").value("EN_PROGRESO"));

        MvcResult listaFinal = mockMvc.perform(get("/api/empleados")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andReturn();

        boolean sigueEnLista = false;
        for (JsonNode empleado : MAPPER.readTree(listaFinal.getResponse().getContentAsString())) {
            if (empleado.get("id").asLong() == idEmpleado) {
                sigueEnLista = true;
                assertTrue("DESVINCULADO".equals(empleado.get("estado").asText()));
            }
        }
        assertTrue(sigueEnLista);
    }
}
