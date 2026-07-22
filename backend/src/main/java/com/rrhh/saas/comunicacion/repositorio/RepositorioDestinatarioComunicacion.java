package com.rrhh.saas.comunicacion.repositorio;

import com.rrhh.saas.comunicacion.dominio.DestinatarioComunicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioDestinatarioComunicacion extends JpaRepository<DestinatarioComunicacion, Long> {

    List<DestinatarioComunicacion> findByComunicacionIdAndDeletedFalse(Long comunicacionId);

    List<DestinatarioComunicacion> findByEmpleadoIdAndDeletedFalseOrderByCreatedAtDesc(Long empleadoId);

    Optional<DestinatarioComunicacion> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    long countByComunicacionIdAndFechaLecturaIsNotNullAndDeletedFalse(Long comunicacionId);

    long countByComunicacionIdAndDeletedFalse(Long comunicacionId);
}
