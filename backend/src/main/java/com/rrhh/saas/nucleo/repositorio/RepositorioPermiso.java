package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioPermiso extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigoAndDeletedFalse(String codigo);

    List<Permiso> findByDeletedFalse();
}
