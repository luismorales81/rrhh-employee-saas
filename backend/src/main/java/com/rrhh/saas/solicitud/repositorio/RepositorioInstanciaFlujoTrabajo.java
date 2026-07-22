package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.InstanciaFlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.EstadoInstanciaFlujo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioInstanciaFlujoTrabajo extends JpaRepository<InstanciaFlujoTrabajo, Long> {

    Optional<InstanciaFlujoTrabajo> findBySolicitudId(Long solicitudId);

    List<InstanciaFlujoTrabajo> findByAprobadorActualIdAndEstado(Long aprobadorId, EstadoInstanciaFlujo estado);
}
