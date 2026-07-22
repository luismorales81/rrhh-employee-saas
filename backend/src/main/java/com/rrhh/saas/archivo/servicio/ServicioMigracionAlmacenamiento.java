package com.rrhh.saas.archivo.servicio;

import com.rrhh.saas.archivo.almacenamiento.AdaptadorAlmacenamientoDisco;
import com.rrhh.saas.archivo.almacenamiento.AdaptadorAlmacenamientoS3;
import com.rrhh.saas.archivo.almacenamiento.PuertoAlmacenamientoArchivos;
import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.repositorio.RepositorioArchivoAlmacenado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/**
 * Migra blobs desde disco local hacia almacenamiento S3 (MinIO o R2).
 * Perfil Spring: migracion. Activar con --migrar-almacenamiento=true
 */
@Service
@Profile("migracion")
public class ServicioMigracionAlmacenamiento {

    private static final Logger log = LoggerFactory.getLogger(ServicioMigracionAlmacenamiento.class);

    private final RepositorioArchivoAlmacenado repositorio;
    private final PuertoAlmacenamientoArchivos origenDisco;
    private final PuertoAlmacenamientoArchivos destinoS3;

    public ServicioMigracionAlmacenamiento(RepositorioArchivoAlmacenado repositorio,
                                           PropiedadesAlmacenamiento propiedades) {
        this.repositorio = repositorio;
        this.origenDisco = new AdaptadorAlmacenamientoDisco(propiedades);
        this.destinoS3 = new AdaptadorAlmacenamientoS3(propiedades);
    }

    @Transactional(readOnly = true)
    public ResultadoMigracion ejecutar() {
        List<ArchivoAlmacenado> archivos = repositorio.findByDeletedFalse();
        int exitos = 0;
        int omitidos = 0;
        int fallos = 0;

        for (ArchivoAlmacenado archivo : archivos) {
            String clave = archivo.getClaveAlmacenamiento();
            try {
                if (destinoS3.existe(clave)) {
                    log.info("Omitido (ya existe en S3): id={} clave={}", archivo.getId(), clave);
                    omitidos++;
                    continue;
                }
                if (!origenDisco.existe(clave)) {
                    log.warn("No encontrado en disco: id={} clave={}", archivo.getId(), clave);
                    fallos++;
                    continue;
                }
                try (InputStream contenido = origenDisco.leer(clave)) {
                    byte[] bytes = contenido.readAllBytes();
                    String hashOrigen = calcularHash(bytes);
                    if (!hashOrigen.equals(archivo.getHashSha256())) {
                        log.error("Hash en disco no coincide con BD: id={} clave={}", archivo.getId(), clave);
                        fallos++;
                        continue;
                    }
                    destinoS3.guardar(clave, new java.io.ByteArrayInputStream(bytes), bytes.length, archivo.getTipoMime());
                    String hashDestino = calcularHash(bytes);
                    if (!hashDestino.equals(archivo.getHashSha256())) {
                        log.error("Hash destino no coincide tras subida: id={}", archivo.getId());
                        fallos++;
                        continue;
                    }
                    log.info("Migrado OK: id={} clave={}", archivo.getId(), clave);
                    exitos++;
                }
            } catch (Exception e) {
                log.error("Error migrando id={} clave={}: {}", archivo.getId(), clave, e.getMessage());
                fallos++;
            }
        }

        return new ResultadoMigracion(archivos.size(), exitos, omitidos, fallos);
    }

    private String calcularHash(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(contenido));
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular hash", e);
        }
    }

    public record ResultadoMigracion(int total, int exitos, int omitidos, int fallos) {
    }
}
