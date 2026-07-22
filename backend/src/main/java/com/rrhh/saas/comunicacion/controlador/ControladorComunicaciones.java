package com.rrhh.saas.comunicacion.controlador;

import com.rrhh.saas.comunicacion.dto.RespuestaComunicacion;
import com.rrhh.saas.comunicacion.dto.RespuestaDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dto.SolicitudPublicarComunicacion;
import com.rrhh.saas.comunicacion.servicio.ServicioComunicacion;
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
@RequestMapping("/api/comunicaciones")
public class ControladorComunicaciones {

    private final ServicioComunicacion servicioComunicacion;

    public ControladorComunicaciones(ServicioComunicacion servicioComunicacion) {
        this.servicioComunicacion = servicioComunicacion;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('comunicacion.publicar')")
    public ResponseEntity<RespuestaComunicacion> publicar(
            @Valid @RequestBody SolicitudPublicarComunicacion solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioComunicacion.publicar(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('comunicacion.ver')")
    public ResponseEntity<List<RespuestaComunicacion>> listar() {
        return ResponseEntity.ok(servicioComunicacion.listar());
    }

    @GetMapping("/programadas")
    @PreAuthorize("hasAuthority('comunicacion.ver')")
    public ResponseEntity<List<RespuestaComunicacion>> listarProgramadas() {
        return ResponseEntity.ok(servicioComunicacion.listarProgramadas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('comunicacion.ver')")
    public ResponseEntity<RespuestaComunicacion> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioComunicacion.obtenerPorId(id));
    }

    @GetMapping("/{id}/destinatarios")
    @PreAuthorize("hasAuthority('comunicacion.destinatarios.ver')")
    public ResponseEntity<List<RespuestaDestinatarioComunicacion>> listarDestinatarios(@PathVariable Long id) {
        return ResponseEntity.ok(servicioComunicacion.listarDestinatarios(id));
    }

    @GetMapping("/mis-comunicaciones")
    @PreAuthorize("hasAuthority('comunicacion.mis.ver')")
    public ResponseEntity<List<RespuestaDestinatarioComunicacion>> listarMisComunicaciones() {
        return ResponseEntity.ok(servicioComunicacion.listarMisComunicaciones());
    }

    @GetMapping("/destinatarios/{id}")
    @PreAuthorize("hasAuthority('comunicacion.destinatarios.ver') or hasAuthority('comunicacion.mis.ver')")
    public ResponseEntity<RespuestaDestinatarioComunicacion> obtenerDestinatario(@PathVariable Long id) {
        return ResponseEntity.ok(servicioComunicacion.obtenerDestinatarioPorId(id));
    }

    @PostMapping("/destinatarios/{id}/confirmar-lectura")
    @PreAuthorize("hasAuthority('comunicacion.confirmar_lectura')")
    public ResponseEntity<RespuestaDestinatarioComunicacion> confirmarLectura(@PathVariable Long id) {
        return ResponseEntity.ok(servicioComunicacion.confirmarLectura(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('comunicacion.publicar')")
    public ResponseEntity<RespuestaComunicacion> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioComunicacion.cancelarProgramada(id));
    }
}
