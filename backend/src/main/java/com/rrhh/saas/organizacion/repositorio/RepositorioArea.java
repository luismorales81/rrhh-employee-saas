package com.rrhh.saas.organizacion.repositorio;

import com.rrhh.saas.organizacion.dominio.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioArea extends JpaRepository<Area, Long> {

    List<Area> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<Area> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    boolean existsByEmpresaIdAndGerenciaIdAndDeletedFalse(Long empresaId, Long gerenciaId);
}
