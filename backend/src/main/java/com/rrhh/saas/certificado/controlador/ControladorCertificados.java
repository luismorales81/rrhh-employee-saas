package com.rrhh.saas.certificado.controlador;



import com.rrhh.saas.certificado.dto.RespuestaConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.dto.RespuestaReporteAusentismo;

import com.rrhh.saas.certificado.dto.SolicitudActualizarConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.dto.SolicitudCargarCertificado;

import com.rrhh.saas.certificado.dto.SolicitudRechazarCertificado;

import com.rrhh.saas.certificado.dto.SolicitudValidarCertificado;

import com.rrhh.saas.certificado.servicio.ServicioCertificado;

import com.rrhh.saas.certificado.servicio.ServicioConfiguracionAuditoriaMedica;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;

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

@RequestMapping("/api")

public class ControladorCertificados {



    private final ServicioCertificado servicioCertificado;

    private final ServicioConfiguracionAuditoriaMedica servicioConfiguracion;



    public ControladorCertificados(ServicioCertificado servicioCertificado,

                                   ServicioConfiguracionAuditoriaMedica servicioConfiguracion) {

        this.servicioCertificado = servicioCertificado;

        this.servicioConfiguracion = servicioConfiguracion;

    }



    @PostMapping("/certificados")

    @PreAuthorize("hasAuthority('certificado.cargar')")

    public ResponseEntity<Object> cargar(@Valid @RequestBody SolicitudCargarCertificado solicitud,

                                         HttpServletRequest peticion) {

        return ResponseEntity.status(HttpStatus.CREATED)

                .body(servicioCertificado.cargar(solicitud, peticion));

    }



    @GetMapping("/certificados/bandeja-revision")

    @PreAuthorize("hasAuthority('certificado.ver_completo') or hasAuthority('certificado.ver_resumen') "

            + "or hasAuthority('certificado.validar')")

    public ResponseEntity<List<?>> bandejaRevision(

            @RequestParam(defaultValue = "false") boolean incluirRequiereAclaracion) {

        return ResponseEntity.ok(servicioCertificado.listarBandejaRevision(incluirRequiereAclaracion));

    }



    @GetMapping("/certificados/{id}")

    @PreAuthorize("hasAuthority('certificado.ver_completo') or hasAuthority('certificado.ver_resumen') "

            + "or hasAuthority('certificado.ver_propio')")

    public ResponseEntity<Object> obtener(@PathVariable Long id) {

        return ResponseEntity.ok(servicioCertificado.obtenerPorId(id));

    }



    @GetMapping("/certificados/empleado/{empleadoId}")

    @PreAuthorize("hasAuthority('certificado.ver_completo') or hasAuthority('certificado.ver_resumen') "

            + "or hasAuthority('certificado.ver_propio')")

    public ResponseEntity<List<?>> historial(@PathVariable Long empleadoId) {

        return ResponseEntity.ok(servicioCertificado.listarPorEmpleado(empleadoId));

    }



    @PatchMapping("/certificados/{id}/reprocesar")

    @PreAuthorize("hasAuthority('certificado.configurar') or hasAuthority('certificado.validar')")

    public ResponseEntity<Object> reprocesar(@PathVariable Long id, HttpServletRequest peticion) {

        return ResponseEntity.ok(servicioCertificado.reprocesar(id, peticion));

    }



    @PatchMapping("/certificados/{id}/validar")

    @PreAuthorize("hasAuthority('certificado.validar')")

    public ResponseEntity<Object> validar(@PathVariable Long id,

                                          @Valid @RequestBody SolicitudValidarCertificado solicitud,

                                          HttpServletRequest peticion) {

        return ResponseEntity.ok(servicioCertificado.validar(id, solicitud, peticion));

    }



    @PatchMapping("/certificados/{id}/rechazar")

    @PreAuthorize("hasAuthority('certificado.rechazar')")

    public ResponseEntity<Object> rechazar(@PathVariable Long id,

                                           @Valid @RequestBody SolicitudRechazarCertificado solicitud,

                                           HttpServletRequest peticion) {

        return ResponseEntity.ok(servicioCertificado.rechazar(id, solicitud, peticion));

    }



    @GetMapping("/reportes/ausentismo")

    @PreAuthorize("hasAuthority('reporte.ausentismo.ver')")

    public ResponseEntity<RespuestaReporteAusentismo> reporteAusentismo(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(servicioCertificado.reporteAusentismo(desde, hasta));

    }



    @GetMapping("/certificados/configuracion-auditoria")

    @PreAuthorize("hasAuthority('certificado.configurar')")

    public ResponseEntity<RespuestaConfiguracionAuditoriaMedica> obtenerConfiguracion() {

        return ResponseEntity.ok(servicioConfiguracion.obtener());

    }



    @PutMapping("/certificados/configuracion-auditoria")

    @PreAuthorize("hasAuthority('certificado.configurar')")

    public ResponseEntity<RespuestaConfiguracionAuditoriaMedica> actualizarConfiguracion(

            @Valid @RequestBody SolicitudActualizarConfiguracionAuditoriaMedica solicitud) {

        return ResponseEntity.ok(servicioConfiguracion.actualizar(solicitud));

    }

}


