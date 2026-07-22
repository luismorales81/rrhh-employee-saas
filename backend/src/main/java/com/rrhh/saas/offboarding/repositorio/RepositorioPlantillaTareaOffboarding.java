package com.rrhh.saas.offboarding.repositorio;

import com.rrhh.saas.offboarding.dominio.PlantillaTareaOffboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioPlantillaTareaOffboarding extends JpaRepository<PlantillaTareaOffboarding, Long> {

    List<PlantillaTareaOffboarding> findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(Long empresaId);

    Optional<PlantillaTareaOffboarding> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
