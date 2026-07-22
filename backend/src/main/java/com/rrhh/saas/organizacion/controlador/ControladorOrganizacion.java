package com.rrhh.saas.organizacion.controlador;



import com.rrhh.saas.organizacion.dto.RespuestaArea;

import com.rrhh.saas.organizacion.dto.RespuestaGerencia;

import com.rrhh.saas.organizacion.dto.RespuestaPuesto;

import com.rrhh.saas.organizacion.dto.RespuestaSector;

import com.rrhh.saas.organizacion.dto.SolicitudActualizarArea;

import com.rrhh.saas.organizacion.dto.SolicitudActualizarGerencia;

import com.rrhh.saas.organizacion.dto.SolicitudActualizarPuesto;

import com.rrhh.saas.organizacion.dto.SolicitudCrearArea;

import com.rrhh.saas.organizacion.dto.SolicitudCrearGerencia;

import com.rrhh.saas.organizacion.dto.SolicitudCrearPuesto;

import com.rrhh.saas.organizacion.servicio.ServicioOrganizacion;

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

@RequestMapping("/api/organizacion")

public class ControladorOrganizacion {



    private final ServicioOrganizacion servicioOrganizacion;



    public ControladorOrganizacion(ServicioOrganizacion servicioOrganizacion) {

        this.servicioOrganizacion = servicioOrganizacion;

    }



    @PostMapping("/gerencias")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaGerencia> crearGerencia(@Valid @RequestBody SolicitudCrearGerencia solicitud) {

        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOrganizacion.crearGerencia(solicitud));

    }



    @PutMapping("/gerencias/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaGerencia> actualizarGerencia(

            @PathVariable Long id,

            @RequestBody SolicitudActualizarGerencia solicitud) {

        return ResponseEntity.ok(servicioOrganizacion.actualizarGerencia(id, solicitud));

    }



    @DeleteMapping("/gerencias/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<Void> eliminarGerencia(@PathVariable Long id) {

        servicioOrganizacion.eliminarGerencia(id);

        return ResponseEntity.noContent().build();

    }



    @GetMapping("/gerencias")

    @PreAuthorize("hasAuthority('organizacion.gestionar') or hasAuthority('empleado.ver')")

    public ResponseEntity<List<RespuestaGerencia>> listarGerencias() {

        return ResponseEntity.ok(servicioOrganizacion.listarGerencias());

    }



    @PostMapping("/areas")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaArea> crearArea(@Valid @RequestBody SolicitudCrearArea solicitud) {

        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOrganizacion.crearArea(solicitud));

    }



    @PutMapping("/areas/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaArea> actualizarArea(

            @PathVariable Long id,

            @RequestBody SolicitudActualizarArea solicitud) {

        return ResponseEntity.ok(servicioOrganizacion.actualizarArea(id, solicitud));

    }



    @DeleteMapping("/areas/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<Void> eliminarArea(@PathVariable Long id) {

        servicioOrganizacion.eliminarArea(id);

        return ResponseEntity.noContent().build();

    }



    @GetMapping("/areas")

    @PreAuthorize("hasAuthority('organizacion.gestionar') or hasAuthority('empleado.ver')")

    public ResponseEntity<List<RespuestaArea>> listarAreas() {

        return ResponseEntity.ok(servicioOrganizacion.listarAreas());

    }



    @PostMapping("/puestos")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaPuesto> crearPuesto(@Valid @RequestBody SolicitudCrearPuesto solicitud) {

        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOrganizacion.crearPuesto(solicitud));

    }



    @PutMapping("/puestos/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<RespuestaPuesto> actualizarPuesto(

            @PathVariable Long id,

            @RequestBody SolicitudActualizarPuesto solicitud) {

        return ResponseEntity.ok(servicioOrganizacion.actualizarPuesto(id, solicitud));

    }



    @DeleteMapping("/puestos/{id}")

    @PreAuthorize("hasAuthority('organizacion.gestionar')")

    public ResponseEntity<Void> eliminarPuesto(@PathVariable Long id) {

        servicioOrganizacion.eliminarPuesto(id);

        return ResponseEntity.noContent().build();

    }



    @GetMapping("/puestos")

    @PreAuthorize("hasAuthority('organizacion.gestionar') or hasAuthority('empleado.ver')")

    public ResponseEntity<List<RespuestaPuesto>> listarPuestos() {

        return ResponseEntity.ok(servicioOrganizacion.listarPuestos());

    }



    @GetMapping("/sectores")

    @PreAuthorize("hasAuthority('organizacion.gestionar') or hasAuthority('empleado.ver')")

    public ResponseEntity<List<RespuestaSector>> listarSectores() {

        return ResponseEntity.ok(servicioOrganizacion.listarSectores());

    }

}

