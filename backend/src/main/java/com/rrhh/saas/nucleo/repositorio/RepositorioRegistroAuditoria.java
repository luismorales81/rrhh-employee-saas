package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioRegistroAuditoria extends JpaRepository<RegistroAuditoria, Long> {
}
