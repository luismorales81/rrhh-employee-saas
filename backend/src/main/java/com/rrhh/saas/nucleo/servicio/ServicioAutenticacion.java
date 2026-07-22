package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Permiso;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.TokenRefresco;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.dto.RespuestaAutenticacion;
import com.rrhh.saas.nucleo.dto.RespuestaPerfilUsuario;
import com.rrhh.saas.nucleo.dto.SolicitudLogin;
import com.rrhh.saas.nucleo.dto.SolicitudRefrescoToken;
import com.rrhh.saas.nucleo.repositorio.RepositorioTokenRefresco;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.nucleo.seguridad.CatalogoPermisosSuperAdmin;
import com.rrhh.saas.nucleo.seguridad.PropiedadesJwt;
import com.rrhh.saas.nucleo.seguridad.ProveedorJwt;
import com.rrhh.saas.nucleo.seguridad.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioAutenticacion {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioTokenRefresco repositorioTokenRefresco;
    private final PasswordEncoder passwordEncoder;
    private final ProveedorJwt proveedorJwt;
    private final PropiedadesJwt propiedadesJwt;

    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario,
                                 RepositorioTokenRefresco repositorioTokenRefresco,
                                 PasswordEncoder passwordEncoder,
                                 ProveedorJwt proveedorJwt,
                                 PropiedadesJwt propiedadesJwt) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioTokenRefresco = repositorioTokenRefresco;
        this.passwordEncoder = passwordEncoder;
        this.proveedorJwt = proveedorJwt;
        this.propiedadesJwt = propiedadesJwt;
    }

    @Transactional
    public RespuestaAutenticacion login(SolicitudLogin solicitud) {
        Usuario usuario = repositorioUsuario.findByEmailAndDeletedFalse(solicitud.email())
                .orElseThrow(() -> new ExcepcionNegocio("Credenciales inválidas", 401));

        // Mismo mensaje/status para inactivo o password incorrecta: evita enumeración de cuentas.
        if (!usuario.isActivo() || !passwordEncoder.matches(solicitud.password(), usuario.getPasswordHash())) {
            throw new ExcepcionNegocio("Credenciales inválidas", 401);
        }

        usuario.setUltimoLogin(LocalDateTime.now());
        repositorioUsuario.save(usuario);

        return generarTokens(usuario);
    }

    @Transactional
    public RespuestaAutenticacion refrescar(SolicitudRefrescoToken solicitud) {
        if (!proveedorJwt.esTokenValido(solicitud.tokenRefresco())) {
            throw new ExcepcionNegocio("Token de refresco inválido", 401);
        }

        Claims claims = proveedorJwt.parsearToken(solicitud.tokenRefresco());
        if (!"refresco".equals(claims.get("tipo"))) {
            throw new ExcepcionNegocio("Token de refresco inválido", 401);
        }

        TokenRefresco tokenRefresco = repositorioTokenRefresco
                .findByTokenAndRevocadoFalse(solicitud.tokenRefresco())
                .orElseThrow(() -> new ExcepcionNegocio("Token de refresco inválido", 401));

        if (tokenRefresco.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new ExcepcionNegocio("Token de refresco expirado", 401);
        }

        Usuario usuario = tokenRefresco.getUsuario();
        if (!usuario.isActivo() || usuario.isDeleted()) {
            throw new ExcepcionNegocio("Token de refresco inválido", 401);
        }

        tokenRefresco.setRevocado(true);
        repositorioTokenRefresco.save(tokenRefresco);

        return generarTokens(usuario);
    }

    @Transactional
    public void cerrarSesion() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("No autenticado", 401);
        }
        repositorioTokenRefresco.revocarTodosPorUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public void verificarContrasenaActual(String contrasena) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("No autenticado", 401);
        }
        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado", 404));
        if (contrasena == null || !passwordEncoder.matches(contrasena, usuario.getPasswordHash())) {
            throw new ExcepcionNegocio("Contraseña incorrecta", 401);
        }
    }

    @Transactional(readOnly = true)
    public RespuestaPerfilUsuario obtenerPerfilActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new ExcepcionNegocio("No autenticado", 401);
        }
        List<String> permisos = usuario.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(p -> !p.startsWith("ROLE_"))
                .toList();
        return new RespuestaPerfilUsuario(
                usuario.getId(),
                usuario.getEmpresaId(),
                usuario.getUsername(),
                usuario.isEsSuperAdmin(),
                permisos
        );
    }

    private RespuestaAutenticacion generarTokens(Usuario usuario) {
        List<String> permisos = extraerPermisos(usuario);
        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
        String empresaNombre = usuario.getEmpresa() != null ? usuario.getEmpresa().getNombre() : null;

        String tokenAcceso = proveedorJwt.generarTokenAcceso(
                usuario.getId(), usuario.getEmail(), empresaId, usuario.isEsSuperAdmin(), permisos);

        String tokenRefrescoValor = proveedorJwt.generarTokenRefresco(usuario.getId());
        TokenRefresco tokenRefresco = new TokenRefresco();
        tokenRefresco.setUsuario(usuario);
        tokenRefresco.setToken(tokenRefrescoValor);
        tokenRefresco.setExpiraEn(LocalDateTime.now().plusNanos(propiedadesJwt.getRefrescoExpiracionMs() * 1_000_000L));
        repositorioTokenRefresco.save(tokenRefresco);

        return new RespuestaAutenticacion(
                tokenAcceso,
                tokenRefrescoValor,
                usuario.getId(),
                empresaId,
                empresaNombre,
                usuario.getEmail(),
                usuario.isEsSuperAdmin(),
                permisos
        );
    }

    private List<String> extraerPermisos(Usuario usuario) {
        List<String> permisos = new ArrayList<>();
        if (usuario.isEsSuperAdmin()) {
            permisos.addAll(CatalogoPermisosSuperAdmin.todos());
        }
        for (Rol rol : usuario.getRoles()) {
            for (Permiso permiso : rol.getPermisos()) {
                permisos.add(permiso.getCodigo());
            }
        }
        return permisos.stream().distinct().toList();
    }

}
