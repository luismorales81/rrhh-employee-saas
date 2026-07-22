package com.rrhh.saas.recibo.controlador;

import com.rrhh.saas.recibo.dto.RespuestaAlertasRecibos;
import com.rrhh.saas.recibo.dto.RespuestaAnalisisCargaMasiva;
import com.rrhh.saas.recibo.dto.RespuestaCargaMasivaRecibos;
import com.rrhh.saas.recibo.dto.RespuestaEventoFirmaRecibo;
import com.rrhh.saas.recibo.dto.RespuestaRecibo;
import com.rrhh.saas.recibo.dto.SolicitudCargarRecibo;
import com.rrhh.saas.recibo.dto.SolicitudConfirmarRecepcionRecibo;
import com.rrhh.saas.recibo.dto.SolicitudRepublicarRecibo;
import com.rrhh.saas.recibo.servicio.ServicioCargaMasivaRecibos;
import com.rrhh.saas.recibo.servicio.ServicioComprobanteRecibo;
import com.rrhh.saas.recibo.servicio.ServicioExpedienteRecibo;
import com.rrhh.saas.recibo.servicio.ServicioRecibo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/recibos")
public class ControladorRecibos {

    private final ServicioRecibo servicioRecibo;
    private final ServicioExpedienteRecibo servicioExpedienteRecibo;
    private final ServicioCargaMasivaRecibos servicioCargaMasivaRecibos;
    private final ServicioComprobanteRecibo servicioComprobanteRecibo;

    public ControladorRecibos(ServicioRecibo servicioRecibo,
                              ServicioExpedienteRecibo servicioExpedienteRecibo,
                              ServicioCargaMasivaRecibos servicioCargaMasivaRecibos,
                              ServicioComprobanteRecibo servicioComprobanteRecibo) {
        this.servicioRecibo = servicioRecibo;
        this.servicioExpedienteRecibo = servicioExpedienteRecibo;
        this.servicioCargaMasivaRecibos = servicioCargaMasivaRecibos;
        this.servicioComprobanteRecibo = servicioComprobanteRecibo;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('recibo.cargar')")
    public ResponseEntity<RespuestaRecibo> cargar(@Valid @RequestBody SolicitudCargarRecibo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioRecibo.cargar(solicitud));
    }

