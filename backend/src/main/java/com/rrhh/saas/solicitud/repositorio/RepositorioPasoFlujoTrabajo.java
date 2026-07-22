package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.PasoFlujoTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioPasoFlujoTrabajo extends JpaRepository<PasoFlujoTrabajo, Long> {

    Optional<PasoFlujoTrabajo> findByFlujoTrabajoIdAndOrdenAndDeletedFalse(Long flujoTrabajoId, int orden);
}
