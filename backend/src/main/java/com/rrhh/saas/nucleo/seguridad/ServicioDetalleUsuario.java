package com.rrhh.saas.nucleo.seguridad;

import com.rrhh.saas.nucleo.dominio.Permiso;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioDetalleUsuario implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    public ServicioDetalleUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<String> permisos = new ArrayList<>();
        if (usuario.isEsSuperAdmin()) {
            permisos.addAll(CatalogoPermisosSuperAdmin.todos());
        } else {
            for (Rol rol : usuario.getRoles()) {
                for (Permiso permiso : rol.getPermisos()) {
                    permisos.add(permiso.getCodigo());
                }
            }
        }

        Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;

        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getEmail(),
                empresaId,
                usuario.isEsSuperAdmin(),
                permisos.stream().distinct().toList(),
                usuario.isActivo()
        );
    }
}
