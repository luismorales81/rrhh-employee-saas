package com.rrhh.saas.archivo.servicio;

import com.rrhh.saas.archivo.almacenamiento.PuertoAlmacenamientoArchivos;
import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.dto.RespuestaArchivoAlmacenado;
import com.rrhh.saas.archivo.repositorio.RepositorioArchivoAlmacenado;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ServicioAlmacenamiento {

    private final RepositorioArchivoAlmacenado repositorio;
    private final RepositorioRecibo repositorioRecibo;
    private final ServicioEmpresa servicioEmpresa;
    private final PropiedadesAlmacenamiento propiedades;
    private final PuertoAlmacenamientoArchivos puertoAlmacenamiento;

    public ServicioAlmacenamiento(RepositorioArchivoAlmacenado repositorio,
                                  RepositorioRecibo repositorioRecibo,
                                  ServicioEmpresa servicioEmpresa,
                                  PropiedadesAlmacenamiento propiedades,
                                  PuertoAlmacenamientoArchivos puertoAlmacenamiento) {
        this.repositorio = repositorio;
        this.repositorioRecibo = repositorioRecibo;
        this.servicioEmpresa = servicioEmpresa;
        this.propiedades = propiedades;
        this.puertoAlmacenamiento = puertoAlmacenamiento;
    }

    @Transactional
    public RespuestaArchivoAlmacenado guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ExcepcionNegocio("El archivo está vacío");
        }
        if (archivo.getSize() > propiedades.getTamanoMaximoBytes()) {
            throw new ExcepcionNegocio("El archivo supera el tamaño máximo permitido");
        }

        String tipoMime = archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream";
        ValidadorArchivo.validarTipoMimePermitido(tipoMime);

        Empresa empresa = obtenerEmpresaActual();
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo");
        }
        ValidadorArchivo.validarContenidoContraTipoMime(tipoMime, contenido);

        return persistirArchivo(
                empresa,
                ValidadorArchivo.sanitizarNombreArchivo(archivo.getOriginalFilename()),
                tipoMime,
                contenido
        );
    }

    @Transactional
    public RespuestaArchivoAlmacenado guardarDesdeBase64(String nombreOriginal, String tipoMime, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new ExcepcionNegocio("El contenido está vacío");
        }
        if (contenido.length > propiedades.getTamanoMaximoBytes()) {
            throw new ExcepcionNegocio("El archivo supera el tamaño máximo permitido");
        }

        ValidadorArchivo.validarTipoMimePermitido(tipoMime);
        ValidadorArchivo.validarContenidoContraTipoMime(tipoMime, contenido);

        Empresa empresa = obtenerEmpresaActual();
        return persistirArchivo(
                empresa,
                ValidadorArchivo.sanitizarNombreArchivo(nombreOriginal),
                tipoMime,
                contenido
        );
    }

    @Transactional
    public void eliminarLogico(Long id) {
        ArchivoAlmacenado archivo = obtenerEntidad(id);
        if (repositorioRecibo.existsByArchivo_IdAndDeletedFalse(id)) {
            throw new ExcepcionNegocio(
                    "No se puede eliminar un archivo referenciado por un recibo activo", 400);
        }
        archivo.setDeleted(true);
        archivo.setDeletedAt(java.time.LocalDateTime.now());
        repositorio.save(archivo);
    }

    @Transactional(readOnly = true)
    public ArchivoAlmacenado obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Archivo no encontrado", 404));
    }

    @Transactional(readOnly = true)
    public Resource obtenerContenido(Long id) {
        ArchivoAlmacenado archivo = obtenerEntidad(id);
        InputStream flujo = puertoAlmacenamiento.leer(archivo.getClaveAlmacenamiento());
        return new InputStreamResource(flujo) {
            @Override
            public long contentLength() {
                return archivo.getTamano();
            }
        };
    }

    @Transactional(readOnly = true)
    public byte[] leerBytes(Long id) {
        ArchivoAlmacenado archivo = obtenerEntidad(id);
        try (InputStream inputStream = puertoAlmacenamiento.leer(archivo.getClaveAlmacenamiento())) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el contenido del archivo");
        }
    }

    public String construirUrlContenido(Long id) {
        return "/api/archivos/" + id + "/contenido";
    }

    @Transactional(readOnly = true)
    public String calcularHashArchivo(Long id) {
        ArchivoAlmacenado archivo = obtenerEntidad(id);
        try (InputStream inputStream = puertoAlmacenamiento.leer(archivo.getClaveAlmacenamiento())) {
            return calcularHashSha256(inputStream);
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo para verificar integridad");
        }
    }

    private RespuestaArchivoAlmacenado persistirArchivo(Empresa empresa, String nombreOriginal,
                                                        String tipoMime, byte[] contenido) {
        String hash = calcularHashSha256(contenido);
        String extension = obtenerExtension(nombreOriginal);
        String nombreArchivo = UUID.randomUUID() + extension;
        String clave = construirClave(empresa.getId(), nombreArchivo);

        puertoAlmacenamiento.guardar(clave, new ByteArrayInputStream(contenido), contenido.length, tipoMime);

        ArchivoAlmacenado entidad = new ArchivoAlmacenado();
        entidad.setEmpresa(empresa);
        entidad.setNombreOriginal(nombreOriginal != null ? nombreOriginal : nombreArchivo);
        entidad.setClaveAlmacenamiento(clave);
        entidad.setTipoMime(tipoMime);
        entidad.setTamano(contenido.length);
        entidad.setHashSha256(hash);
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId != null) {
            entidad.setCreatedBy(usuarioId);
        }

        return mapear(repositorio.save(entidad));
    }

    public static String construirClave(Long empresaId, String nombreArchivo) {
        return "empresa/" + empresaId + "/archivos/" + nombreArchivo;
    }

    private RespuestaArchivoAlmacenado mapear(ArchivoAlmacenado archivo) {
        return new RespuestaArchivoAlmacenado(
                archivo.getId(),
                archivo.getNombreOriginal(),
                archivo.getTipoMime(),
                archivo.getTamano(),
                archivo.getHashSha256(),
                construirUrlContenido(archivo.getId())
        );
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf('.'));
    }

    private String calcularHashSha256(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, leidos);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new ExcepcionNegocio("Error al calcular hash del archivo");
        }
    }

    private String calcularHashSha256(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(contenido));
        } catch (NoSuchAlgorithmException e) {
            throw new ExcepcionNegocio("Error al calcular hash del archivo");
        }
    }
}
