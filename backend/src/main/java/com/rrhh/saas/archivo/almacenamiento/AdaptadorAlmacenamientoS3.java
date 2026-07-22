package com.rrhh.saas.archivo.almacenamiento;

import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;

@Component
@ConditionalOnProperty(name = "app.almacenamiento.proveedor", havingValue = "s3")
public class AdaptadorAlmacenamientoS3 implements PuertoAlmacenamientoArchivos {

    private final PropiedadesAlmacenamiento propiedades;
    private final S3Client clienteS3;

    public AdaptadorAlmacenamientoS3(PropiedadesAlmacenamiento propiedades) {
        this.propiedades = propiedades;
        this.clienteS3 = construirCliente();
    }

    @Override
    public void guardar(String clave, InputStream contenido, long tamano, String tipoMime) {
        try {
            PutObjectRequest solicitud = PutObjectRequest.builder()
                    .bucket(propiedades.getS3().getBucket())
                    .key(clave)
                    .contentType(tipoMime)
                    .contentLength(tamano)
                    .build();
            clienteS3.putObject(solicitud, RequestBody.fromInputStream(contenido, tamano));
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo guardar el archivo en almacenamiento S3");
        }
    }

    @Override
    public InputStream leer(String clave) {
        try {
            GetObjectRequest solicitud = GetObjectRequest.builder()
                    .bucket(propiedades.getS3().getBucket())
                    .key(clave)
                    .build();
            return clienteS3.getObject(solicitud);
        } catch (NoSuchKeyException e) {
            throw new ExcepcionNegocio("Archivo no disponible en almacenamiento S3", 404);
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo desde almacenamiento S3", 404);
        }
    }

    @Override
    public boolean existe(String clave) {
        try {
            HeadObjectRequest solicitud = HeadObjectRequest.builder()
                    .bucket(propiedades.getS3().getBucket())
                    .key(clave)
                    .build();
            clienteS3.headObject(solicitud);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new ExcepcionNegocio("Error al verificar existencia del archivo en S3");
        }
    }

    @PreDestroy
    public void cerrar() {
        if (clienteS3 != null) {
            clienteS3.close();
        }
    }

    private S3Client construirCliente() {
        PropiedadesAlmacenamiento.PropiedadesS3 s3 = propiedades.getS3();
        if (s3.getEndpoint() == null || s3.getEndpoint().isBlank()) {
            throw new ExcepcionNegocio("Falta app.almacenamiento.s3.endpoint para proveedor s3");
        }
        if (s3.getAccessKey() == null || s3.getSecretKey() == null) {
            throw new ExcepcionNegocio("Faltan credenciales S3 (access-key / secret-key)");
        }

        S3Configuration.Builder configBuilder = S3Configuration.builder();
        if (s3.isPathStyle()) {
            configBuilder.pathStyleAccessEnabled(true);
        }

        return S3Client.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())))
                .serviceConfiguration(configBuilder.build())
                .build();
    }
}
