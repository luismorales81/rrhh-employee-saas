package com.rrhh.saas.nucleo.repositorio;

import com.rrhh.saas.nucleo.dominio.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioEmpresa extends JpaRepository<Empresa, Long> {

    List<Empresa> findByDeletedFalse();
}
