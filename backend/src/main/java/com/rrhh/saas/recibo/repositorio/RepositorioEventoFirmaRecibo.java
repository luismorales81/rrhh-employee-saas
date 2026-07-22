package com.rrhh.saas.recibo.repositorio;

import com.rrhh.saas.recibo.dominio.EventoFirmaRecibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioEventoFirmaRecibo extends JpaRepository<EventoFirmaRecibo, Long> {

    List<EventoFirmaRecibo> findByReciboIdOrderByTimestampEventoAsc(Long reciboId);
}
