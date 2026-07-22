package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioFlujoTrabajo extends JpaRepository<FlujoTrabajo, Long> {

    List<FlujoTrabajo> findByEmpresaIdAndDeletedFalse(Long empresaId);

    @Query("SELECT f FROM FlujoTrabajo f LEFT JOIN FETCH f.pasos p " +
            "WHERE f.id = :id AND f.empresa.id = :empresaId AND f.deleted = false " +
            "ORDER BY p.orden ASC")
    Optional<FlujoTrabajo> buscarConPasos(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
