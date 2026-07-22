package com.rrhh.saas.ausentismo.repositorio;

import com.rrhh.saas.ausentismo.dominio.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositorioAusencia extends JpaRepository<Ausencia, Long> {

    Optional<Ausencia> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    List<Ausencia> findByEmpleadoIdAndDeletedFalseOrderByFechaDesc(Long empleadoId);

    @Query("SELECT a FROM Ausencia a WHERE a.empresa.id = :empresaId AND a.deleted = false " +
            "ORDER BY a.fecha DESC")
    List<Ausencia> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT a FROM Ausencia a WHERE a.empresa.id = :empresaId AND a.fecha BETWEEN :desde AND :hasta " +
            "AND a.deleted = false ORDER BY a.fecha DESC")
    List<Ausencia> findByEmpresaIdAndRangoFechas(@Param("empresaId") Long empresaId,
                                                   @Param("desde") LocalDate desde,
                                                   @Param("hasta") LocalDate hasta);
}
