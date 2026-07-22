package com.rrhh.saas.nucleo.controlador;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.nucleo.dto.RespuestaAutenticacion;
import com.rrhh.saas.nucleo.dto.RespuestaPerfilUsuario;
import com.rrhh.saas.nucleo.dto.SolicitudLogin;
import com.rrhh.saas.nucleo.dto.SolicitudRefrescoToken;
import com.rrhh.saas.nucleo.seguridad.ServicioLimitacionIntentosAuth;
import com.rrhh.saas.nucleo.servicio.ServicioAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final ServicioLimitacionIntentosAuth servicioLimitacionIntentosAuth;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion,
                                    ServicioLimitacionIntentosAuth servicioLimitacionIntentosAuth) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.servicioLimitacionIntentosAuth = servicioLimitacionIntentosAuth;
    }

    @PostMapping("/login")
    public ResponseEntity<RespuestaAutenticacion> login(@Valid @RequestBody SolicitudLogin solicitud,
                                                        HttpServletRequest request) {
        String ip = obtenerIpCliente(request);
        servicioLimitacionIntentosAuth.verificarLogin(ip, solicitud.email());
        try {
            RespuestaAutenticacion respuesta = servicioAutenticacion.login(solicitud);
            servicioLimitacionIntentosAuth.registrarExitoLogin(ip, solicitud.email());
            return ResponseEntity.ok(respuesta);
        } catch (ExcepcionNegocio ex) {
            if (ex.getCodigoHttp() == 401 || ex.getCodigoHttp() == 403) {
                servicioLimitacionIntentosAuth.registrarFalloLogin(ip, solicitud.email());
            }
            throw ex;
        }
    }

    @PostMapping("/refrescar")
    public ResponseEntity<RespuestaAutenticacion> refrescar(@Valid @RequestBody SolicitudRefrescoToken solicitud,
                                                            HttpServletRequest request) {
        String ip = obtenerIpCliente(request);
        servicioLimitacionIntentosAuth.verificarRefresco(ip);
        servicioLimitacionIntentosAuth.registrarIntentoRefresco(ip);
        return ResponseEntity.ok(servicioAutenticacion.refrescar(solicitud));
    }

    @PostMapping("/cerrar-sesion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cerrarSesion() {
        servicioAutenticacion.cerrarSesion();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/yo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RespuestaPerfilUsuario> perfilActual() {
        return ResponseEntity.ok(servicioAutenticacion.obtenerPerfilActual());
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String reenviada = request.getHeader("X-Forwarded-For");
        if (reenviada != null && !reenviada.isBlank()) {
            return reenviada.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
