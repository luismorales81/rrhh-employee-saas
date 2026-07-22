package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioRol extends JpaRepository<Rol, Long> {

    List<Rol> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<Rol> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<Rol> findByEmpresaIdAndNombreAndDeletedFalse(Long empresaId, String nombre);
}
