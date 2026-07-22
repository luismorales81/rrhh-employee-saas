package com.rrhh.saas.vacacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.util.UtilidadPruebasApi;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CircuitoVacacionesIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void simularFechaInvalida_devuelveFechaSugerida() throws Exception {
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        LocalDate martes = LocalDate.now().plusMonths(2).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        mockMvc.perform(post("/api/vacaciones/simular-fecha")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicioDeseada":"%s","diasDeseados":7}
                                """.formatted(martes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esValida").value(false))
                .andExpect(jsonPath("$.fechaSugerida").exists());
    }

    @Test
    @Order(2)
    void solicitarFraccionMenorA7_devuelve422() throws Exception {
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        LocalDate lunes = LocalDate.now().plusMonths(3).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate miercoles = lunes.plusDays(2);

        mockMvc.perform(post("/api/vacaciones/solicitudes")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio":"%s","fechaFin":"%s","motivo":"Fracción corta"}
                                """.formatted(lunes, miercoles)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    @Order(3)
    void solicitarAprobarConFechasDistintasYActualizarSaldo() throws Exception {
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");

        MvcResult saldoInicialResult = mockMvc.perform(get("/api/vacaciones/saldo/propio")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode saldoInicial = MAPPER.readTree(saldoInicialResult.getResponse().getContentAsString());
        BigDecimal disponiblesAntes = saldoInicial.get("diasDisponibles").decimalValue();

        LocalDate inicio = LocalDate.now().plusMonths(3).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate fin = inicio.plusDays(6);
        LocalDate inicioAprobado = inicio.plusWeeks(1);
        LocalDate finAprobado = inicioAprobado.plusDays(6);

        MvcResult solicitudResult = mockMvc.perform(post("/api/vacaciones/solicitudes")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio":"%s","fechaFin":"%s","motivo":"Prueba integración LCT"}
                                """.formatted(inicio, fin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andReturn();

        Long solicitudVacacionesId = MAPPER.readTree(solicitudResult.getResponse().getContentAsString())
                .get("solicitudVacacionesId").asLong();

        mockMvc.perform(patch("/api/vacaciones/solicitudes/" + solicitudVacacionesId + "/aprobar")
                        .header("Authorization", "Bearer " + tokenRrhh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicioAprobada":"%s","fechaFinAprobada":"%s","comentarios":"Fechas ajustadas"}
                                """.formatted(inicioAprobado, finAprobado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaInicioAprobada").value(inicioAprobado.toString()))
                .andExpect(jsonPath("$.fechaFinAprobada").value(finAprobado.toString()))
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        MvcResult saldoFinalResult = mockMvc.perform(get("/api/vacaciones/saldo/propio")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode saldoFinal = MAPPER.readTree(saldoFinalResult.getResponse().getContentAsString());
        BigDecimal disponiblesDespues = saldoFinal.get("diasDisponibles").decimalValue();

        assertTrue(disponiblesDespues.compareTo(disponiblesAntes) < 0);
    }

    @Test
    @Order(4)
    void contrapropuestaAceptadaPorEmpleado_vuelveAEnRevision() throws Exception {
        String tokenEmpleado = UtilidadPruebasApi.obtenerToken(mockMvc, "empleado@demo.local", "Demo123!");
        String tokenRrhh = UtilidadPruebasApi.obtenerToken(mockMvc, "rrhh@demo.local", "Demo123!");

        LocalDate inicio = LocalDate.now().plusMonths(5).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate fin = inicio.plusDays(6);
        LocalDate inicioContra = inicio.plusWeeks(2);
        LocalDate finContra = inicioContra.plusDays(6);

        MvcResult solicitudResult = mockMvc.perform(post("/api/vacaciones/solicitudes")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio":"%s","fechaFin":"%s","motivo":"Flujo contrapropuesta"}
                                """.formatted(inicio, fin)))
                .andExpect(status().isCreated())
                .andReturn();

        Long solicitudVacacionesId = MAPPER.readTree(solicitudResult.getResponse().getContentAsString())
                .get("solicitudVacacionesId").asLong();

        mockMvc.perform(patch("/api/vacaciones/solicitudes/" + solicitudVacacionesId + "/contraproponer")
                        .header("Authorization", "Bearer " + tokenRrhh)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Mejor en otras fechas","fechaInicioContrapropuesta":"%s","fechaFinContrapropuesta":"%s"}
                                """.formatted(inicioContra, finContra)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CAMBIOS_SOLICITADOS"));

        mockMvc.perform(post("/api/vacaciones/solicitudes/" + solicitudVacacionesId + "/aceptar-contrapropuesta")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.fechaInicio").value(inicioContra.toString()))
                .andExpect(jsonPath("$.fechaFin").value(finContra.toString()));

        mockMvc.perform(post("/api/vacaciones/solicitudes/" + solicitudVacacionesId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }
}
