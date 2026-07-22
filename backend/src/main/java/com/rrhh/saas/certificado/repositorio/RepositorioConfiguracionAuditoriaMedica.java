package com.rrhh.saas.certificado.repositorio;

import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioConfiguracionAuditoriaMedica extends JpaRepository<ConfiguracionAuditoriaMedica, Long> {

    Optional<ConfiguracionAuditoriaMedica> findByEmpresaIdAndDeletedFalse(Long empresaId);
}
