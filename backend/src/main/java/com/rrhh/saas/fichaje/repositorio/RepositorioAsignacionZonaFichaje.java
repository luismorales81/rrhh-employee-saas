package com.rrhh.saas.fichaje.repositorio;

import com.rrhh.saas.fichaje.dominio.AsignacionZonaFichaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioAsignacionZonaFichaje extends JpaRepository<AsignacionZonaFichaje, Long> {

    List<AsignacionZonaFichaje> findByZonaFichajeIdAndDeletedFalse(Long zonaFichajeId);
}
