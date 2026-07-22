package com.rrhh.saas.documento.repositorio;

import com.rrhh.saas.documento.dominio.FirmaDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioFirmaDocumento extends JpaRepository<FirmaDocumento, Long> {

    Optional<FirmaDocumento> findByAsignacionIdAndDeletedFalse(Long asignacionId);
}
