package com.rrhh.saas.documento.repositorio;

import com.rrhh.saas.documento.dominio.AsignacionDocumento;
import com.rrhh.saas.documento.dominio.EstadoAsignacionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioAsignacionDocumento extends JpaRepository<AsignacionDocumento, Long> {

    List<AsignacionDocumento> findByEmpresaIdAndDeletedFalseOrderByFechaAsignacionDesc(Long empresaId);

    List<AsignacionDocumento> findByEmpleadoIdAndDeletedFalseOrderByFechaAsignacionDesc(Long empleadoId);

    Optional<AsignacionDocumento> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    @Query("SELECT a FROM AsignacionDocumento a WHERE a.documento.id = :documentoId AND a.deleted = false")
    List<AsignacionDocumento> findByDocumentoId(@Param("documentoId") Long documentoId);

    boolean existsByEmpleadoIdAndDocumentoIdAndDeletedFalse(Long empleadoId, Long documentoId);

    long countByEmpleadoIdAndEstadoNotAndDeletedFalse(Long empleadoId, EstadoAsignacionDocumento estado);

    boolean existsByEmpleado_Usuario_IdAndDocumento_UrlArchivoContainingAndDeletedFalse(
            Long usuarioId, String fragmentoUrl);

    @Query("""
            SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
            FROM FirmaDocumento f
            WHERE f.empleado.usuario.id = :usuarioId
              AND f.deleted = false
              AND f.urlArchivoFirmado LIKE CONCAT('%', :fragmentoUrl, '%')
            """)
    boolean existsFirmaConUrlParaUsuario(@Param("usuarioId") Long usuarioId,
                                         @Param("fragmentoUrl") String fragmentoUrl);
}
