package com.rrhh.saas.nucleo.controlador;

import com.rrhh.saas.nucleo.dto.RespuestaRol;
import com.rrhh.saas.nucleo.dto.SolicitudActualizarRol;
import com.rrhh.saas.nucleo.dto.SolicitudCrearRol;
import com.rrhh.saas.nucleo.servicio.ServicioRol;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class ControladorRol {

    private final ServicioRol servicioRol;

    public ControladorRol(ServicioRol servicioRol) {
        this.servicioRol = servicioRol;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rol.crear')")
    public ResponseEntity<RespuestaRol> crear(@Valid @RequestBody SolicitudCrearRol solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioRol.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('rol.ver')")
    public ResponseEntity<List<RespuestaRol>> listar() {
        return ResponseEntity.ok(servicioRol.listarPorEmpresa());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('rol.ver')")
    public ResponseEntity<RespuestaRol> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioRol.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rol.crear')")
    public ResponseEntity<RespuestaRol> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody SolicitudActualizarRol solicitud) {
        return ResponseEntity.ok(servicioRol.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rol.crear')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioRol.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
