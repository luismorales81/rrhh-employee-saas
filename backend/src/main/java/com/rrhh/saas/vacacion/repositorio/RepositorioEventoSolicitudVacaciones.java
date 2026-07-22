package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.EventoSolicitudVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioEventoSolicitudVacaciones extends JpaRepository<EventoSolicitudVacaciones, Long> {

    List<EventoSolicitudVacaciones> findBySolicitudVacacionesIdOrderByTimestampEventoAsc(Long solicitudVacacionesId);
}
