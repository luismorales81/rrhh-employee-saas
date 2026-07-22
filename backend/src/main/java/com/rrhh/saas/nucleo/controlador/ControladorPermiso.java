package com.rrhh.saas.nucleo.controlador;

import com.rrhh.saas.nucleo.dto.RespuestaPermiso;
import com.rrhh.saas.nucleo.servicio.ServicioPermiso;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
public class ControladorPermiso {

    private final ServicioPermiso servicioPermiso;

    public ControladorPermiso(ServicioPermiso servicioPermiso) {
        this.servicioPermiso = servicioPermiso;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permiso.ver') or hasAuthority('rol.crear')")
    public ResponseEntity<List<RespuestaPermiso>> listar() {
        return ResponseEntity.ok(servicioPermiso.listar());
    }
}
