package com.rrhh.saas.fichaje.controlador;

import com.rrhh.saas.fichaje.dominio.EstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaAsignacionZonaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.RespuestaEstadoFichajeDia;
import com.rrhh.saas.fichaje.dto.RespuestaMarcaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaPoliticaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaResumenFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.RespuestaZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudActualizarPoliticaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudActualizarZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudAsignacionZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.SolicitudConsentimientoFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudCrearZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudMarcarFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudRechazarMarcaFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioConfiguracionFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioMarcaFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioPoliticaFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioZonaFichaje;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fichajes")
public class ControladorFichajes {

    private final ServicioZonaFichaje servicioZona;
    private final ServicioConfiguracionFichaje servicioConfiguracion;
    private final ServicioPoliticaFichaje servicioPolitica;
    private final ServicioMarcaFichaje servicioMarca;

    public ControladorFichajes(ServicioZonaFichaje servicioZona,
                               ServicioConfiguracionFichaje servicioConfiguracion,
                               ServicioPoliticaFichaje servicioPolitica,
                               ServicioMarcaFichaje servicioMarca) {
        this.servicioZona = servicioZona;
        this.servicioConfiguracion = servicioConfiguracion;
        this.servicioPolitica = servicioPolitica;
        this.servicioMarca = servicioMarca;
    }

    @GetMapping("/zonas")
    @PreAuthorize("hasAuthority('fichaje.zona.ver')")
    public ResponseEntity<List<RespuestaZonaFichaje>> listarZonas() {
        return ResponseEntity.ok(servicioZona.listar());
    }

    @GetMapping("/zonas/{id}")
    @PreAuthorize("hasAuthority('fichaje.zona.ver')")
    public ResponseEntity<RespuestaZonaFichaje> obtenerZona(@PathVariable Long id) {
        return ResponseEntity.ok(servicioZona.obtenerPorId(id));
    }

