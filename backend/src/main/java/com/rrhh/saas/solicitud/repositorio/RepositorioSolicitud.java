package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.Solicitud;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioSolicitud extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEmpresaIdAndDeletedFalse(Long empresaId);

    List<Solicitud> findByEmpleadoIdAndDeletedFalse(Long empleadoId);

    Optional<Solicitud> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    List<Solicitud> findByEmpresaIdAndEstadoAndDeletedFalse(Long empresaId, EstadoSolicitud estado);
}
