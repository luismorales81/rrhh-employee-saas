package com.rrhh.saas.onboarding.repositorio;

import com.rrhh.saas.onboarding.dominio.PlantillaTareaOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioPlantillaTareaOnboarding extends JpaRepository<PlantillaTareaOnboarding, Long> {

    List<PlantillaTareaOnboarding> findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(Long empresaId);

    Optional<PlantillaTareaOnboarding> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
