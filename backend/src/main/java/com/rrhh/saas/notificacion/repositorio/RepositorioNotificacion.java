package com.rrhh.saas.notificacion.repositorio;

import com.rrhh.saas.notificacion.dominio.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioNotificacion extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdAndDeletedFalseOrderByCreatedAtDesc(Long usuarioId);

    Optional<Notificacion> findByIdAndUsuarioIdAndDeletedFalse(Long id, Long usuarioId);

    long countByUsuarioIdAndLeidaFalseAndDeletedFalse(Long usuarioId);
}
