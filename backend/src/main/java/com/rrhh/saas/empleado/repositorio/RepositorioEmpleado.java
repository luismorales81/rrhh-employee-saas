package com.rrhh.saas.empleado.repositorio;

import com.rrhh.saas.empleado.dominio.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioEmpleado extends JpaRepository<Empleado, Long> {

    List<Empleado> findByEmpresaIdAndDeletedFalse(Long empresaId);

    List<Empleado> findByEmpresaIdAndAreaIdAndDeletedFalse(Long empresaId, Long areaId);

    List<Empleado> findByEmpresaIdAndSectorIdAndDeletedFalse(Long empresaId, Long sectorId);

    List<Empleado> findByEmpresaIdAndPuestoIdAndDeletedFalse(Long empresaId, Long puestoId);

    Optional<Empleado> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<Empleado> findByUsuarioIdAndDeletedFalse(Long usuarioId);

    long countByEmpresaIdAndGerenciaIdAndDeletedFalse(Long empresaId, Long gerenciaId);

    long countByEmpresaIdAndAreaIdAndDeletedFalse(Long empresaId, Long areaId);

    long countByEmpresaIdAndPuestoIdAndDeletedFalse(Long empresaId, Long puestoId);
}
