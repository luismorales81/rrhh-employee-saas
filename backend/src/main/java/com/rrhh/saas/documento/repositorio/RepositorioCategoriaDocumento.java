package com.rrhh.saas.documento.repositorio;

import com.rrhh.saas.documento.dominio.CategoriaDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioCategoriaDocumento extends JpaRepository<CategoriaDocumento, Long> {

    List<CategoriaDocumento> findByEmpresaIdAndDeletedFalseOrderByNombreAsc(Long empresaId);

    Optional<CategoriaDocumento> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<CategoriaDocumento> findByEmpresaIdAndNombreAndDeletedFalse(Long empresaId, String nombre);
}
