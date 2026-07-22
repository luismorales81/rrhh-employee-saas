package com.rrhh.saas.nucleo.controlador;

import com.rrhh.saas.nucleo.dto.RespuestaEmpresa;
import com.rrhh.saas.nucleo.dto.SolicitudActualizarEmpresa;
import com.rrhh.saas.nucleo.dto.SolicitudCrearEmpresa;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
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
@RequestMapping("/api/empresas")
public class ControladorEmpresa {

    private final ServicioEmpresa servicioEmpresa;

    public ControladorEmpresa(ServicioEmpresa servicioEmpresa) {
        this.servicioEmpresa = servicioEmpresa;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('empresa.crear')")
    public ResponseEntity<RespuestaEmpresa> crear(@Valid @RequestBody SolicitudCrearEmpresa solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioEmpresa.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('empresa.crear') or hasAuthority('empresa.ver')")
    public ResponseEntity<List<RespuestaEmpresa>> listar() {
        return ResponseEntity.ok(servicioEmpresa.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('empresa.crear') or hasAuthority('empresa.ver')")
    public ResponseEntity<RespuestaEmpresa> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioEmpresa.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('empresa.editar')")
    public ResponseEntity<RespuestaEmpresa> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudActualizarEmpresa solicitud) {
        return ResponseEntity.ok(servicioEmpresa.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('empresa.desactivar')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        servicioEmpresa.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
