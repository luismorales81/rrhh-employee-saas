package com.rrhh.saas.nucleo.seguridad;

import com.rrhh.saas.comun.tenant.ContextoTenant;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final ProveedorJwt proveedorJwt;
    private final ServicioDetalleUsuario servicioDetalleUsuario;

    public FiltroJwt(ProveedorJwt proveedorJwt, ServicioDetalleUsuario servicioDetalleUsuario) {
        this.proveedorJwt = proveedorJwt;
        this.servicioDetalleUsuario = servicioDetalleUsuario;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extraerToken(request);
            if (token != null && proveedorJwt.esTokenValido(token)) {
                Claims claims = proveedorJwt.parsearToken(token);
                if (!"refresco".equals(claims.get("tipo"))) {
                    String email = claims.get("email", String.class);
                    UsuarioAutenticado usuario = (UsuarioAutenticado) servicioDetalleUsuario.loadUserByUsername(email);
                    if (!usuario.isEnabled()) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    usuario = aplicarModoSoporte(usuario, request);

                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);

                    ContextoTenant.establecerUsuarioId(usuario.getId());
                    ContextoTenant.establecerEmpresaId(resolverEmpresaId(usuario, request));
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            ContextoTenant.limpiar();
        }
    }

    private String extraerToken(HttpServletRequest request) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            return encabezado.substring(7);
        }
        return null;
    }

    private UsuarioAutenticado aplicarModoSoporte(UsuarioAutenticado usuario, HttpServletRequest request) {
        if (!usuario.isEsSuperAdmin()) {
            return usuario;
        }
        String encabezadoModoSoporte = request.getHeader("X-Modo-Soporte");
        if (encabezadoModoSoporte == null || !"true".equalsIgnoreCase(encabezadoModoSoporte.trim())) {
            return usuario;
        }
        return usuario.conPermisosAdicionales(CatalogoPermisosSoporteTenant.todos());
    }

    private Long resolverEmpresaId(UsuarioAutenticado usuario, HttpServletRequest request) {
        if (usuario.isEsSuperAdmin()) {
            String encabezadoEmpresa = request.getHeader("X-Empresa-Id");
            if (encabezadoEmpresa != null && !encabezadoEmpresa.isBlank()) {
                try {
                    return Long.parseLong(encabezadoEmpresa.trim());
                } catch (NumberFormatException ignored) {
                    return usuario.getEmpresaId();
                }
            }
        }
        return usuario.getEmpresaId();
    }
}
