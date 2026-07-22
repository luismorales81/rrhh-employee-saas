package com.rrhh.saas.archivo.almacenamiento;

import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "app.almacenamiento.proveedor", havingValue = "disco", matchIfMissing = true)
public class AdaptadorAlmacenamientoDisco implements PuertoAlmacenamientoArchivos {

    private static final Pattern PATRON_CLAVE = Pattern.compile("^empresa/(\\d+)/archivos/(.+)$");

    private final PropiedadesAlmacenamiento propiedades;

    public AdaptadorAlmacenamientoDisco(PropiedadesAlmacenamiento propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public void guardar(String clave, InputStream contenido, long tamano, String tipoMime) {
        Path destino = resolverRutaNueva(clave);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo guardar el archivo en disco");
        }
    }

    @Override
    public InputStream leer(String clave) {
        Path ruta = resolverRutaLectura(clave);
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            throw new ExcepcionNegocio("Archivo no disponible en disco", 404);
        }
        try {
            return Files.newInputStream(ruta);
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo desde disco", 404);
        }
    }

    @Override
    public boolean existe(String clave) {
        return Files.exists(resolverRutaLectura(clave));
    }

    private Path resolverRutaNueva(String clave) {
        String[] partes = clave.split("/");
        Path ruta = Path.of(propiedades.getRutaBase());
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                ruta = ruta.resolve(parte);
            }
        }
        return ruta;
    }

    /**
     * Soporta layout nuevo (empresa/id/archivos/file) y legacy (rutaBase/id/file o ruta absoluta).
     */
    private Path resolverRutaLectura(String clave) {
        if (!clave.startsWith("empresa/")) {
            Path rutaAbsoluta = Path.of(clave);
            if (Files.exists(rutaAbsoluta)) {
                return rutaAbsoluta;
            }
        }
        Path nueva = resolverRutaNueva(clave);
        if (Files.exists(nueva)) {
            return nueva;
        }
        Matcher coincidencia = PATRON_CLAVE.matcher(clave);
        if (coincidencia.matches()) {
            Path legacy = Path.of(propiedades.getRutaBase(), coincidencia.group(1), coincidencia.group(2));
            if (Files.exists(legacy)) {
                return legacy;
            }
        }
        return nueva;
    }
}
