package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.TokenRefresco;
import com.rrhh.saas.nucleo.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioTokenRefresco extends JpaRepository<TokenRefresco, Long> {

    Optional<TokenRefresco> findByTokenAndRevocadoFalse(String token);

    List<TokenRefresco> findByUsuarioIdAndRevocadoFalse(Long usuarioId);

    @Modifying
    @Query("UPDATE TokenRefresco t SET t.revocado = true WHERE t.usuario.id = :usuarioId AND t.revocado = false")
    void revocarTodosPorUsuarioId(@Param("usuarioId") Long usuarioId);

    void deleteByUsuario(Usuario usuario);
}
