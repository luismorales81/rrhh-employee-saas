package com.rrhh.saas.nucleo.controlador;

import com.rrhh.saas.nucleo.dto.RespuestaUsuario;
import com.rrhh.saas.nucleo.dto.SolicitudCrearUsuario;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario {

    private final ServicioUsuario servicioUsuario;

    public ControladorUsuario(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('usuario.crear') or hasAuthority('empresa.crear')")
    public ResponseEntity<RespuestaUsuario> crear(@Valid @RequestBody SolicitudCrearUsuario solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioUsuario.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('usuario.ver')")
    public ResponseEntity<List<RespuestaUsuario>> listar() {
        return ResponseEntity.ok(servicioUsuario.listarPorEmpresa());
    }
}
