package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndDeletedFalse(String email);

    List<Usuario> findByEmpresaIdAndDeletedFalse(Long empresaId);

    @Query("SELECT u FROM Usuario u JOIN u.roles r JOIN r.permisos p " +
            "WHERE u.empresa.id = :empresaId AND p.codigo = :codigoPermiso " +
            "AND u.activo = true AND u.deleted = false")
    List<Usuario> buscarPorPermiso(@Param("empresaId") Long empresaId,
                                   @Param("codigoPermiso") String codigoPermiso);

    @Query("SELECT u FROM Usuario u JOIN u.roles r " +
            "WHERE u.empresa.id = :empresaId AND r.id = :rolId " +
            "AND u.activo = true AND u.deleted = false")
    List<Usuario> buscarPorRol(@Param("empresaId") Long empresaId, @Param("rolId") Long rolId);
}