    @PostMapping("/zonas")
    @PreAuthorize("hasAuthority('fichaje.zona.gestionar')")
    public ResponseEntity<RespuestaZonaFichaje> crearZona(@Valid @RequestBody SolicitudCrearZonaFichaje solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioZona.crear(solicitud));
    }

    @PutMapping("/zonas/{id}")
    @PreAuthorize("hasAuthority('fichaje.zona.gestionar')")
    public ResponseEntity<RespuestaZonaFichaje> actualizarZona(
            @PathVariable Long id, @RequestBody SolicitudActualizarZonaFichaje solicitud) {
        return ResponseEntity.ok(servicioZona.actualizar(id, solicitud));
    }

    @DeleteMapping("/zonas/{id}")
    @PreAuthorize("hasAuthority('fichaje.zona.gestionar')")
    public ResponseEntity<Void> eliminarZona(@PathVariable Long id) {
        servicioZona.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/zonas/{id}/asignaciones")
    @PreAuthorize("hasAuthority('fichaje.zona.gestionar')")
    public ResponseEntity<RespuestaAsignacionZonaFichaje> agregarAsignacion(
            @PathVariable Long id, @Valid @RequestBody SolicitudAsignacionZonaFichaje solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioZona.agregarAsignacion(id, solicitud));
    }

    @DeleteMapping("/zonas/{zonaId}/asignaciones/{asignacionId}")
    @PreAuthorize("hasAuthority('fichaje.zona.gestionar')")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Long zonaId, @PathVariable Long asignacionId) {
        servicioZona.eliminarAsignacion(zonaId, asignacionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/configuracion/empleados")
    @PreAuthorize("hasAuthority('fichaje.configurar')")
    public ResponseEntity<List<RespuestaConfiguracionFichajeEmpleado>> listarConfiguraciones() {
        return ResponseEntity.ok(servicioConfiguracion.listarConfiguraciones());
    }

    @PutMapping("/configuracion/empleados/{empleadoId}")
    @PreAuthorize("hasAuthority('fichaje.configurar')")
    public ResponseEntity<RespuestaConfiguracionFichajeEmpleado> actualizarConfiguracion(
            @PathVariable Long empleadoId,
            @RequestBody SolicitudConfiguracionFichajeEmpleado solicitud) {
        return ResponseEntity.ok(servicioConfiguracion.actualizarConfiguracion(empleadoId, solicitud));
    }

    @GetMapping("/politica")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RespuestaPoliticaFichaje> obtenerPolitica() {
        return ResponseEntity.ok(servicioPolitica.obtenerPolitica());
    }

    @PutMapping("/politica")
    @PreAuthorize("hasAuthority('fichaje.configurar')")
    public ResponseEntity<RespuestaPoliticaFichaje> actualizarPolitica(
            @RequestBody SolicitudActualizarPoliticaFichaje solicitud) {
        return ResponseEntity.ok(servicioPolitica.actualizarPolitica(solicitud));
    }

    @PostMapping("/consentimiento")
    @PreAuthorize("hasAuthority('fichaje.registrar')")
    public ResponseEntity<Void> registrarConsentimiento(@RequestBody(required = false) SolicitudConsentimientoFichaje solicitud) {
        String idDispositivo = solicitud != null ? solicitud.idDispositivo() : null;
        servicioConfiguracion.registrarConsentimiento(idDispositivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado-dia")
    @PreAuthorize("hasAuthority('fichaje.registrar')")
    public ResponseEntity<RespuestaEstadoFichajeDia> obtenerEstadoDia() {
        return ResponseEntity.ok(servicioMarca.obtenerEstadoDia());
    }

    @PostMapping("/marcar")
    @PreAuthorize("hasAuthority('fichaje.registrar')")
    public ResponseEntity<RespuestaMarcaFichaje> marcar(
            @Valid @RequestBody SolicitudMarcarFichaje solicitud,
            HttpServletRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioMarca.marcar(solicitud, peticion));
    }

    @GetMapping("/mis-marcas")
    @PreAuthorize("hasAuthority('fichaje.mis.ver')")
    public ResponseEntity<List<RespuestaMarcaFichaje>> listarMisMarcas() {
        return ResponseEntity.ok(servicioMarca.listarMisMarcas());
    }

    @GetMapping("/marcas")
    @PreAuthorize("hasAuthority('fichaje.ver')")
    public ResponseEntity<List<RespuestaMarcaFichaje>> listarMarcas(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) EstadoMarcaFichaje estado,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(servicioMarca.listarMarcas(empleadoId, estado, zonaId, desde, hasta));
    }

    @GetMapping("/marcas/{id}")
    @PreAuthorize("hasAuthority('fichaje.ver')")
    public ResponseEntity<RespuestaMarcaFichaje> obtenerMarca(@PathVariable Long id) {
        return ResponseEntity.ok(servicioMarca.obtenerMarca(id));
    }

    @GetMapping("/marcas/pendientes-revision")
    @PreAuthorize("hasAuthority('fichaje.revisar')")
    public ResponseEntity<List<RespuestaMarcaFichaje>> listarPendientesRevision() {
        return ResponseEntity.ok(servicioMarca.listarPendientesRevision());
    }

    @PostMapping("/marcas/{id}/aprobar")
    @PreAuthorize("hasAuthority('fichaje.revisar')")
    public ResponseEntity<RespuestaMarcaFichaje> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioMarca.aprobar(id));
    }

    @PostMapping("/marcas/{id}/rechazar")
    @PreAuthorize("hasAuthority('fichaje.revisar')")
    public ResponseEntity<RespuestaMarcaFichaje> rechazar(
            @PathVariable Long id, @Valid @RequestBody SolicitudRechazarMarcaFichaje solicitud) {
        return ResponseEntity.ok(servicioMarca.rechazar(id, solicitud.notaRevision()));
    }

    @GetMapping("/reportes/resumen")
    @PreAuthorize("hasAuthority('fichaje.ver')")
    public ResponseEntity<List<RespuestaResumenFichajeEmpleado>> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(servicioMarca.generarResumen(desde, hasta));
    }

    @GetMapping("/reportes/exportar")
    @PreAuthorize("hasAuthority('fichaje.ver')")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long empleadoId) {
        byte[] csv = servicioMarca.exportarCsv(desde, hasta, empleadoId);
        String nombre = "fichajes_" + (desde != null ? desde : LocalDate.now()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}
