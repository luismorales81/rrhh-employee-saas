package com.rrhh.saas.organizacion.repositorio;

import com.rrhh.saas.organizacion.dominio.Gerencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioGerencia extends JpaRepository<Gerencia, Long> {

    List<Gerencia> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<Gerencia> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
