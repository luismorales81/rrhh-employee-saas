package com.rrhh.saas.archivo.controlador;

import com.rrhh.saas.archivo.dto.RespuestaArchivoAlmacenado;
import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.archivo.servicio.ServicioControlAccesoArchivo;
import com.rrhh.saas.archivo.servicio.ValidadorArchivo;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/archivos")
public class ControladorArchivos {

    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioControlAccesoArchivo servicioControlAccesoArchivo;

    public ControladorArchivos(ServicioAlmacenamiento servicioAlmacenamiento,
                               ServicioControlAccesoArchivo servicioControlAccesoArchivo) {
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioControlAccesoArchivo = servicioControlAccesoArchivo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('archivo.subir')")
    public ResponseEntity<RespuestaArchivoAlmacenado> subir(@RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAlmacenamiento.guardar(archivo));
    }

    @GetMapping("/{id}/contenido")
    @PreAuthorize("hasAuthority('archivo.descargar')")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        ArchivoAlmacenado entidad = servicioControlAccesoArchivo.obtenerParaDescargaHttp(id);
        Resource recurso = servicioAlmacenamiento.obtenerContenido(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entidad.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + ValidadorArchivo.sanitizarNombreArchivo(entidad.getNombreOriginal()) + "\"")
                .body(recurso);
    }
}
