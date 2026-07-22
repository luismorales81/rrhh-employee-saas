package com.rrhh.saas.ausentismo.repositorio;

import com.rrhh.saas.ausentismo.dominio.TipoAusencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioTipoAusencia extends JpaRepository<TipoAusencia, Long> {

    List<TipoAusencia> findByEmpresaIdAndDeletedFalse(Long empresaId);

    Optional<TipoAusencia> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    Optional<TipoAusencia> findByEmpresaIdAndNombreAndDeletedFalse(Long empresaId, String nombre);
}
