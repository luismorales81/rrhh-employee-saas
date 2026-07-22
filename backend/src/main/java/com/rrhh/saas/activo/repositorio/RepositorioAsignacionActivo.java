package com.rrhh.saas.activo.repositorio;

import com.rrhh.saas.activo.dominio.AsignacionActivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioAsignacionActivo extends JpaRepository<AsignacionActivo, Long> {

    List<AsignacionActivo> findByEmpleadoIdAndDeletedFalseOrderByFechaEntregaDesc(Long empleadoId);

    List<AsignacionActivo> findByActivoIdAndDeletedFalseOrderByFechaEntregaDesc(Long activoId);

    Optional<AsignacionActivo> findByIdAndDeletedFalse(Long id);

    Optional<AsignacionActivo> findByActivoIdAndFechaDevolucionIsNullAndDeletedFalse(Long activoId);
}
