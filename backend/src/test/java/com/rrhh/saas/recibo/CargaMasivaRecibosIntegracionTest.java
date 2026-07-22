package com.rrhh.saas.recibo;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CargaMasivaRecibosIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analizarYEjecutarCargaMasiva() throws Exception {
        String token = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String periodo = "mas-" + (System.currentTimeMillis() % 100000);
        MockMultipartFile zip = crearZipRecibos();

        mockMvc.perform(multipart("/api/recibos/carga-masiva/analizar")
                        .file(zip)
                        .param("periodo", periodo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArchivos").value(1))
                .andExpect(jsonPath("$.emparejados").value(1));

        MockMultipartFile zipEjecucion = crearZipRecibos();
        var resultado = mockMvc.perform(multipart("/api/recibos/carga-masiva")
                        .file(zipEjecucion)
                        .param("periodo", periodo)
                        .param("tituloBase", "Recibo masivo test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cargados.length()").value(1))
                .andReturn();

        assertTrue(resultado.getResponse().getContentAsString().contains("reciboId"));
    }

    private MockMultipartFile crearZipRecibos() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
            zip.putNextEntry(new ZipEntry("20301234567.pdf"));
            zip.write("%PDF-1.4 demo recibo Juan%".getBytes());
            zip.closeEntry();
        }
        return new MockMultipartFile("archivoZip", "recibos.zip", "application/zip", buffer.toByteArray());
    }
}
