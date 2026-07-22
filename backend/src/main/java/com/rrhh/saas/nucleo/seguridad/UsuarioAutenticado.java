package com.rrhh.saas.nucleo.seguridad;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String email;
    private final Long empresaId;
    private final boolean esSuperAdmin;
    private final List<String> permisos;
    private final boolean activo;

    public UsuarioAutenticado(Long id, String email, Long empresaId,
                              boolean esSuperAdmin, List<String> permisos, boolean activo) {
        this.id = id;
        this.email = email;
        this.empresaId = empresaId;
        this.esSuperAdmin = esSuperAdmin;
        this.permisos = permisos;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public boolean isEsSuperAdmin() {
        return esSuperAdmin;
    }

    public UsuarioAutenticado conPermisosAdicionales(List<String> adicionales) {
        List<String> fusionados = new ArrayList<>(permisos);
        fusionados.addAll(adicionales);
        return new UsuarioAutenticado(
                id,
                email,
                empresaId,
                esSuperAdmin,
                fusionados.stream().distinct().toList(),
                activo
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (esSuperAdmin) {
            List<GrantedAuthority> autoridades = permisos.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            autoridades.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            return autoridades;
        }
        return permisos.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
