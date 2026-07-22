package com.rrhh.saas.recibo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.repositorio.RepositorioArchivoAlmacenado;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoLegalRecibosIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioArchivoAlmacenado repositorioArchivo;

    @Test
    void flujoFelizPublicarVisualizarFirmarEventosYComprobante() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String periodo = "legal-" + (System.currentTimeMillis() % 100000);

        Long empleadoId = obtenerEmpleadoJuanId(tokenRrhh);
        Long archivoId = subirPdf(tokenRrhh);
        Long reciboId = publicarRecibo(tokenRrhh, empleadoId, periodo, archivoId);

        mockMvc.perform(post("/api/recibos/" + reciboId + "/visualizar")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VISTO"));

        mockMvc.perform(post("/api/recibos/" + reciboId + "/confirmar-recepcion")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"CONFIRMADO","comentario":null,"contrasena":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FIRMADO"));

        mockMvc.perform(get("/api/recibos/" + reciboId + "/eventos-firma")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].tipo").value("CONFIRMADO"));

        byte[] comprobante = mockMvc.perform(get("/api/recibos/" + reciboId + "/comprobante")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        JsonNode jsonComprobante = MAPPER.readTree(comprobante);
        assertEquals("COMPROBANTE_FIRMA_RECIBO", jsonComprobante.get("tipoDocumento").asText());
        assertEquals(periodo, jsonComprobante.get("periodo").asText());
    }

    @Test
    void rechazaDisconformidadSinComentario() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String periodo = "disc-" + (System.currentTimeMillis() % 100000);

        Long reciboId = publicarRecibo(
                tokenRrhh,
                obtenerEmpleadoJuanId(tokenRrhh),
                periodo,
                subirPdf(tokenRrhh)
        );

        mockMvc.perform(post("/api/recibos/" + reciboId + "/visualizar")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/recibos/" + reciboId + "/confirmar-recepcion")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"DISCONFORMIDAD","comentario":"corto","contrasena":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaDobleFirma() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String periodo = "doble-" + (System.currentTimeMillis() % 100000);

        Long reciboId = publicarRecibo(
                tokenRrhh,
                obtenerEmpleadoJuanId(tokenRrhh),
                periodo,
                subirPdf(tokenRrhh)
        );

        mockMvc.perform(post("/api/recibos/" + reciboId + "/visualizar")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk());

        String cuerpoFirma = """
                {"tipo":"CONFIRMADO","comentario":null,"contrasena":null}
                """;

        mockMvc.perform(post("/api/recibos/" + reciboId + "/confirmar-recepcion")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoFirma))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/recibos/" + reciboId + "/confirmar-recepcion")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoFirma))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaAccesoEmpleadoAjeno() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String periodo = "ajeno-" + (System.currentTimeMillis() % 100000);

        Long empleadoMariaId = obtenerEmpleadoPorNumero(tokenRrhh, "EMP-002");
        Long reciboId = publicarRecibo(tokenRrhh, empleadoMariaId, periodo, subirPdf(tokenRrhh));

        mockMvc.perform(get("/api/recibos/" + reciboId)
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaFirmaSiHashAlterado() throws Exception {
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String periodo = "hash-" + (System.currentTimeMillis() % 100000);

        Long archivoId = subirPdf(tokenRrhh);
        Long reciboId = publicarRecibo(
                tokenRrhh,
                obtenerEmpleadoJuanId(tokenRrhh),
                periodo,
                archivoId
        );

        ArchivoAlmacenado archivo = repositorioArchivo.findById(archivoId).orElseThrow();
        Path ruta = resolverRutaArchivo(archivo.getClaveAlmacenamiento());
        Files.writeString(ruta, "%PDF-ALTERADO-MALICIOSO%");

        mockMvc.perform(post("/api/recibos/" + reciboId + "/visualizar")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/recibos/" + reciboId + "/confirmar-recepcion")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"CONFIRMADO","comentario":null,"contrasena":null}
                                """))
                .andExpect(status().isConflict());
    }

    private Long subirPdf(String tokenRrhh) throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "archivo",
                "recibo-legal-test.pdf",
                "application/pdf",
                "%PDF-1.4 prueba flujo legal%".getBytes()
        );

        MvcResult resultado = mockMvc.perform(multipart("/api/archivos")
                        .file(pdf)
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isCreated())
                .andReturn();

        return MAPPER.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long publicarRecibo(String tokenRrhh, Long empleadoId, String periodo, Long archivoId)
            throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/recibos")
                        .header("Authorization", "Bearer " + tokenRrhh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empleadoId": %d,
                                  "periodo": "%s",
                                  "titulo": "Recibo prueba legal",
                                  "archivoId": %d,
                                  "montoNeto": 150000.00,
                                  "montoBruto": 200000.00
                                }
                                """.formatted(empleadoId, periodo, archivoId)))
                .andExpect(status().isCreated())
                .andReturn();

        return MAPPER.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long obtenerEmpleadoJuanId(String tokenRrhh) throws Exception {
        return obtenerEmpleadoPorNumero(tokenRrhh, "EMP-001");
    }

    private Long obtenerEmpleadoPorNumero(String tokenRrhh, String numeroEmpleado) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/empleados")
                        .header("Authorization", "Bearer " + tokenRrhh))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode lista = MAPPER.readTree(resultado.getResponse().getContentAsString());
        for (JsonNode empleado : lista) {
            if (numeroEmpleado.equals(empleado.get("numeroEmpleado").asText())) {
                return empleado.get("id").asLong();
            }
        }
        throw new IllegalStateException("Empleado no encontrado: " + numeroEmpleado);
    }

    private Path resolverRutaArchivo(String clave) throws Exception {
        Path base = Path.of("./target/test-archivos");
        Path directa = base.resolve(clave);
        if (Files.exists(directa)) {
            return directa;
        }
        String[] partes = clave.split("/");
        Path acumulada = base;
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                acumulada = acumulada.resolve(parte);
            }
        }
        if (Files.exists(acumulada)) {
            return acumulada;
        }
        return Files.walk(base)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".pdf"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró archivo en disco de prueba"));
    }
}
