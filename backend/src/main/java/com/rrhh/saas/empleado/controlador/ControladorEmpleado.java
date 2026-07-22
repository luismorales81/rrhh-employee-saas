package com.rrhh.saas.empleado.controlador;

import com.rrhh.saas.empleado.dto.RespuestaEmpleado;
import com.rrhh.saas.empleado.dto.RespuestaLegajoEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudActualizarEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudCrearEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioLegajo;
import com.rrhh.saas.offboarding.dto.RespuestaTareaOffboardingEmpleado;
import com.rrhh.saas.offboarding.dto.SolicitudCompletarTareaOffboarding;
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
@RequestMapping("/api/empleados")
public class ControladorEmpleado {

    private final ServicioEmpleado servicioEmpleado;
    private final ServicioLegajo servicioLegajo;
    private final ServicioOffboarding servicioOffboarding;

    public ControladorEmpleado(ServicioEmpleado servicioEmpleado,
                                ServicioLegajo servicioLegajo,
                                ServicioOffboarding servicioOffboarding) {
        this.servicioEmpleado = servicioEmpleado;
        this.servicioLegajo = servicioLegajo;
        this.servicioOffboarding = servicioOffboarding;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('empleado.crear')")
    public ResponseEntity<RespuestaEmpleado> crear(@Valid @RequestBody SolicitudCrearEmpleado solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioEmpleado.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('empleado.ver')")
    public ResponseEntity<List<RespuestaEmpleado>> listar() {
        return ResponseEntity.ok(servicioEmpleado.listar());
    }

    @GetMapping("/propio")
    @PreAuthorize("hasAuthority('empleado.ver')")
    public ResponseEntity<RespuestaEmpleado> obtenerPropio() {
        return ResponseEntity.ok(servicioEmpleado.obtenerPropio());
    }

    @GetMapping("/{id}/legajo")
    @PreAuthorize("hasAuthority('empleado.ver')")
    public ResponseEntity<RespuestaLegajoEmpleado> obtenerLegajo(@PathVariable Long id) {
        return ResponseEntity.ok(servicioLegajo.obtenerLegajo(id));
    }

    @PostMapping("/{id}/onboarding/iniciar")
    @PreAuthorize("hasAuthority('empleado.actualizar')")
    public ResponseEntity<RespuestaLegajoEmpleado> iniciarOnboarding(@PathVariable Long id) {
        return ResponseEntity.ok(servicioLegajo.iniciarOnboarding(id));
    }

    @PostMapping("/{id}/onboarding/completar")
    @PreAuthorize("hasAuthority('empleado.actualizar')")
    public ResponseEntity<RespuestaLegajoEmpleado> completarOnboarding(@PathVariable Long id) {
        return ResponseEntity.ok(servicioLegajo.completarOnboarding(id));
    }

    @GetMapping("/{id}/offboarding/tareas")
    @PreAuthorize("hasAuthority('offboarding.ver')")
    public ResponseEntity<List<RespuestaTareaOffboardingEmpleado>> listarTareasOffboarding(@PathVariable Long id) {
        return ResponseEntity.ok(servicioOffboarding.listarTareasEmpleado(id));
    }

    @PostMapping("/{id}/offboarding/tareas/{tareaId}/completar")
    @PreAuthorize("hasAuthority('offboarding.ver')")
    public ResponseEntity<RespuestaTareaOffboardingEmpleado> completarTareaOffboarding(
            @PathVariable Long id,
            @PathVariable Long tareaId,
            @RequestBody(required = false) SolicitudCompletarTareaOffboarding solicitud) {
        String notas = solicitud != null ? solicitud.notas() : null;
        return ResponseEntity.ok(servicioOffboarding.completarTarea(id, tareaId, notas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('empleado.ver')")
    public ResponseEntity<RespuestaEmpleado> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioEmpleado.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('empleado.actualizar')")
    public ResponseEntity<RespuestaEmpleado> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody SolicitudActualizarEmpleado solicitud) {
        return ResponseEntity.ok(servicioEmpleado.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('empleado.actualizar')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        servicioEmpleado.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
