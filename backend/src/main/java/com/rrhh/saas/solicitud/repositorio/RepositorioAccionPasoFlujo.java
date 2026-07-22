package com.rrhh.saas.solicitud.repositorio;

import com.rrhh.saas.solicitud.dominio.AccionPasoFlujo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioAccionPasoFlujo extends JpaRepository<AccionPasoFlujo, Long> {
}
