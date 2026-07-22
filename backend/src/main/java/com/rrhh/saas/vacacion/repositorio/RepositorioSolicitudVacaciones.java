package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.SolicitudVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositorioSolicitudVacaciones extends JpaRepository<SolicitudVacaciones, Long> {

    Optional<SolicitudVacaciones> findBySolicitudId(Long solicitudId);

    @Query("SELECT sv FROM SolicitudVacaciones sv WHERE sv.solicitud.empresa.id = :empresaId " +
            "AND sv.solicitud.deleted = false")
    List<SolicitudVacaciones> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT sv FROM SolicitudVacaciones sv WHERE sv.solicitud.empleado.id = :empleadoId " +
            "AND sv.solicitud.deleted = false")
    List<SolicitudVacaciones> findByEmpleadoId(@Param("empleadoId") Long empleadoId);

    @Query("SELECT sv FROM SolicitudVacaciones sv JOIN FETCH sv.solicitud s JOIN FETCH s.empleado e " +
            "WHERE s.empresa.id = :empresaId AND s.deleted = false " +
            "AND sv.fechaInicio <= :hasta AND sv.fechaFin >= :desde " +
            "AND (:empleadoId IS NULL OR e.id = :empleadoId)")
    List<SolicitudVacaciones> findByRangoFechas(@Param("empresaId") Long empresaId,
                                                @Param("desde") LocalDate desde,
                                                @Param("hasta") LocalDate hasta,
                                                @Param("empleadoId") Long empleadoId);

    @Query("SELECT sv FROM SolicitudVacaciones sv JOIN FETCH sv.solicitud s " +
            "WHERE s.empleado.id = :empleadoId AND s.deleted = false " +
            "AND YEAR(sv.fechaInicio) = :anio")
    List<SolicitudVacaciones> findByEmpleadoIdAndAnio(@Param("empleadoId") Long empleadoId,
                                                      @Param("anio") int anio);

    @Query("SELECT sv FROM SolicitudVacaciones sv JOIN FETCH sv.solicitud s JOIN FETCH s.empleado e " +
            "WHERE s.empresa.id = :empresaId AND s.deleted = false " +
            "AND (:estado IS NULL OR s.estado = :estado)")
    List<SolicitudVacaciones> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId,
                                                       @Param("estado") com.rrhh.saas.solicitud.dominio.EstadoSolicitud estado);

    @Query("SELECT sv FROM SolicitudVacaciones sv JOIN FETCH sv.solicitud s JOIN FETCH s.empleado e " +
            "WHERE sv.id = :id AND s.empresa.id = :empresaId AND s.deleted = false")
    Optional<SolicitudVacaciones> findByIdAndEmpresaId(@Param("id") Long id,
                                                       @Param("empresaId") Long empresaId);
}
