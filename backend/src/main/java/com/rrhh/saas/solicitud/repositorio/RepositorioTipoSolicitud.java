package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioTipoSolicitud extends JpaRepository<TipoSolicitud, Long> {

    List<TipoSolicitud> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<TipoSolicitud> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<TipoSolicitud> findByEmpresaIdAndNombreAndDeletedFalse(Long empresaId, String nombre);

    boolean existsByFlujoTrabajoIdAndDeletedFalse(Long flujoTrabajoId);
}
