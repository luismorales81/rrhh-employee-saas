package com.rrhh.saas.certificado.repositorio;

import com.rrhh.saas.certificado.dominio.EventoCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioEventoCertificado extends JpaRepository<EventoCertificado, Long> {

    List<EventoCertificado> findByCertificadoIdOrderByTimestampUtcAsc(Long certificadoId);
}
