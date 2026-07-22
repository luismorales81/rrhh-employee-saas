package com.rrhh.saas.novedad.repositorio;

import com.rrhh.saas.novedad.dominio.EstadoNovedadLiquidacion;
import com.rrhh.saas.novedad.dominio.NovedadLiquidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioNovedadLiquidacion extends JpaRepository<NovedadLiquidacion, Long> {

    List<NovedadLiquidacion> findByEmpresaIdAndDeletedFalseOrderByFechaDesc(Long empresaId);

    List<NovedadLiquidacion> findByEmpresaIdAndPeriodoAndDeletedFalseOrderByFechaDesc(Long empresaId, String periodo);

    List<NovedadLiquidacion> findByEmpresaIdAndPeriodoAndEstadoAndDeletedFalseOrderByFechaDesc(
            Long empresaId, String periodo, EstadoNovedadLiquidacion estado);

    Optional<NovedadLiquidacion> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
