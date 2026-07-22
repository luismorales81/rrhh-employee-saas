package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.solicitud.dominio.AccionPaso;
import com.rrhh.saas.solicitud.dominio.AccionPasoFlujo;
import com.rrhh.saas.solicitud.dominio.AprobacionSolicitud;
import com.rrhh.saas.solicitud.dominio.EstadoInstanciaFlujo;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.InstanciaFlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.PasoFlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.Solicitud;
import com.rrhh.saas.solicitud.dominio.TipoPaso;
import com.rrhh.saas.solicitud.repositorio.RepositorioAccionPasoFlujo;
import com.rrhh.saas.solicitud.repositorio.RepositorioAprobacionSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioFlujoTrabajo;
import com.rrhh.saas.solicitud.repositorio.RepositorioInstanciaFlujoTrabajo;
import com.rrhh.saas.solicitud.repositorio.RepositorioPasoFlujoTrabajo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicioMotorWorkflow {

    private final RepositorioInstanciaFlujoTrabajo repositorioInstancia;
    private final RepositorioPasoFlujoTrabajo repositorioPaso;
    private final RepositorioFlujoTrabajo repositorioFlujo;
    private final RepositorioAccionPasoFlujo repositorioAccion;
    private final RepositorioAprobacionSolicitud repositorioAprobacion;
    private final ResolvedorAprobador resolvedorAprobador;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioMotorWorkflow(RepositorioInstanciaFlujoTrabajo repositorioInstancia,
                                 RepositorioPasoFlujoTrabajo repositorioPaso,
                                 RepositorioFlujoTrabajo repositorioFlujo,
                                 RepositorioAccionPasoFlujo repositorioAccion,
                                 RepositorioAprobacionSolicitud repositorioAprobacion,
                                 ResolvedorAprobador resolvedorAprobador,
                                 RepositorioUsuario repositorioUsuario,
                                 ServicioAuditoria servicioAuditoria,
                                 ApplicationEventPublisher publicadorEventos) {
        this.repositorioInstancia = repositorioInstancia;
        this.repositorioPaso = repositorioPaso;
        this.repositorioFlujo = repositorioFlujo;
        this.repositorioAccion = repositorioAccion;
        this.repositorioAprobacion = repositorioAprobacion;
        this.resolvedorAprobador = resolvedorAprobador;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public InstanciaFlujoTrabajo iniciar(Solicitud solicitud, FlujoTrabajo flujo) {
        FlujoTrabajo flujoConPasos = repositorioFlujo
                .buscarConPasos(flujo.getId(), solicitud.getEmpresa().getId())
                .orElseThrow(() -> new ExcepcionNegocio("Flujo de trabajo no encontrado", 404));

        List<PasoFlujoTrabajo> pasos = flujoConPasos.getPasos().stream()
                .filter(p -> !p.isDeleted())
                .sorted(Comparator.comparingInt(PasoFlujoTrabajo::getOrden))
                .toList();

        if (pasos.isEmpty()) {
            throw new ExcepcionNegocio("El flujo de trabajo no tiene pasos configurados");
        }

        PasoFlujoTrabajo primerPaso = pasos.get(0);
        Usuario aprobador = resolvedorAprobador.resolver(primerPaso, solicitud.getEmpleado());

        InstanciaFlujoTrabajo instancia = new InstanciaFlujoTrabajo();
        instancia.setEmpresa(solicitud.getEmpresa());
        instancia.setSolicitud(solicitud);
        instancia.setFlujoTrabajo(flujoConPasos);
        instancia.setPasoActualOrden(primerPaso.getOrden());
        instancia.setAprobadorActual(aprobador);
        instancia.setEstado(EstadoInstanciaFlujo.EN_PROGRESO);

        InstanciaFlujoTrabajo guardada = repositorioInstancia.save(instancia);

        solicitud.setEstado(EstadoSolicitud.EN_REVISION);
        publicadorEventos.publishEvent(new EventoWorkflowIniciado(
                guardada.getId(), solicitud.getId(), flujoConPasos.getId(), solicitud.getEmpresa().getId()));
        publicadorEventos.publishEvent(new EventoPasoAsignado(
                guardada.getId(), primerPaso.getId(), aprobador.getId(), solicitud.getEmpresa().getId()));

        return guardada;
    }

    @Transactional
    public Solicitud procesarAccion(Long solicitudId, AccionPaso accion, String comentarios, Usuario usuarioActual) {
        InstanciaFlujoTrabajo instancia = repositorioInstancia.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ExcepcionNegocio("No hay flujo activo para esta solicitud", 404));

        if (instancia.getEstado() != EstadoInstanciaFlujo.EN_PROGRESO) {
            throw new ExcepcionNegocio("El flujo ya fue finalizado");
        }

        if (instancia.getAprobadorActual() == null
                || !instancia.getAprobadorActual().getId().equals(usuarioActual.getId())) {
            throw new ExcepcionNegocio("No es el aprobador actual de este paso", 403);
        }

        PasoFlujoTrabajo pasoActual = repositorioPaso
                .findByFlujoTrabajoIdAndOrdenAndDeletedFalse(
                        instancia.getFlujoTrabajo().getId(), instancia.getPasoActualOrden())
                .orElseThrow(() -> new ExcepcionNegocio("Paso actual no encontrado", 404));

        registrarAccion(instancia, pasoActual, usuarioActual, accion, comentarios);
        registrarAprobacion(instancia.getSolicitud(), pasoActual, usuarioActual, accion.name(), comentarios);

        Solicitud solicitud = instancia.getSolicitud();

        return switch (accion) {
            case RECHAZAR -> finalizarRechazo(instancia, solicitud, comentarios);
            case SOLICITAR_CAMBIOS -> solicitarCambios(instancia, solicitud, comentarios);
            case ESCALAR -> escalar(instancia, solicitud, pasoActual);
            case APROBAR -> avanzarAprobacion(instancia, solicitud, pasoActual);
        };
    }

    private Solicitud finalizarRechazo(InstanciaFlujoTrabajo instancia, Solicitud solicitud, String comentarios) {
        instancia.setEstado(EstadoInstanciaFlujo.RECHAZADO);
        solicitud.setEstado(EstadoSolicitud.RECHAZADO);
        repositorioInstancia.save(instancia);
        servicioAuditoria.registrar("RECHAZAR", "Solicitud", solicitud.getId(), null, comentarios);
        publicadorEventos.publishEvent(new EventoSolicitudRechazada(solicitud.getId(), solicitud.getEmpresa().getId()));
        return solicitud;
    }

    private Solicitud solicitarCambios(InstanciaFlujoTrabajo instancia, Solicitud solicitud, String comentarios) {
        instancia.setEstado(EstadoInstanciaFlujo.EN_PROGRESO);
        solicitud.setEstado(EstadoSolicitud.CAMBIOS_SOLICITADOS);
        repositorioInstancia.save(instancia);
        servicioAuditoria.registrar("SOLICITAR_CAMBIOS", "Solicitud", solicitud.getId(), null, comentarios);
        publicadorEventos.publishEvent(new EventoCambiosSolicitados(
                instancia.getId(), solicitud.getId(), solicitud.getEmpresa().getId()));
        return solicitud;
    }

    private Solicitud escalar(InstanciaFlujoTrabajo instancia, Solicitud solicitud, PasoFlujoTrabajo pasoActual) {
        List<Usuario> candidatos = buscarCandidatosEscalacion(pasoActual, solicitud.getEmpleado());
        Usuario actual = instancia.getAprobadorActual();
        Usuario siguiente = candidatos.stream()
                .filter(u -> !u.getId().equals(actual.getId()))
                .findFirst()
                .orElseThrow(() -> new ExcepcionNegocio("No hay aprobador disponible para escalar"));

        instancia.setAprobadorActual(siguiente);
        repositorioInstancia.save(instancia);
        servicioAuditoria.registrar("ESCALAR", "Solicitud", solicitud.getId(), null,
                "Nuevo aprobador: " + siguiente.getEmail());
        publicadorEventos.publishEvent(new EventoPasoEscalado(
                instancia.getId(), pasoActual.getId(), siguiente.getId(), solicitud.getEmpresa().getId()));
        return solicitud;
    }

    private Solicitud avanzarAprobacion(InstanciaFlujoTrabajo instancia, Solicitud solicitud,
                                        PasoFlujoTrabajo pasoActual) {
        List<PasoFlujoTrabajo> pasos = instancia.getFlujoTrabajo().getPasos().stream()
                .filter(p -> !p.isDeleted())
                .sorted(Comparator.comparingInt(PasoFlujoTrabajo::getOrden))
                .toList();

        int indiceActual = -1;
        for (int i = 0; i < pasos.size(); i++) {
            if (pasos.get(i).getOrden() == pasoActual.getOrden()) {
                indiceActual = i;
                break;
            }
        }

        if (indiceActual >= pasos.size() - 1) {
            instancia.setEstado(EstadoInstanciaFlujo.COMPLETADO);
            instancia.setAprobadorActual(null);
            solicitud.setEstado(EstadoSolicitud.APROBADO);
            repositorioInstancia.save(instancia);
            servicioAuditoria.registrar("APROBAR", "Solicitud", solicitud.getId(), null, "Flujo completado");
            publicadorEventos.publishEvent(new EventoSolicitudAprobada(
                    solicitud.getId(), solicitud.getEmpresa().getId()));
            return solicitud;
        }

        PasoFlujoTrabajo siguientePaso = pasos.get(indiceActual + 1);
        Usuario siguienteAprobador = resolvedorAprobador.resolver(siguientePaso, solicitud.getEmpleado());
        instancia.setPasoActualOrden(siguientePaso.getOrden());
        instancia.setAprobadorActual(siguienteAprobador);
        repositorioInstancia.save(instancia);

        publicadorEventos.publishEvent(new EventoPasoAsignado(
                instancia.getId(), siguientePaso.getId(), siguienteAprobador.getId(),
                solicitud.getEmpresa().getId()));
        return solicitud;
    }

    private void registrarAccion(InstanciaFlujoTrabajo instancia, PasoFlujoTrabajo paso,
                                 Usuario usuario, AccionPaso accion, String comentarios) {
        AccionPasoFlujo registro = new AccionPasoFlujo();
        registro.setInstanciaFlujoTrabajo(instancia);
        registro.setPasoFlujoTrabajo(paso);
        registro.setUsuario(usuario);
        registro.setAccion(accion);
        registro.setComentarios(comentarios);
        repositorioAccion.save(registro);
    }

    private void registrarAprobacion(Solicitud solicitud, PasoFlujoTrabajo paso,
                                       Usuario aprobador, String estado, String comentarios) {
        AprobacionSolicitud aprobacion = new AprobacionSolicitud();
        aprobacion.setSolicitud(solicitud);
        aprobacion.setPasoFlujoTrabajo(paso);
        aprobacion.setAprobador(aprobador);
        aprobacion.setEstado(estado);
        aprobacion.setComentarios(comentarios);
        aprobacion.setFechaAprobacion(LocalDateTime.now());
        repositorioAprobacion.save(aprobacion);
    }

    private List<Usuario> buscarCandidatosEscalacion(PasoFlujoTrabajo paso, Empleado solicitante) {
        if (paso.getTipoPaso() == TipoPaso.ROL_ESPECIFICO && paso.getRol() != null) {
            return repositorioUsuario.buscarPorRol(solicitante.getEmpresa().getId(), paso.getRol().getId());
        }
        return List.of(resolvedorAprobador.resolver(paso, solicitante));
    }

    public record EventoWorkflowIniciado(Long instanciaId, Long solicitudId, Long flujoId, Long empresaId) {
    }

    public record EventoPasoAsignado(Long instanciaId, Long pasoId, Long aprobadorId, Long empresaId) {
    }

    public record EventoSolicitudAprobada(Long solicitudId, Long empresaId) {
    }

    public record EventoSolicitudRechazada(Long solicitudId, Long empresaId) {
    }

    public record EventoCambiosSolicitados(Long instanciaId, Long solicitudId, Long empresaId) {
    }

    public record EventoPasoEscalado(Long instanciaId, Long pasoId, Long aprobadorId, Long empresaId) {
    }
}
