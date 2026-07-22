package com.rrhh.saas.documento.repositorio;

import com.rrhh.saas.documento.dominio.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioDocumento extends JpaRepository<Documento, Long> {

    List<Documento> findByEmpresaIdAndDeletedFalseOrderByTituloAsc(Long empresaId);

    Optional<Documento> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
