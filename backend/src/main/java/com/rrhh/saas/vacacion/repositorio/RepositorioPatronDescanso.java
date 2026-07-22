package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.PatronDescanso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositorioPatronDescanso extends JpaRepository<PatronDescanso, Long> {

    @Query("SELECT p FROM PatronDescanso p JOIN FETCH p.diasDescanso WHERE p.empleado.id = :empleadoId " +
            "AND p.deleted = false AND p.vigenciaDesde <= :fecha " +
            "AND (p.vigenciaHasta IS NULL OR p.vigenciaHasta >= :fecha) " +
            "ORDER BY p.vigenciaDesde DESC")
    List<PatronDescanso> buscarVigentesEn(@Param("empleadoId") Long empleadoId,
                                          @Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM PatronDescanso p JOIN FETCH p.diasDescanso WHERE p.empleado.id = :empleadoId " +
            "AND p.deleted = false ORDER BY p.vigenciaDesde DESC")
    List<PatronDescanso> findByEmpleadoIdAndDeletedFalse(@Param("empleadoId") Long empleadoId);

    Optional<PatronDescanso> findByIdAndEmpleadoIdAndDeletedFalse(Long id, Long empleadoId);
}
