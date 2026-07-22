package com.rrhh.saas.fichaje.repositorio;

import com.rrhh.saas.fichaje.dominio.EstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.MarcaFichaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioMarcaFichaje extends JpaRepository<MarcaFichaje, Long> {

    Optional<MarcaFichaje> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<MarcaFichaje> findFirstByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(Long empleadoId);

    List<MarcaFichaje> findByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(Long empleadoId);

    @Query("""
            SELECT m FROM MarcaFichaje m
            WHERE m.empresa.id = :empresaId AND m.deleted = false
            AND (:empleadoId IS NULL OR m.empleado.id = :empleadoId)
            AND (:estado IS NULL OR m.estado = :estado)
            AND (:zonaId IS NULL OR m.zonaFichaje.id = :zonaId)
            AND m.fechaHoraServidor >= :desde AND m.fechaHoraServidor < :hasta
            ORDER BY m.fechaHoraServidor DESC
            """)
    List<MarcaFichaje> buscarConFiltros(
            @Param("empresaId") Long empresaId,
            @Param("empleadoId") Long empleadoId,
            @Param("estado") EstadoMarcaFichaje estado,
            @Param("zonaId") Long zonaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    List<MarcaFichaje> findByEmpresaIdAndEstadoAndDeletedFalseOrderByFechaHoraServidorDesc(
            Long empresaId, EstadoMarcaFichaje estado);
}
