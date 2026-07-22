package com.rrhh.saas.onboarding.controlador;

import com.rrhh.saas.onboarding.dto.RespuestaPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dto.RespuestaTareaOnboardingEmpleado;
import com.rrhh.saas.onboarding.dto.SolicitudCompletarTareaOnboarding;
import com.rrhh.saas.onboarding.dto.SolicitudActualizarPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.dto.SolicitudCrearPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.servicio.ServicioOnboarding;
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
@RequestMapping("/api")
public class ControladorOnboarding {

    private final ServicioOnboarding servicioOnboarding;

    public ControladorOnboarding(ServicioOnboarding servicioOnboarding) {
        this.servicioOnboarding = servicioOnboarding;
    }

    @GetMapping("/onboarding/plantillas")
    @PreAuthorize("hasAuthority('onboarding.ver')")
    public ResponseEntity<List<RespuestaPlantillaTareaOnboarding>> listarPlantillas() {
        return ResponseEntity.ok(servicioOnboarding.listarPlantillas());
    }

    @PostMapping("/onboarding/plantillas")
    @PreAuthorize("hasAuthority('onboarding.gestionar')")
    public ResponseEntity<RespuestaPlantillaTareaOnboarding> crearPlantilla(
            @Valid @RequestBody SolicitudCrearPlantillaTareaOnboarding solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOnboarding.crearPlantilla(solicitud));
    }

    @PutMapping("/onboarding/plantillas/{id}")
    @PreAuthorize("hasAuthority('onboarding.gestionar')")
    public ResponseEntity<RespuestaPlantillaTareaOnboarding> actualizarPlantilla(
            @PathVariable Long id,
            @RequestBody SolicitudActualizarPlantillaTareaOnboarding solicitud) {
        return ResponseEntity.ok(servicioOnboarding.actualizarPlantilla(id, solicitud));
    }

    @DeleteMapping("/onboarding/plantillas/{id}")
    @PreAuthorize("hasAuthority('onboarding.gestionar')")
    public ResponseEntity<Void> eliminarPlantilla(@PathVariable Long id) {
        servicioOnboarding.eliminarPlantilla(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empleados/{id}/onboarding/tareas")
    @PreAuthorize("hasAuthority('onboarding.ver')")
    public ResponseEntity<List<RespuestaTareaOnboardingEmpleado>> listarTareas(@PathVariable Long id) {
        return ResponseEntity.ok(servicioOnboarding.listarTareasEmpleado(id));
    }

    @PostMapping("/empleados/{id}/onboarding/tareas/{tareaId}/completar")
    @PreAuthorize("hasAuthority('onboarding.ver')")
    public ResponseEntity<RespuestaTareaOnboardingEmpleado> completarTarea(
            @PathVariable Long id,
            @PathVariable Long tareaId,
            @RequestBody(required = false) SolicitudCompletarTareaOnboarding solicitud) {
        String notas = solicitud != null ? solicitud.notas() : null;
        return ResponseEntity.ok(servicioOnboarding.completarTarea(id, tareaId, notas));
    }
}
