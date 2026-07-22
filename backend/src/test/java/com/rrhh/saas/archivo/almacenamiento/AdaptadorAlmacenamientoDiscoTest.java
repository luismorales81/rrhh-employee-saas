package com.rrhh.saas.archivo.almacenamiento;

import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptadorAlmacenamientoDiscoTest {

    @TempDir
    Path directorioTemporal;

    private AdaptadorAlmacenamientoDisco adaptador;

    @BeforeEach
    void configurar() {
        PropiedadesAlmacenamiento propiedades = new PropiedadesAlmacenamiento();
        propiedades.setRutaBase(directorioTemporal.toString());
        adaptador = new AdaptadorAlmacenamientoDisco(propiedades);
    }

    @Test
    void guardarLeerYExiste_conClaveLogica() throws Exception {
        String clave = "empresa/1/archivos/prueba.txt";
        byte[] contenido = "hola almacenamiento".getBytes();

        adaptador.guardar(clave, new ByteArrayInputStream(contenido), contenido.length, "text/plain");

        assertTrue(adaptador.existe(clave));
        try (InputStream leido = adaptador.leer(clave)) {
            assertArrayEquals(contenido, leido.readAllBytes());
        }
    }

    @Test
    void existe_devuelveFalseSiNoHayArchivo() {
        assertFalse(adaptador.existe("empresa/99/archivos/inexistente.pdf"));
    }
}
