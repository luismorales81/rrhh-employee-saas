package com.rrhh.saas.vacacion.controlador;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.vacacion.dto.RespuestaConfiguracionVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaDetalleSolicitudVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaEventoCalendarioVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaPatronDescanso;
import com.rrhh.saas.vacacion.dto.RespuestaSaldoVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaSimularFechaVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaPoliticaVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaSolicitudVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudActualizarConfiguracionVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudAprobarVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudContrapropuestaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPatronDescanso;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPoliticaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudRechazarVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudSimularFechaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudVacacionesDto;
import com.rrhh.saas.vacacion.servicio.ServicioConfiguracionVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioPatronDescanso;
import com.rrhh.saas.vacacion.servicio.ServicioPoliticaVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioSaldoVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioVacaciones;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/vacaciones")
public class ControladorVacaciones {

    private final ServicioPoliticaVacaciones servicioPoliticaVacaciones;
    private final ServicioSaldoVacaciones servicioSaldoVacaciones;
    private final ServicioVacaciones servicioVacaciones;
    private final ServicioConfiguracionVacaciones servicioConfiguracionVacaciones;
    private final ServicioPatronDescanso servicioPatronDescanso;

    public ControladorVacaciones(ServicioPoliticaVacaciones servicioPoliticaVacaciones,
                                 ServicioSaldoVacaciones servicioSaldoVacaciones,
                                 ServicioVacaciones servicioVacaciones,
                                 ServicioConfiguracionVacaciones servicioConfiguracionVacaciones,
                                 ServicioPatronDescanso servicioPatronDescanso) {
        this.servicioPoliticaVacaciones = servicioPoliticaVacaciones;
        this.servicioSaldoVacaciones = servicioSaldoVacaciones;
        this.servicioVacaciones = servicioVacaciones;
        this.servicioConfiguracionVacaciones = servicioConfiguracionVacaciones;
        this.servicioPatronDescanso = servicioPatronDescanso;
    }

