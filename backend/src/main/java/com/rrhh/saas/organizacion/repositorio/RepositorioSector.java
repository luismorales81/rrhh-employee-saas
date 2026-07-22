package com.rrhh.saas.organizacion.repositorio;

import com.rrhh.saas.organizacion.dominio.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioSector extends JpaRepository<Sector, Long> {

    List<Sector> findByEmpresaIdAndDeletedFalse(Long empresaId);
}
