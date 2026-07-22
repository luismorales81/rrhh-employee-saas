package com.rrhh.saas.empleado.repositorio;

import com.rrhh.saas.empleado.dominio.LegajoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioLegajoEmpleado extends JpaRepository<LegajoEmpleado, Long> {

    Optional<LegajoEmpleado> findByEmpleadoIdAndDeletedFalse(Long empleadoId);
}
