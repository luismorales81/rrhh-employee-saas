package com.rrhh.saas.notificacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.notificacion.dominio.Notificacion;
import com.rrhh.saas.notificacion.dto.RespuestaNotificacion;
import com.rrhh.saas.notificacion.repositorio.RepositorioNotificacion;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioNotificacion {

    private final RepositorioNotificacion repositorio;
    private final ServicioUsuario servicioUsuario;

    public ServicioNotificacion(RepositorioNotificacion repositorio, ServicioUsuario servicioUsuario) {
        this.repositorio = repositorio;
        this.servicioUsuario = servicioUsuario;
    }

    @Transactional
    public void crear(Long empresaId, Long usuarioId, String titulo, String mensaje, String tipo, Long referenciaId) {
        Usuario usuario = servicioUsuario.obtenerEntidad(usuarioId);
        Notificacion notificacion = new Notificacion();
        notificacion.setEmpresaId(empresaId);
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setReferenciaId(referenciaId);
        repositorio.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<RespuestaNotificacion> listarPropias() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        return repositorio.findByUsuarioIdAndDeletedFalseOrderByCreatedAtDesc(usuarioId).stream()
                .map(this::mapear).toList();
    }

    @Transactional
    public RespuestaNotificacion marcarLeida(Long id) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Notificacion notificacion = repositorio.findByIdAndUsuarioIdAndDeletedFalse(id, usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Notificación no encontrada", 404));
        notificacion.setLeida(true);
        return mapear(repositorio.save(notificacion));
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        return repositorio.countByUsuarioIdAndLeidaFalseAndDeletedFalse(usuarioId);
    }

    private RespuestaNotificacion mapear(Notificacion notificacion) {
        return new RespuestaNotificacion(
                notificacion.getId(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.getReferenciaId(),
                notificacion.isLeida(),
                notificacion.getCreatedAt()
        );
    }
}
