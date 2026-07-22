package com.rrhh.saas.offboarding.repositorio;

import com.rrhh.saas.offboarding.dominio.TareaOffboardingEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioTareaOffboardingEmpleado extends JpaRepository<TareaOffboardingEmpleado, Long> {

    List<TareaOffboardingEmpleado> findByEmpleadoIdAndDeletedFalseOrderByPlantillaOrdenAsc(Long empleadoId);

    Optional<TareaOffboardingEmpleado> findByIdAndEmpleadoIdAndDeletedFalse(Long id, Long empleadoId);

    @Query("SELECT t FROM TareaOffboardingEmpleado t WHERE t.empleado.id = :empleadoId " +
            "AND t.plantilla.tipo = :tipo AND t.completada = false AND t.deleted = false")
    Optional<TareaOffboardingEmpleado> findPendientePorTipo(@Param("empleadoId") Long empleadoId,
                                                             @Param("tipo") String tipo);

    @Query("SELECT COUNT(t) > 0 FROM TareaOffboardingEmpleado t WHERE t.empleado.id = :empleadoId " +
            "AND t.plantilla.obligatoria = true AND t.completada = false AND t.deleted = false")
    boolean tieneObligatoriasPendientes(@Param("empleadoId") Long empleadoId);
}
