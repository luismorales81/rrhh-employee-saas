package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.seguridad.UtilidadContextoSeguridad;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.dto.RespuestaUsuario;
import com.rrhh.saas.nucleo.dto.SolicitudCrearUsuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServicioUsuario {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final ServicioEmpresa servicioEmpresa;
    private final PasswordEncoder passwordEncoder;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario,
                           RepositorioRol repositorioRol,
                           ServicioEmpresa servicioEmpresa,
                           PasswordEncoder passwordEncoder,
                           ServicioAuditoria servicioAuditoria) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.servicioEmpresa = servicioEmpresa;
        this.passwordEncoder = passwordEncoder;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaUsuario crear(SolicitudCrearUsuario solicitud) {
        repositorioUsuario.findByEmailAndDeletedFalse(solicitud.email())
                .ifPresent(u -> { throw new ExcepcionNegocio("El email ya está registrado"); });

        Long empresaId = resolverEmpresaIdCreacion(solicitud.empresaId());
        Empresa empresa = servicioEmpresa.obtenerEntidad(empresaId);
        Set<Rol> roles = new HashSet<>();
        if (solicitud.rolesIds() != null) {
            for (Long rolId : solicitud.rolesIds()) {
                Rol rol = repositorioRol.findByIdAndEmpresaIdAndDeletedFalse(rolId, empresaId)
                        .orElseThrow(() -> new ExcepcionNegocio("Rol no encontrado"));
                roles.add(rol);
            }
        }

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setEmail(solicitud.email());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.password()));
        usuario.setActivo(true);
        usuario.setRoles(roles);

        Usuario guardado = repositorioUsuario.save(usuario);
        servicioAuditoria.registrar("CREAR", "Usuario", guardado.getId(), null, guardado.getEmail());
        return mapear(guardado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaUsuario> listarPorEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioUsuario.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    public Usuario obtenerEntidad(Long id) {
        return repositorioUsuario.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado", 404));
    }

    public Usuario obtenerPorEmail(String email) {
        return repositorioUsuario.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado", 404));
    }

    private Long resolverEmpresaIdCreacion(Long empresaIdSolicitud) {
        if (UtilidadContextoSeguridad.esSuperAdminPlataforma()) {
            if (empresaIdSolicitud == null) {
                throw new ExcepcionNegocio("Debe indicar empresaId", 400);
            }
            return empresaIdSolicitud;
        }
        Long empresaContexto = ContextoTenant.obtenerEmpresaId();
        if (empresaContexto == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        if (empresaIdSolicitud != null && !empresaIdSolicitud.equals(empresaContexto)) {
            throw new ExcepcionNegocio("No puede crear usuarios en otra empresa", 403);
        }
        return empresaContexto;
    }

    private RespuestaUsuario mapear(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream().map(Rol::getNombre).toList();
        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
        return new RespuestaUsuario(usuario.getId(), empresaId, usuario.getEmail(), usuario.isActivo(), roles);
    }
}
