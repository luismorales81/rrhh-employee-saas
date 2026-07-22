package com.rrhh.saas.recibo.repositorio;

import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositorioRecibo extends JpaRepository<Recibo, Long> {

    List<Recibo> findByEmpresaIdAndVigenteTrueAndDeletedFalseOrderByFechaPublicacionDesc(Long empresaId);

    List<Recibo> findByEmpleadoIdAndVigenteTrueAndDeletedFalseOrderByFechaPublicacionDesc(Long empleadoId);

    List<Recibo> findByEmpleadoIdAndDeletedFalseOrderByFechaPublicacionDesc(Long empleadoId);

    Optional<Recibo> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    boolean existsByArchivo_IdAndDeletedFalse(Long archivoId);

    boolean existsByArchivo_IdAndEmpleado_Usuario_IdAndDeletedFalse(Long archivoId, Long usuarioId);

    List<Recibo> findByArchivoIsNotNullAndHashPdfIsNotNullAndDeletedFalse();

    boolean existsByEmpleadoIdAndPeriodoAndVigenteTrueAndDeletedFalse(Long empleadoId, String periodo);

    List<Recibo> findByReciboAnteriorIdOrderByVersionAsc(Long reciboAnteriorId);

    List<Recibo> findByEmpresaIdAndVigenteTrueAndEstadoInAndDeletedFalseAndFechaPublicacionBefore(
            Long empresaId, Collection<EstadoRecibo> estados, LocalDateTime antes);

    List<Recibo> findByEmpresaIdAndVigenteTrueAndEstadoAndDeletedFalseOrderByFechaFirmaDesc(
            Long empresaId, EstadoRecibo estado);

    long countByEmpresaIdAndVigenteTrueAndEstadoInAndDeletedFalse(
            Long empresaId, Collection<EstadoRecibo> estados);
}
