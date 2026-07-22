package com.rrhh.saas.comunicacion.repositorio;

import com.rrhh.saas.comunicacion.dominio.Comunicacion;
import com.rrhh.saas.comunicacion.dominio.EstadoComunicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioComunicacion extends JpaRepository<Comunicacion, Long> {

    @Query("SELECT c FROM Comunicacion c WHERE c.empresa.id = :empresaId AND c.deleted = false " +
            "ORDER BY COALESCE(c.fechaPublicacion, c.fechaPublicacionProgramada, c.createdAt) DESC")
    List<Comunicacion> findByEmpresaIdAndDeletedFalseOrderByFechaPublicacionDesc(@Param("empresaId") Long empresaId);

    List<Comunicacion> findByEstadoAndFechaPublicacionProgramadaLessThanEqualAndDeletedFalse(
            EstadoComunicacion estado, LocalDateTime fecha);

    List<Comunicacion> findByEmpresaIdAndEstadoAndDeletedFalseOrderByFechaPublicacionProgramadaAsc(
            Long empresaId, EstadoComunicacion estado);

    Optional<Comunicacion> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
