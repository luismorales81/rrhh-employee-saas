package com.rrhh.saas.novedad.controlador;

import com.rrhh.saas.novedad.dto.RespuestaConfirmacionNovedades;
import com.rrhh.saas.novedad.dto.RespuestaNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudActualizarNovedadLiquidacion;
import com.rrhh.saas.novedad.dto.SolicitudCrearNovedadLiquidacion;
import com.rrhh.saas.novedad.servicio.ServicioNovedadLiquidacion;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/api/novedades")
public class ControladorNovedades {

    private final ServicioNovedadLiquidacion servicioNovedad;

    public ControladorNovedades(ServicioNovedadLiquidacion servicioNovedad) {
        this.servicioNovedad = servicioNovedad;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('novedad.ver')")
    public ResponseEntity<List<RespuestaNovedadLiquidacion>> listar() {
        return ResponseEntity.ok(servicioNovedad.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('novedad.ver')")
    public ResponseEntity<RespuestaNovedadLiquidacion> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioNovedad.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('novedad.gestionar')")
    public ResponseEntity<RespuestaNovedadLiquidacion> crear(
            @Valid @RequestBody SolicitudCrearNovedadLiquidacion solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioNovedad.crear(solicitud));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('novedad.gestionar')")
    public ResponseEntity<RespuestaNovedadLiquidacion> actualizar(
            @PathVariable Long id,
            @RequestBody SolicitudActualizarNovedadLiquidacion solicitud) {
        return ResponseEntity.ok(servicioNovedad.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('novedad.gestionar')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioNovedad.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('novedad.ver')")
    public ResponseEntity<byte[]> exportar(@RequestParam String periodo) {
        byte[] csv = servicioNovedad.exportarCsv(periodo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novedades_" + periodo + ".csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @PostMapping("/confirmar")
    @PreAuthorize("hasAuthority('novedad.gestionar')")
    public ResponseEntity<RespuestaConfirmacionNovedades> confirmarLote(@RequestParam String periodo) {
        int cantidad = servicioNovedad.confirmarLote(periodo);
        return ResponseEntity.ok(new RespuestaConfirmacionNovedades(periodo, cantidad));
    }
}
