package com.rrhh.saas.ausentismo.controlador;

import com.rrhh.saas.ausentismo.dto.RespuestaAusencia;
import com.rrhh.saas.ausentismo.dto.RespuestaTipoAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudActualizarAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudCrearTipoAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudJustificarAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudRegistrarMisAusencias;
import com.rrhh.saas.ausentismo.dto.SolicitudRegistrarAusencia;
import com.rrhh.saas.ausentismo.servicio.ServicioAusencia;
import com.rrhh.saas.ausentismo.servicio.ServicioTipoAusencia;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ausentismo")
public class ControladorAusentismo {

    private final ServicioTipoAusencia servicioTipoAusencia;
    private final ServicioAusencia servicioAusencia;

    public ControladorAusentismo(ServicioTipoAusencia servicioTipoAusencia,
                                 ServicioAusencia servicioAusencia) {
        this.servicioTipoAusencia = servicioTipoAusencia;
        this.servicioAusencia = servicioAusencia;
    }

    @PostMapping("/tipos")
    @PreAuthorize("hasAuthority('ausencia.tipo.gestionar')")
    public ResponseEntity<RespuestaTipoAusencia> crearTipo(
            @Valid @RequestBody SolicitudCrearTipoAusencia solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioTipoAusencia.crear(solicitud));
    }

    @GetMapping("/tipos")
    @PreAuthorize("hasAuthority('ausencia.tipo.ver') or hasAuthority('ausencia.registrar') or hasAuthority('ausencia.solicitar')")
    public ResponseEntity<List<RespuestaTipoAusencia>> listarTipos() {
        return ResponseEntity.ok(servicioTipoAusencia.listar());
    }

    @PostMapping("/ausencias")
    @PreAuthorize("hasAuthority('ausencia.registrar')")
    public ResponseEntity<RespuestaAusencia> registrar(@Valid @RequestBody SolicitudRegistrarAusencia solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAusencia.registrar(solicitud));
    }

    @PutMapping("/ausencias/{id}")
    @PreAuthorize("hasAuthority('ausencia.actualizar')")
    public ResponseEntity<RespuestaAusencia> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody SolicitudActualizarAusencia solicitud) {
        return ResponseEntity.ok(servicioAusencia.actualizar(id, solicitud));
    }

    @GetMapping("/ausencias")
    @PreAuthorize("hasAuthority('ausencia.ver')")
    public ResponseEntity<List<RespuestaAusencia>> listarAusencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(servicioAusencia.listarPorRango(desde, hasta));
        }
        return ResponseEntity.ok(servicioAusencia.listarPorEmpresa());
    }

    @GetMapping("/ausencias/empleado/{empleadoId}")
    @PreAuthorize("hasAuthority('ausencia.ver')")
    public ResponseEntity<List<RespuestaAusencia>> listarPorEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(servicioAusencia.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/ausencias/{id}")
    @PreAuthorize("hasAuthority('ausencia.ver')")
    public ResponseEntity<RespuestaAusencia> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioAusencia.obtenerPorId(id));
    }

    @PostMapping("/mis-ausencias")
    @PreAuthorize("hasAuthority('ausencia.solicitar')")
    public ResponseEntity<RespuestaAusencia> registrarPropia(
            @Valid @RequestBody SolicitudRegistrarMisAusencias solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAusencia.registrarPropia(solicitud));
    }

    @GetMapping("/mis-ausencias")
    @PreAuthorize("hasAuthority('ausencia.mis.ver')")
    public ResponseEntity<List<RespuestaAusencia>> listarPropias() {
        return ResponseEntity.ok(servicioAusencia.listarPropias());
    }

    @PutMapping("/mis-ausencias/{id}/justificar")
    @PreAuthorize("hasAuthority('ausencia.solicitar')")
    public ResponseEntity<RespuestaAusencia> justificarPropia(
            @PathVariable Long id,
            @RequestBody SolicitudJustificarAusencia solicitud) {
        return ResponseEntity.ok(servicioAusencia.justificarPropia(id, solicitud.urlCertificado()));
    }
}
