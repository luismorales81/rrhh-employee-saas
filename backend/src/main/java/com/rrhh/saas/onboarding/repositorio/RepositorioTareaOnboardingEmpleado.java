package com.rrhh.saas.onboarding.repositorio;

import com.rrhh.saas.onboarding.dominio.TareaOnboardingEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioTareaOnboardingEmpleado extends JpaRepository<TareaOnboardingEmpleado, Long> {

    List<TareaOnboardingEmpleado> findByEmpleadoIdAndDeletedFalseOrderByPlantillaOrdenAsc(Long empleadoId);

    Optional<TareaOnboardingEmpleado> findByIdAndEmpleadoIdAndDeletedFalse(Long id, Long empleadoId);

    @Query("SELECT COUNT(t) > 0 FROM TareaOnboardingEmpleado t WHERE t.empleado.id = :empleadoId " +
            "AND t.plantilla.obligatoria = true AND t.completada = false AND t.deleted = false")
    boolean tieneObligatoriasPendientes(@Param("empleadoId") Long empleadoId);
}
