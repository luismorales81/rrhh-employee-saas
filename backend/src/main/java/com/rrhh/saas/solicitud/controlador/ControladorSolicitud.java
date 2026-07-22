package com.rrhh.saas.solicitud.controlador;

import com.rrhh.saas.solicitud.dto.RespuestaFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.RespuestaSolicitud;
import com.rrhh.saas.solicitud.dto.RespuestaTipoSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudActualizarFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudActualizarTipoSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudAccionWorkflow;
import com.rrhh.saas.solicitud.dto.SolicitudCrearFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudCrearSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudCrearTipoSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioFlujoTrabajo;
import com.rrhh.saas.solicitud.servicio.ServicioSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioTipoSolicitud;
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
public class ControladorSolicitud {

    private final ServicioFlujoTrabajo servicioFlujoTrabajo;
    private final ServicioTipoSolicitud servicioTipoSolicitud;
    private final ServicioSolicitud servicioSolicitud;

    public ControladorSolicitud(ServicioFlujoTrabajo servicioFlujoTrabajo,
                                ServicioTipoSolicitud servicioTipoSolicitud,
                                ServicioSolicitud servicioSolicitud) {
        this.servicioFlujoTrabajo = servicioFlujoTrabajo;
        this.servicioTipoSolicitud = servicioTipoSolicitud;
        this.servicioSolicitud = servicioSolicitud;
    }

