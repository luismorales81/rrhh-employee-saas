package com.rrhh.saas.certificado.servicio;

import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.EventoCertificado;
import com.rrhh.saas.certificado.repositorio.RepositorioEventoCertificado;
import com.rrhh.saas.comun.utilidad.UtilidadClienteHttp;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioEventoCertificado {

    private final RepositorioEventoCertificado repositorio;

    public ServicioEventoCertificado(RepositorioEventoCertificado repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void registrar(Certificado certificado,
                          EstadoCertificado estadoAnterior,
                          EstadoCertificado estadoNuevo,
                          Long actorUsuarioId,
                          String observaciones,
                          HttpServletRequest peticion) {
        EventoCertificado evento = new EventoCertificado();
        evento.setCertificadoId(certificado.getId());
        evento.setEmpresaId(certificado.getEmpresa().getId());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(estadoNuevo);
        evento.setActorUsuarioId(actorUsuarioId);
        evento.setObservaciones(observaciones);
        evento.setDireccionIp(UtilidadClienteHttp.obtenerDireccionIp(peticion));
        evento.setUserAgent(UtilidadClienteHttp.obtenerUserAgent(peticion));
        repositorio.save(evento);
    }
}
