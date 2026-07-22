package com.rrhh.saas.fichaje.repositorio;

import com.rrhh.saas.fichaje.dominio.PoliticaFichajeEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioPoliticaFichajeEmpresa extends JpaRepository<PoliticaFichajeEmpresa, Long> {

    Optional<PoliticaFichajeEmpresa> findByEmpresaIdAndDeletedFalse(Long empresaId);
}
