package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioPoliticaVacaciones extends JpaRepository<PoliticaVacaciones, Long> {

    List<PoliticaVacaciones> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<PoliticaVacaciones> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<PoliticaVacaciones> findByEmpresaIdAndActivaTrueAndDeletedFalse(Long empresaId);

    @Modifying
    @Query("UPDATE PoliticaVacaciones p SET p.activa = false WHERE p.empresa.id = :empresaId AND p.deleted = false")
    void desactivarTodas(@Param("empresaId") Long empresaId);
}
