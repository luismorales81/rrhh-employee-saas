package com.rrhh.saas.organizacion.repositorio;

import com.rrhh.saas.organizacion.dominio.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioPuesto extends JpaRepository<Puesto, Long> {

    List<Puesto> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<Puesto> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
