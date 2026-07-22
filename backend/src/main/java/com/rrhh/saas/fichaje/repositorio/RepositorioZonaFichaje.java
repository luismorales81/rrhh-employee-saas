package com.rrhh.saas.fichaje.repositorio;

import com.rrhh.saas.fichaje.dominio.ZonaFichaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioZonaFichaje extends JpaRepository<ZonaFichaje, Long> {

    List<ZonaFichaje> findByEmpresaIdAndDeletedFalseOrderByNombreAsc(Long empresaId);

    Optional<ZonaFichaje> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
