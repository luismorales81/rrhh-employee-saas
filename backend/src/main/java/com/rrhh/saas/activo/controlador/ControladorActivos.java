package com.rrhh.saas.activo.controlador;

import com.rrhh.saas.activo.dto.RespuestaActivo;
import com.rrhh.saas.activo.dto.RespuestaAsignacionActivo;
import com.rrhh.saas.activo.dto.SolicitudAsignarActivo;
import com.rrhh.saas.activo.dto.SolicitudCrearActivo;
import com.rrhh.saas.activo.dto.SolicitudDevolverActivo;
import com.rrhh.saas.activo.servicio.ServicioActivo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activos")
public class ControladorActivos {

    private final ServicioActivo servicioActivo;

    public ControladorActivos(ServicioActivo servicioActivo) {
        this.servicioActivo = servicioActivo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('activo.ver')")
    public ResponseEntity<List<RespuestaActivo>> listar() {
        return ResponseEntity.ok(servicioActivo.listar());
    }

    @GetMapping("/asignaciones/empleado/{empleadoId}")
    @PreAuthorize("hasAuthority('activo.ver')")
    public ResponseEntity<List<RespuestaAsignacionActivo>> listarAsignacionesEmpleado(
            @PathVariable Long empleadoId) {
        return ResponseEntity.ok(servicioActivo.listarAsignacionesEmpleado(empleadoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('activo.ver')")
    public ResponseEntity<RespuestaActivo> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioActivo.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('activo.gestionar')")
    public ResponseEntity<RespuestaActivo> crear(@Valid @RequestBody SolicitudCrearActivo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioActivo.crear(solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('activo.gestionar')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioActivo.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('activo.gestionar')")
    public ResponseEntity<RespuestaAsignacionActivo> asignar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAsignarActivo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioActivo.asignar(id, solicitud));
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAuthority('activo.gestionar')")
    public ResponseEntity<RespuestaAsignacionActivo> devolver(
            @PathVariable Long id,
            @RequestBody(required = false) SolicitudDevolverActivo solicitud) {
        SolicitudDevolverActivo datos = solicitud != null ? solicitud : new SolicitudDevolverActivo(null, null);
        return ResponseEntity.ok(servicioActivo.devolver(id, datos));
    }

    @GetMapping("/{id}/asignaciones")
    @PreAuthorize("hasAuthority('activo.ver')")
    public ResponseEntity<List<RespuestaAsignacionActivo>> listarAsignaciones(@PathVariable Long id) {
        return ResponseEntity.ok(servicioActivo.listarAsignacionesActivo(id));
    }
}