    @PostMapping("/flujos-trabajo")
    @PreAuthorize("hasAuthority('workflow.configurar')")
    public ResponseEntity<RespuestaFlujoTrabajo> crearFlujo(@Valid @RequestBody SolicitudCrearFlujoTrabajo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioFlujoTrabajo.crear(solicitud));
    }

    @GetMapping("/flujos-trabajo")
    @PreAuthorize("hasAuthority('workflow.ver')")
    public ResponseEntity<List<RespuestaFlujoTrabajo>> listarFlujos() {
        return ResponseEntity.ok(servicioFlujoTrabajo.listar());
    }

    @GetMapping("/flujos-trabajo/{id}")
    @PreAuthorize("hasAuthority('workflow.ver')")
    public ResponseEntity<RespuestaFlujoTrabajo> obtenerFlujo(@PathVariable Long id) {
        return ResponseEntity.ok(servicioFlujoTrabajo.obtenerPorId(id));
    }

    @PutMapping("/flujos-trabajo/{id}")
    @PreAuthorize("hasAuthority('workflow.configurar')")
    public ResponseEntity<RespuestaFlujoTrabajo> actualizarFlujo(@PathVariable Long id,
            @Valid @RequestBody SolicitudActualizarFlujoTrabajo solicitud) {
        return ResponseEntity.ok(servicioFlujoTrabajo.actualizar(id, solicitud));
    }

    @DeleteMapping("/flujos-trabajo/{id}")
    @PreAuthorize("hasAuthority('workflow.configurar')")
    public ResponseEntity<Void> eliminarFlujo(@PathVariable Long id) {
        servicioFlujoTrabajo.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/flujos-trabajo/{id}/activar")
    @PreAuthorize("hasAuthority('workflow.configurar')")
    public ResponseEntity<RespuestaFlujoTrabajo> alternarActivoFlujo(@PathVariable Long id) {
        return ResponseEntity.ok(servicioFlujoTrabajo.alternarActivo(id));
    }

    @PostMapping("/tipos-solicitud")
    @PreAuthorize("hasAuthority('tipo_solicitud.gestionar')")
    public ResponseEntity<RespuestaTipoSolicitud> crearTipo(@Valid @RequestBody SolicitudCrearTipoSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioTipoSolicitud.crear(solicitud));
    }

    @GetMapping("/tipos-solicitud")
    @PreAuthorize("hasAnyAuthority('solicitud.ver', 'solicitud.crear')")
    public ResponseEntity<List<RespuestaTipoSolicitud>> listarTipos() {
        return ResponseEntity.ok(servicioTipoSolicitud.listar());
    }

    @GetMapping("/tipos-solicitud/{id}")
    @PreAuthorize("hasAuthority('solicitud.ver')")
    public ResponseEntity<RespuestaTipoSolicitud> obtenerTipo(@PathVariable Long id) {
        return ResponseEntity.ok(servicioTipoSolicitud.obtenerPorId(id));
    }

    @PutMapping("/tipos-solicitud/{id}")
    @PreAuthorize("hasAuthority('tipo_solicitud.gestionar')")
    public ResponseEntity<RespuestaTipoSolicitud> actualizarTipo(@PathVariable Long id,
            @Valid @RequestBody SolicitudActualizarTipoSolicitud solicitud) {
        return ResponseEntity.ok(servicioTipoSolicitud.actualizar(id, solicitud));
    }

    @DeleteMapping("/tipos-solicitud/{id}")
    @PreAuthorize("hasAuthority('tipo_solicitud.gestionar')")
    public ResponseEntity<Void> eliminarTipo(@PathVariable Long id) {
        servicioTipoSolicitud.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAuthority('solicitud.crear')")
    public ResponseEntity<RespuestaSolicitud> crear(@Valid @RequestBody SolicitudCrearSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioSolicitud.crear(solicitud));
    }

    @PostMapping("/solicitudes/{id}/enviar")
    @PreAuthorize("hasAuthority('solicitud.crear')")
    public ResponseEntity<RespuestaSolicitud> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioSolicitud.enviar(id));
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasAuthority('solicitud.aprobar')")
    public ResponseEntity<RespuestaSolicitud> aprobar(@PathVariable Long id,
                                                       @RequestBody(required = false) SolicitudAccionWorkflow accion) {
        SolicitudAccionWorkflow dto = accion != null ? accion : new SolicitudAccionWorkflow(
                com.rrhh.saas.solicitud.dominio.AccionPaso.APROBAR, null);
        if (dto.accion() != com.rrhh.saas.solicitud.dominio.AccionPaso.APROBAR) {
            dto = new SolicitudAccionWorkflow(com.rrhh.saas.solicitud.dominio.AccionPaso.APROBAR, dto.comentarios());
        }
        return ResponseEntity.ok(servicioSolicitud.procesarAccion(id, dto));
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    @PreAuthorize("hasAuthority('solicitud.rechazar')")
    public ResponseEntity<RespuestaSolicitud> rechazar(@PathVariable Long id,
                                                       @RequestBody(required = false) SolicitudAccionWorkflow accion) {
        String comentarios = accion != null ? accion.comentarios() : null;
        return ResponseEntity.ok(servicioSolicitud.procesarAccion(id,
                new SolicitudAccionWorkflow(com.rrhh.saas.solicitud.dominio.AccionPaso.RECHAZAR, comentarios)));
    }

    @PostMapping("/solicitudes/{id}/solicitar-cambios")
    @PreAuthorize("hasAuthority('solicitud.solicitar_cambios')")
    public ResponseEntity<RespuestaSolicitud> solicitarCambios(@PathVariable Long id,
                                                               @Valid @RequestBody SolicitudAccionWorkflow accion) {
        return ResponseEntity.ok(servicioSolicitud.procesarAccion(id,
                new SolicitudAccionWorkflow(com.rrhh.saas.solicitud.dominio.AccionPaso.SOLICITAR_CAMBIOS, accion.comentarios())));
    }

    @PostMapping("/solicitudes/{id}/escalar")
    @PreAuthorize("hasAuthority('solicitud.escalar')")
    public ResponseEntity<RespuestaSolicitud> escalar(@PathVariable Long id,
                                                      @RequestBody(required = false) SolicitudAccionWorkflow accion) {
        String comentarios = accion != null ? accion.comentarios() : null;
        return ResponseEntity.ok(servicioSolicitud.procesarAccion(id,
                new SolicitudAccionWorkflow(com.rrhh.saas.solicitud.dominio.AccionPaso.ESCALAR, comentarios)));
    }

    @PostMapping("/solicitudes/{id}/cancelar")
    @PreAuthorize("hasAuthority('solicitud.cancelar') or hasAuthority('solicitud.aprobar')")
    public ResponseEntity<RespuestaSolicitud> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioSolicitud.cancelar(id));
    }

    @GetMapping("/solicitudes/propias")
    @PreAuthorize("hasAuthority('solicitud.crear')")
    public ResponseEntity<List<RespuestaSolicitud>> listarPropias() {
        return ResponseEntity.ok(servicioSolicitud.listarPropias());
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('solicitud.ver')")
    public ResponseEntity<List<RespuestaSolicitud>> listar() {
        return ResponseEntity.ok(servicioSolicitud.listar());
    }

    @GetMapping("/solicitudes/pendientes-aprobacion")
    @PreAuthorize("hasAuthority('solicitud.aprobar')")
    public ResponseEntity<List<RespuestaSolicitud>> listarPendientes() {
        return ResponseEntity.ok(servicioSolicitud.listarPendientesAprobacion());
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAuthority('solicitud.ver')")
    public ResponseEntity<RespuestaSolicitud> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioSolicitud.obtenerPorId(id));
    }
}
