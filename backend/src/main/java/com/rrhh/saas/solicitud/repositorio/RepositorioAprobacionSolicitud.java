package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.AprobacionSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioAprobacionSolicitud extends JpaRepository<AprobacionSolicitud, Long> {

    List<AprobacionSolicitud> findBySolicitudId(Long solicitudId);
}