    @PostMapping("/politicas")
    @PreAuthorize("hasAuthority('vacacion.politica.gestionar')")
    public ResponseEntity<RespuestaPoliticaVacaciones> crearPolitica(
            @Valid @RequestBody SolicitudCrearPoliticaVacaciones solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioPoliticaVacaciones.crear(solicitud));
    }

    @GetMapping("/politicas")
    @PreAuthorize("hasAuthority('vacacion.politica.ver') or hasAuthority('vacacion.politica.gestionar')")
    public ResponseEntity<List<RespuestaPoliticaVacaciones>> listarPoliticas() {
        return ResponseEntity.ok(servicioPoliticaVacaciones.listar());
    }

    @PostMapping("/politicas/{id}/activar")
    @PreAuthorize("hasAuthority('vacacion.politica.gestionar')")
    public ResponseEntity<RespuestaPoliticaVacaciones> activarPolitica(@PathVariable Long id) {
        return ResponseEntity.ok(servicioPoliticaVacaciones.activar(id));
    }

    @GetMapping("/configuracion")
    @PreAuthorize("hasAuthority('vacacion.configurar')")
    public ResponseEntity<RespuestaConfiguracionVacaciones> obtenerConfiguracion() {
        return ResponseEntity.ok(servicioConfiguracionVacaciones.obtener());
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasAuthority('vacacion.configurar')")
    public ResponseEntity<RespuestaConfiguracionVacaciones> actualizarConfiguracion(
            @Valid @RequestBody SolicitudActualizarConfiguracionVacaciones solicitud) {
        return ResponseEntity.ok(servicioConfiguracionVacaciones.actualizar(solicitud));
    }

    @GetMapping("/saldo/propio")
    @PreAuthorize("hasAuthority('vacacion.saldo.ver')")
    public ResponseEntity<RespuestaSaldoVacaciones> obtenerSaldoPropio(
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(servicioSaldoVacaciones.obtenerSaldoPropio(anio));
    }

    @GetMapping("/saldo/propio/historial")
    @PreAuthorize("hasAuthority('vacacion.saldo.ver')")
    public ResponseEntity<List<RespuestaSaldoVacaciones>> historialSaldoPropio() {
        return ResponseEntity.ok(servicioSaldoVacaciones.obtenerHistorialSaldoPropio());
    }

    @GetMapping("/saldo/empleado/{empleadoId}")
    @PreAuthorize("hasAuthority('vacacion.saldo.gestionar') or hasAuthority('vacacion.saldo.ver')")
    public ResponseEntity<RespuestaSaldoVacaciones> obtenerSaldoEmpleado(
            @PathVariable Long empleadoId,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(servicioSaldoVacaciones.obtenerSaldo(empleadoId, anio));
    }

    @GetMapping("/saldos")
    @PreAuthorize("hasAuthority('vacacion.saldo.gestionar')")
    public ResponseEntity<List<RespuestaSaldoVacaciones>> listarSaldos() {
        return ResponseEntity.ok(servicioSaldoVacaciones.listarPorEmpresa());
    }

    @PostMapping("/simular-fecha")
    @PreAuthorize("hasAuthority('vacacion.solicitar')")
    public ResponseEntity<RespuestaSimularFechaVacaciones> simularFecha(
            @Valid @RequestBody SolicitudSimularFechaVacaciones solicitud) {
        return ResponseEntity.ok(servicioVacaciones.simularFecha(solicitud));
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAuthority('vacacion.solicitar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> solicitar(
            @Valid @RequestBody SolicitudVacacionesDto solicitud,
            HttpServletRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioVacaciones.solicitar(solicitud, peticion));
    }

    @GetMapping("/solicitudes/propias")
    @PreAuthorize("hasAuthority('vacacion.ver') or hasAuthority('vacacion.solicitar')")
    public ResponseEntity<List<RespuestaSolicitudVacaciones>> listarPropias() {
        return ResponseEntity.ok(servicioVacaciones.listarPropias());
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAuthority('vacacion.ver')")
    public ResponseEntity<List<RespuestaSolicitudVacaciones>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado) {
        return ResponseEntity.ok(servicioVacaciones.listarPorEmpresa(estado));
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAuthority('vacacion.ver') or hasAuthority('vacacion.solicitar')")
    public ResponseEntity<RespuestaDetalleSolicitudVacaciones> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(servicioVacaciones.obtenerDetalle(id));
    }

    @PatchMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasAuthority('vacacion.aprobar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> aprobar(
            @PathVariable Long id,
            @RequestBody(required = false) SolicitudAprobarVacaciones solicitud,
            HttpServletRequest peticion) {
        SolicitudAprobarVacaciones body = solicitud != null ? solicitud : new SolicitudAprobarVacaciones(null, null, null);
        return ResponseEntity.ok(servicioVacaciones.aprobar(id, body, peticion));
    }

    @PatchMapping("/solicitudes/{id}/contraproponer")
    @PreAuthorize("hasAuthority('vacacion.aprobar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> contraproponer(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudContrapropuestaVacaciones solicitud,
            HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioVacaciones.contraproponer(id, solicitud, peticion));
    }

    @PatchMapping("/solicitudes/{id}/rechazar")
    @PreAuthorize("hasAuthority('vacacion.aprobar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRechazarVacaciones solicitud,
            HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioVacaciones.rechazar(id, solicitud, peticion));
    }

    @PostMapping("/solicitudes/{id}/aceptar-contrapropuesta")
    @PreAuthorize("hasAuthority('vacacion.solicitar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> aceptarContrapropuesta(
            @PathVariable Long id,
            HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioVacaciones.aceptarContrapropuesta(id, peticion));
    }

    @PostMapping("/solicitudes/{id}/cancelar")
    @PreAuthorize("hasAuthority('vacacion.solicitar')")
    public ResponseEntity<RespuestaSolicitudVacaciones> cancelar(
            @PathVariable Long id,
            HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioVacaciones.cancelar(id, peticion));
    }

    @GetMapping("/empleados/{empleadoId}/patron-descanso")
    @PreAuthorize("hasAuthority('vacacion.patron.gestionar') or hasAuthority('vacacion.ver')")
    public ResponseEntity<List<RespuestaPatronDescanso>> listarPatronesDescanso(
            @PathVariable Long empleadoId) {
        return ResponseEntity.ok(servicioPatronDescanso.listarPorEmpleado(empleadoId));
    }

    @PostMapping("/empleados/{empleadoId}/patron-descanso")
    @PreAuthorize("hasAuthority('vacacion.patron.gestionar')")
    public ResponseEntity<RespuestaPatronDescanso> crearPatronDescanso(
            @PathVariable Long empleadoId,
            @Valid @RequestBody SolicitudCrearPatronDescanso solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioPatronDescanso.crear(empleadoId, solicitud));
    }

    @GetMapping("/calendario")
    @PreAuthorize("hasAuthority('vacacion.ver') or hasAuthority('vacacion.solicitar')")
    public ResponseEntity<List<RespuestaEventoCalendarioVacaciones>> obtenerCalendario(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta) {
        return ResponseEntity.ok(servicioVacaciones.obtenerCalendario(desde, hasta));
    }
}
