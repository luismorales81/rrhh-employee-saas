package com.rrhh.saas.offboarding.controlador;

import com.rrhh.saas.offboarding.dto.RespuestaPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dto.SolicitudActualizarPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.dto.SolicitudCrearPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.servicio.ServicioOffboarding;
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
@RequestMapping("/api/offboarding")
public class ControladorOffboarding {

    private final ServicioOffboarding servicioOffboarding;

    public ControladorOffboarding(ServicioOffboarding servicioOffboarding) {
        this.servicioOffboarding = servicioOffboarding;
    }

    @GetMapping("/plantillas")
    @PreAuthorize("hasAuthority('offboarding.ver')")
    public ResponseEntity<List<RespuestaPlantillaTareaOffboarding>> listarPlantillas() {
        return ResponseEntity.ok(servicioOffboarding.listarPlantillas());
    }

    @PostMapping("/plantillas")
    @PreAuthorize("hasAuthority('offboarding.gestionar')")
    public ResponseEntity<RespuestaPlantillaTareaOffboarding> crearPlantilla(
            @Valid @RequestBody SolicitudCrearPlantillaTareaOffboarding solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOffboarding.crearPlantilla(solicitud));
    }

    @PutMapping("/plantillas/{id}")
    @PreAuthorize("hasAuthority('offboarding.gestionar')")
    public ResponseEntity<RespuestaPlantillaTareaOffboarding> actualizarPlantilla(
            @PathVariable Long id,
            @RequestBody SolicitudActualizarPlantillaTareaOffboarding solicitud) {
        return ResponseEntity.ok(servicioOffboarding.actualizarPlantilla(id, solicitud));
    }

    @DeleteMapping("/plantillas/{id}")
    @PreAuthorize("hasAuthority('offboarding.gestionar')")
    public ResponseEntity<Void> eliminarPlantilla(@PathVariable Long id) {
        servicioOffboarding.eliminarPlantilla(id);
        return ResponseEntity.noContent().build();
    }
}
