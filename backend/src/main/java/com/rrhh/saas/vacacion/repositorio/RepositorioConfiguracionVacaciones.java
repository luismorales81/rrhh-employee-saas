package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.ConfiguracionVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioConfiguracionVacaciones extends JpaRepository<ConfiguracionVacaciones, Long> {

    Optional<ConfiguracionVacaciones> findByEmpresaIdAndDeletedFalse(Long empresaId);
}
