package com.rrhh.saas.certificado.repositorio;



import com.rrhh.saas.certificado.dominio.Certificado;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;



import java.time.LocalDate;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Optional;



public interface RepositorioCertificado extends JpaRepository<Certificado, Long> {



    Optional<Certificado> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);



    @Query("""

            SELECT c FROM Certificado c

            LEFT JOIN FETCH c.empleado

            WHERE c.id = :id AND c.empresa.id = :empresaId AND c.deleted = false

            """)

    Optional<Certificado> findByIdAndEmpresaIdAndDeletedFalseConDatos(

            @Param("id") Long id,

            @Param("empresaId") Long empresaId);



    @Query("""

            SELECT c FROM Certificado c

            JOIN FETCH c.empleado

            WHERE c.empleado.id = :empleadoId

              AND c.empresa.id = :empresaId

              AND c.deleted = false

            ORDER BY c.createdAt DESC

            """)

    List<Certificado> findByEmpleadoIdAndEmpresaIdAndDeletedFalseOrderByCreatedAtDescConDatos(

            @Param("empleadoId") Long empleadoId,

            @Param("empresaId") Long empresaId);



    @Query("""

            SELECT c FROM Certificado c

            WHERE c.empleado.id = :empleadoId

              AND c.empresa.id = :empresaId

              AND c.deleted = false

              AND c.createdAt >= :desde

              AND c.estado IN :estados

            ORDER BY c.createdAt DESC

            """)

    List<Certificado> buscarHistorialPeriodo(

            @Param("empleadoId") Long empleadoId,

            @Param("empresaId") Long empresaId,

            @Param("desde") LocalDateTime desde,

            @Param("estados") List<EstadoCertificado> estados);



    @Query("""

            SELECT DISTINCT c FROM Certificado c

            LEFT JOIN FETCH c.empleado

            WHERE c.empresa.id = :empresaId

              AND c.deleted = false

              AND c.estado = :estado

            """)

    List<Certificado> buscarPorEmpresaYEstadoConDatos(

            @Param("empresaId") Long empresaId,

            @Param("estado") EstadoCertificado estado);



    @Query("""

            SELECT c FROM Certificado c

            JOIN FETCH c.empleado

            WHERE c.empresa.id = :empresaId

              AND c.deleted = false

              AND c.estado IN :estados

            """)

    List<Certificado> buscarBandejaPorEstados(

            @Param("empresaId") Long empresaId,

            @Param("estados") List<EstadoCertificado> estados);



    @Query("""

            SELECT DISTINCT c FROM Certificado c

            JOIN FETCH c.empleado

            WHERE c.empresa.id = :empresaId

              AND c.deleted = false

              AND c.estado = com.rrhh.saas.certificado.dominio.EstadoCertificado.VALIDADO

              AND EXISTS (

                  SELECT 1 FROM DatosExtraidosCertificado d

                  WHERE d.certificado = c

                    AND d.esVigente = true

                    AND d.deleted = false

                    AND d.fechaEmision BETWEEN :desde AND :hasta

              )

            """)

    List<Certificado> buscarValidadosPorRangoEmision(

            @Param("empresaId") Long empresaId,

            @Param("desde") LocalDate desde,

            @Param("hasta") LocalDate hasta);



    @Query("""

            SELECT c FROM Certificado c

            JOIN FETCH c.empresa

            WHERE c.estado = com.rrhh.saas.certificado.dominio.EstadoCertificado.EN_PROCESAMIENTO

              AND c.deleted = false

              AND (c.ultimoIntentoExtraccion IS NULL OR c.ultimoIntentoExtraccion < :limite)

            """)

    List<Certificado> buscarColgadosEnProcesamiento(@Param("limite") LocalDateTime limite);

    boolean existsByArchivoIdAndEmpleado_Usuario_IdAndDeletedFalse(Long archivoId, Long usuarioId);

}


