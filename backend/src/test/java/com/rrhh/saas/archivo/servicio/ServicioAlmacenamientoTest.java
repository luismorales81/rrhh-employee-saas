package com.rrhh.saas.archivo.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.archivo.dto.RespuestaArchivoAlmacenado;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioAlmacenamientoTest {

    @Autowired
    private ServicioAlmacenamiento servicioAlmacenamiento;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @BeforeEach
    void establecerContexto() {
        Empresa empresa = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> "Demo SA".equals(e.getNombre()))
                .findFirst()
                .orElseThrow();
        ContextoTenant.establecerEmpresaId(empresa.getId());
    }

    @AfterEach
    void limpiarContexto() {
        ContextoTenant.limpiar();
    }

    @Test
    void subirDescargarYCalcularHash() throws Exception {
        byte[] contenido = "%PDF-1.4 prueba".getBytes();
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "prueba.pdf", "application/pdf", contenido);

        RespuestaArchivoAlmacenado respuesta = servicioAlmacenamiento.guardar(archivo);

        assertNotNull(respuesta.id());
        assertEquals("prueba.pdf", respuesta.nombreOriginal());
        assertEquals(contenido.length, respuesta.tamano());
        assertNotNull(respuesta.hashSha256());
        assertEquals(64, respuesta.hashSha256().length());

        Resource recurso = servicioAlmacenamiento.obtenerContenido(respuesta.id());
        assertTrue(recurso.contentLength() > 0);
    }
}
