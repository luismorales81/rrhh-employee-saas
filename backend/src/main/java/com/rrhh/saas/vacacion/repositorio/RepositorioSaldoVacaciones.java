package com.rrhh.saas.vacacion.repositorio;

import com.rrhh.saas.vacacion.dominio.SaldoVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioSaldoVacaciones extends JpaRepository<SaldoVacaciones, Long> {

    Optional<SaldoVacaciones> findByEmpleadoIdAndAnioAndDeletedFalse(Long empleadoId, int anio);

    List<SaldoVacaciones> findByEmpleadoIdAndDeletedFalseOrderByAnioDesc(Long empleadoId);

    List<SaldoVacaciones> findByEmpleadoEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<SaldoVacaciones> findByEmpleadoIdAndDeletedFalse(Long empleadoId);
}