    @PostMapping(value = "/carga-masiva/analizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('recibo.cargar')")
    public ResponseEntity<RespuestaAnalisisCargaMasiva> analizarCargaMasiva(
            @RequestParam("archivoZip") MultipartFile archivoZip,
            @RequestParam String periodo) {
        return ResponseEntity.ok(servicioCargaMasivaRecibos.analizar(archivoZip, periodo));
    }

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('recibo.cargar')")
    public ResponseEntity<RespuestaCargaMasivaRecibos> ejecutarCargaMasiva(
            @RequestParam("archivoZip") MultipartFile archivoZip,
            @RequestParam String periodo,
            @RequestParam String tituloBase,
            @RequestParam(required = false) BigDecimal montoNeto,
            @RequestParam(required = false) BigDecimal montoBruto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioCargaMasivaRecibos.ejecutar(archivoZip, periodo, tituloBase, montoNeto, montoBruto));
    }

    @GetMapping("/alertas/seguimiento")
    @PreAuthorize("hasAuthority('recibo.ver')")
    public ResponseEntity<RespuestaAlertasRecibos> obtenerAlertasSeguimiento() {
        return ResponseEntity.ok(servicioRecibo.obtenerResumenAlertas());
    }

    @GetMapping("/empleado/{empleadoId}")
    @PreAuthorize("hasAuthority('recibo.ver')")
    public ResponseEntity<List<RespuestaRecibo>> listarPorEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(servicioRecibo.listarPorEmpleado(empleadoId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('recibo.ver')")
    public ResponseEntity<List<RespuestaRecibo>> listar() {
        return ResponseEntity.ok(servicioRecibo.listarEmpresa());
    }

    @GetMapping("/mis-recibos")
    @PreAuthorize("hasAuthority('recibo.mis.ver')")
    public ResponseEntity<List<RespuestaRecibo>> listarPropios() {
        return ResponseEntity.ok(servicioRecibo.listarPropios());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('recibo.ver') or hasAuthority('recibo.mis.ver')")
    public ResponseEntity<RespuestaRecibo> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioRecibo.obtenerPorId(id));
    }

    @GetMapping("/{id}/versiones")
    @PreAuthorize("hasAuthority('recibo.ver') or hasAuthority('recibo.mis.ver')")
    public ResponseEntity<List<RespuestaRecibo>> listarVersiones(@PathVariable Long id) {
        return ResponseEntity.ok(servicioRecibo.listarVersiones(id));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('recibo.ver') or hasAuthority('recibo.mis.ver')")
    public ResponseEntity<Resource> descargarPdf(@PathVariable Long id) {
        Resource recurso = servicioRecibo.obtenerPdf(id);
        String tipoMime = servicioRecibo.obtenerTipoMimePdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipoMime))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recibo-" + id + ".pdf\"")
                .body(recurso);
    }

    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAuthority('recibo.ver') or hasAuthority('recibo.mis.ver')")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long id) {
        var recibo = servicioRecibo.obtenerReciboEmpresaEntidad(id);
        byte[] json = servicioComprobanteRecibo.generarComprobanteJson(recibo);
        String nombre = servicioComprobanteRecibo.construirNombreArchivo(recibo);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(json);
    }

    @GetMapping("/{id}/expediente")
    @PreAuthorize("hasAuthority('recibo.exportar_expediente')")
    public ResponseEntity<byte[]> exportarExpediente(@PathVariable Long id) {
        var resultado = servicioExpedienteRecibo.exportar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resultado.nombreArchivo() + "\"")
                .body(resultado.contenido());
    }

    @PostMapping("/{id}/visualizar")
    @PreAuthorize("hasAuthority('recibo.mis.ver')")
    public ResponseEntity<RespuestaRecibo> visualizar(@PathVariable Long id, HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioRecibo.registrarVisualizacion(id, peticion));
    }

    @PostMapping("/{id}/confirmar-recepcion")
    @PreAuthorize("hasAuthority('recibo.firmar')")
    public ResponseEntity<RespuestaRecibo> confirmarRecepcion(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudConfirmarRecepcionRecibo solicitud,
            HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioRecibo.confirmarRecepcion(id, solicitud, peticion));
    }

    @PostMapping("/{id}/republicar")
    @PreAuthorize("hasAuthority('recibo.cargar')")
    public ResponseEntity<RespuestaRecibo> republicar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRepublicarRecibo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioRecibo.republicar(id, solicitud));
    }

    @PostMapping("/{id}/reenviar-notificacion")
    @PreAuthorize("hasAuthority('recibo.cargar')")
    public ResponseEntity<RespuestaRecibo> reenviarNotificacion(@PathVariable Long id) {
        return ResponseEntity.ok(servicioRecibo.reenviarNotificacion(id));
    }

    @GetMapping("/{id}/eventos-firma")
    @PreAuthorize("hasAuthority('recibo.ver') or hasAuthority('recibo.mis.ver')")
    public ResponseEntity<List<RespuestaEventoFirmaRecibo>> listarEventosFirma(@PathVariable Long id) {
        return ResponseEntity.ok(servicioRecibo.listarEventosFirma(id));
    }

    @PostMapping("/{id}/firmar")
    @PreAuthorize("hasAuthority('recibo.firmar')")
    public ResponseEntity<RespuestaRecibo> firmar(@PathVariable Long id, HttpServletRequest peticion) {
        return ResponseEntity.ok(servicioRecibo.firmar(id, peticion));
    }
}
