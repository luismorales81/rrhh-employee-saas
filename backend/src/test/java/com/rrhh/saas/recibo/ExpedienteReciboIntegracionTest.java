package com.rrhh.saas.recibo;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpedienteReciboIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportarExpedienteZip() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");

        var listado = mockMvc.perform(get("/api/recibos")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andReturn();

        String cuerpo = listado.getResponse().getContentAsString();
        int inicioId = cuerpo.indexOf("\"id\":") + 5;
        int finId = cuerpo.indexOf(',', inicioId);
        Long reciboId = Long.parseLong(cuerpo.substring(inicioId, finId).trim());

        byte[] zip = mockMvc.perform(get("/api/recibos/" + reciboId + "/expediente")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(zip.length > 4);
        assertTrue(zip[0] == 'P' && zip[1] == 'K');
    }
}
