package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.utilidad.UtilidadClienteHttp;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.vacacion.dominio.EventoSolicitudVacaciones;
import com.rrhh.saas.vacacion.dominio.SolicitudVacaciones;
import com.rrhh.saas.vacacion.dominio.TipoEventoSolicitudVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioEventoSolicitudVacaciones;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioEventoSolicitudVacaciones {

    private final RepositorioEventoSolicitudVacaciones repositorio;

    public ServicioEventoSolicitudVacaciones(RepositorioEventoSolicitudVacaciones repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void registrar(SolicitudVacaciones solicitudVacaciones,
                          TipoEventoSolicitudVacaciones tipo,
                          EstadoSolicitud estadoAnterior,
                          EstadoSolicitud estadoNuevo,
                          Long usuarioId,
                          String comentario,
                          HttpServletRequest peticion) {
        EventoSolicitudVacaciones evento = new EventoSolicitudVacaciones();
        evento.setSolicitudVacacionesId(solicitudVacaciones.getId());
        evento.setSolicitudId(solicitudVacaciones.getSolicitud().getId());
        evento.setEmpresaId(solicitudVacaciones.getSolicitud().getEmpresa().getId());
        evento.setUsuarioId(usuarioId);
        evento.setTipoEvento(tipo);
        evento.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);
        evento.setEstadoNuevo(estadoNuevo != null ? estadoNuevo.name() : null);
        evento.setComentario(comentario);
        evento.setDireccionIp(UtilidadClienteHttp.obtenerDireccionIp(peticion));
        evento.setUserAgent(UtilidadClienteHttp.obtenerUserAgent(peticion));
        repositorio.save(evento);
    }
}
