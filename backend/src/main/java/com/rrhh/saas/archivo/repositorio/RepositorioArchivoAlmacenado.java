package com.rrhh.saas.archivo.repositorio;

import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioArchivoAlmacenado extends JpaRepository<ArchivoAlmacenado, Long> {

    Optional<ArchivoAlmacenado> findByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);

    List<ArchivoAlmacenado> findByDeletedFalse();
}
