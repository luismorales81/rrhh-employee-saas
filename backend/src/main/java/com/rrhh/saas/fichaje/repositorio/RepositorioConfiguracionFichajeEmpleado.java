package com.rrhh.saas.fichaje.repositorio;

import com.rrhh.saas.fichaje.dominio.ConfiguracionFichajeEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioConfiguracionFichajeEmpleado extends JpaRepository<ConfiguracionFichajeEmpleado, Long> {

    Optional<ConfiguracionFichajeEmpleado> findByEmpleadoIdAndDeletedFalse(Long empleadoId);

    @Query("""
            SELECT c FROM ConfiguracionFichajeEmpleado c
            JOIN c.empleado e
            WHERE e.empresa.id = :empresaId AND c.deleted = false AND e.deleted = false
            ORDER BY e.apellido, e.nombre
            """)
    List<ConfiguracionFichajeEmpleado> findByEmpresaId(@Param("empresaId") Long empresaId);
}
