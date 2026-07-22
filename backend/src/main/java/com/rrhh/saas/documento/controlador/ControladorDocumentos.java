package com.rrhh.saas.documento.controlador;

import com.rrhh.saas.documento.dto.RespuestaEvidenciaFirma;
import com.rrhh.saas.documento.dto.RespuestaAsignacionDocumento;
import com.rrhh.saas.documento.dto.RespuestaCategoriaDocumento;
import com.rrhh.saas.documento.dto.RespuestaDocumento;
import com.rrhh.saas.documento.dto.SolicitudAsignarDocumento;
import com.rrhh.saas.documento.dto.SolicitudCrearCategoriaDocumento;
import com.rrhh.saas.documento.dto.SolicitudCrearDocumento;
import com.rrhh.saas.documento.dto.SolicitudFirmarDocumento;
import com.rrhh.saas.documento.servicio.ServicioAsignacionDocumento;
import com.rrhh.saas.documento.servicio.ServicioCategoriaDocumento;
import com.rrhh.saas.documento.servicio.ServicioDocumento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class ControladorDocumentos {

    private final ServicioCategoriaDocumento servicioCategoriaDocumento;
    private final ServicioDocumento servicioDocumento;
    private final ServicioAsignacionDocumento servicioAsignacionDocumento;

    public ControladorDocumentos(ServicioCategoriaDocumento servicioCategoriaDocumento,
                                 ServicioDocumento servicioDocumento,
                                 ServicioAsignacionDocumento servicioAsignacionDocumento) {
        this.servicioCategoriaDocumento = servicioCategoriaDocumento;
        this.servicioDocumento = servicioDocumento;
        this.servicioAsignacionDocumento = servicioAsignacionDocumento;
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAuthority('documento.categoria.gestionar')")
    public ResponseEntity<RespuestaCategoriaDocumento> crearCategoria(
            @Valid @RequestBody SolicitudCrearCategoriaDocumento solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCategoriaDocumento.crear(solicitud));
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('documento.categoria.ver') or hasAuthority('documento.gestionar')")
    public ResponseEntity<List<RespuestaCategoriaDocumento>> listarCategorias() {
        return ResponseEntity.ok(servicioCategoriaDocumento.listar());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('documento.gestionar')")
    public ResponseEntity<RespuestaDocumento> crearDocumento(
            @Valid @RequestBody SolicitudCrearDocumento solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioDocumento.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('documento.ver') or hasAuthority('documento.gestionar')")
    public ResponseEntity<List<RespuestaDocumento>> listarDocumentos() {
        return ResponseEntity.ok(servicioDocumento.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('documento.ver') or hasAuthority('documento.gestionar')")
    public ResponseEntity<RespuestaDocumento> obtenerDocumento(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDocumento.obtenerPorId(id));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('documento.asignar')")
    public ResponseEntity<List<RespuestaAsignacionDocumento>> asignar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAsignarDocumento solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAsignacionDocumento.asignar(id, solicitud));
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAuthority('documento.asignacion.ver')")
    public ResponseEntity<List<RespuestaAsignacionDocumento>> listarAsignaciones() {
        return ResponseEntity.ok(servicioAsignacionDocumento.listarPorEmpresa());
    }

    @GetMapping("/asignaciones/empleado/{empleadoId}")
    @PreAuthorize("hasAuthority('documento.asignacion.ver')")
    public ResponseEntity<List<RespuestaAsignacionDocumento>> listarAsignacionesEmpleado(
            @PathVariable Long empleadoId) {
        return ResponseEntity.ok(servicioAsignacionDocumento.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/mis-asignaciones")
    @PreAuthorize("hasAuthority('documento.mis.ver')")
    public ResponseEntity<List<RespuestaAsignacionDocumento>> listarMisAsignaciones() {
        return ResponseEntity.ok(servicioAsignacionDocumento.listarMisAsignaciones());
    }

    @GetMapping("/asignaciones/{id}")
    @PreAuthorize("hasAuthority('documento.asignacion.ver') or hasAuthority('documento.mis.ver')")
    public ResponseEntity<RespuestaAsignacionDocumento> obtenerAsignacion(@PathVariable Long id) {
        return ResponseEntity.ok(servicioAsignacionDocumento.obtenerPorId(id));
    }

    @PostMapping("/asignaciones/{id}/confirmar-lectura")
    @PreAuthorize("hasAuthority('documento.confirmar_lectura')")
    public ResponseEntity<RespuestaAsignacionDocumento> confirmarLectura(@PathVariable Long id) {
        return ResponseEntity.ok(servicioAsignacionDocumento.confirmarLectura(id));
    }

    @PostMapping("/asignaciones/{id}/firmar")
    @PreAuthorize("hasAuthority('documento.firmar')")
    public ResponseEntity<RespuestaAsignacionDocumento> firmar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) SolicitudFirmarDocumento solicitud,
            HttpServletRequest peticion) {
        SolicitudFirmarDocumento datos = solicitud != null
                ? solicitud
                : new SolicitudFirmarDocumento(null, null, null);
        return ResponseEntity.ok(servicioAsignacionDocumento.firmar(id, datos, peticion));
    }

    @GetMapping("/asignaciones/{id}/evidencia-firma")
    @PreAuthorize("hasAuthority('documento.firmar') or hasAuthority('documento.mis.ver')")
    public ResponseEntity<RespuestaEvidenciaFirma> obtenerEvidenciaFirma(@PathVariable Long id) {
        return ResponseEntity.ok(servicioAsignacionDocumento.obtenerEvidenciaFirma(id));
    }
}
