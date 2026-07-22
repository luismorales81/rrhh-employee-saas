package com.rrhh.saas.notificacion.controlador;

import com.rrhh.saas.notificacion.dto.RespuestaNotificacion;
import com.rrhh.saas.notificacion.servicio.ServicioNotificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class ControladorNotificaciones {

    private final ServicioNotificacion servicioNotificacion;

    public ControladorNotificaciones(ServicioNotificacion servicioNotificacion) {
        this.servicioNotificacion = servicioNotificacion;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('notificacion.ver')")
    public ResponseEntity<List<RespuestaNotificacion>> listar() {
        return ResponseEntity.ok(servicioNotificacion.listarPropias());
    }

    @GetMapping("/no-leidas")
    @PreAuthorize("hasAuthority('notificacion.ver')")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        return ResponseEntity.ok(Map.of("cantidad", servicioNotificacion.contarNoLeidas()));
    }

    @PostMapping("/{id}/marcar-leida")
    @PreAuthorize("hasAuthority('notificacion.ver')")
    public ResponseEntity<RespuestaNotificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(servicioNotificacion.marcarLeida(id));
    }
}
