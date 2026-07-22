package com.rrhh.saas.activo.repositorio;

import com.rrhh.saas.activo.dominio.Activo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioActivo extends JpaRepository<Activo, Long> {

    List<Activo> findByEmpresaIdAndDeletedFalseOrderByNombreAsc(Long empresaId);

    Optional<Activo> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
