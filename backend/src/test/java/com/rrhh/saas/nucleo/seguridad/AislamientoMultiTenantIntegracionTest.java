package com.rrhh.saas.nucleo.seguridad;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RrhhSaasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AislamientoMultiTenantIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void tenantSoloVeSuEmpresaYNoCreaUsuariosEnOtroTenant() throws Exception {
        String tokenSuperAdmin = UtilidadPruebasApi.obtenerToken(mockMvc, "admin@rrhh.local", "Admin123!");

        MvcResult empresaDemoResult = mockMvc.perform(get("/api/empresas")
                        .header("Authorization", "Bearer " + tokenSuperAdmin))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode empresas = MAPPER.readTree(empresaDemoResult.getResponse().getContentAsString());
        Long empresaDemoId = null;
        for (JsonNode empresa : empresas) {
            if ("Demo SA".equals(empresa.get("nombre").asText())) {
                empresaDemoId = empresa.get("id").asLong();
                break;
            }
        }
        assertTrue(empresaDemoId != null, "Debe existir la empresa demo");

        MvcResult empresaBResult = mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + tokenSuperAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Empresa B Test",
                                  "razonSocial": "Empresa B Test SA",
                                  "cuit": "30999999993",
                                  "email": "contacto@empresab.test",
                                  "telefono": "011-0000-0000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Long empresaBId = MAPPER.readTree(empresaBResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult rolesResult = mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + tokenSuperAdmin)
                        .header("X-Empresa-Id", empresaDemoId)
                        .header("X-Modo-Soporte", "true"))
                .andExpect(status().isOk())
                .andReturn();
        Long rolAdministradorId = null;
        for (JsonNode rol : MAPPER.readTree(rolesResult.getResponse().getContentAsString())) {
            if ("Administrador".equals(rol.get("nombre").asText())) {
                rolAdministradorId = rol.get("id").asLong();
                break;
            }
        }
        assertTrue(rolAdministradorId != null, "Debe existir el rol Administrador");

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenSuperAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": %d,
                                  "email": "admin.demo@test.local",
                                  "password": "AdminDemo123!",
                                  "rolesIds": [%d]
                                }
                                """.formatted(empresaDemoId, rolAdministradorId)))
                .andExpect(status().isCreated());

        String tokenAdminDemo = UtilidadPruebasApi.obtenerToken(mockMvc, "admin.demo@test.local", "AdminDemo123!");

        MvcResult listadoTenant = mockMvc.perform(get("/api/empresas")
                        .header("Authorization", "Bearer " + tokenAdminDemo))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode empresasTenant = MAPPER.readTree(listadoTenant.getResponse().getContentAsString());
        assertEquals(1, empresasTenant.size());
        assertEquals(empresaDemoId, empresasTenant.get(0).get("id").asLong());

        mockMvc.perform(get("/api/empresas/" + empresaBId)
                        .header("Authorization", "Bearer " + tokenAdminDemo))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenAdminDemo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": %d,
                                  "email": "intruso@empresab.test",
                                  "password": "Intruso123!",
                                  "rolesIds": [%d]
                                }
                                """.formatted(empresaBId, rolAdministradorId)))
                .andExpect(status().isForbidden());

        MvcResult empleadosDemo = mockMvc.perform(get("/api/empleados")
                        .header("Authorization", "Bearer " + tokenAdminDemo))
                .andExpect(status().isOk())
                .andReturn();
        Long empleadoDemoId = MAPPER.readTree(empleadosDemo.getResponse().getContentAsString()).get(0).get("id").asLong();

        mockMvc.perform(get("/api/empleados/" + empleadoDemoId)
                        .header("Authorization", "Bearer " + tokenSuperAdmin)
                        .header("X-Empresa-Id", empresaBId)
                        .header("X-Modo-Soporte", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cerrarSesionRevocaTokensRefresco() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"rrhh@demo.local","password":"Demo123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sesion = MAPPER.readTree(login.getResponse().getContentAsString());
        String tokenAcceso = sesion.get("tokenAcceso").asText();
        String tokenRefresco = sesion.get("tokenRefresco").asText();

        mockMvc.perform(post("/api/auth/cerrar-sesion")
                        .header("Authorization", "Bearer " + tokenAcceso))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refrescar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tokenRefresco":"%s"}
                                """.formatted(tokenRefresco)))
                .andExpect(status().isUnauthorized());
    }
}
