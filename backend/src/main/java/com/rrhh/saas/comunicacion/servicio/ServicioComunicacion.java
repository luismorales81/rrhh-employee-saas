package com.rrhh.saas.comunicacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comunicacion.dominio.Comunicacion;
import com.rrhh.saas.comunicacion.dominio.DestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dominio.EstadoComunicacion;
import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dto.RespuestaComunicacion;
import com.rrhh.saas.comunicacion.dto.RespuestaDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dto.SolicitudPublicarComunicacion;
import com.rrhh.saas.comunicacion.repositorio.RepositorioComunicacion;
import com.rrhh.saas.comunicacion.repositorio.RepositorioDestinatarioComunicacion;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.organizacion.dominio.Area;
import com.rrhh.saas.organizacion.dominio.Puesto;
import com.rrhh.saas.organizacion.dominio.Sector;
import com.rrhh.saas.organizacion.repositorio.RepositorioArea;
import com.rrhh.saas.organizacion.repositorio.RepositorioPuesto;
import com.rrhh.saas.organizacion.repositorio.RepositorioSector;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServicioComunicacion {

    private final RepositorioComunicacion repositorioComunicacion;
    private final RepositorioDestinatarioComunicacion repositorioDestinatario;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioArea repositorioArea;
    private final RepositorioSector repositorioSector;
    private final RepositorioPuesto repositorioPuesto;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioComunicacion(RepositorioComunicacion repositorioComunicacion,
                                RepositorioDestinatarioComunicacion repositorioDestinatario,
                                RepositorioEmpleado repositorioEmpleado,
                                RepositorioArea repositorioArea,
                                RepositorioSector repositorioSector,
                                RepositorioPuesto repositorioPuesto,
                                ServicioEmpleado servicioEmpleado,
                                ServicioEmpresa servicioEmpresa,
                                ServicioAuditoria servicioAuditoria,
                                ApplicationEventPublisher publicadorEventos) {
        this.repositorioComunicacion = repositorioComunicacion;
        this.repositorioDestinatario = repositorioDestinatario;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioArea = repositorioArea;
        this.repositorioSector = repositorioSector;
        this.repositorioPuesto = repositorioPuesto;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public RespuestaComunicacion publicar(SolicitudPublicarComunicacion solicitud) {
        if (solicitud.fechaPublicacionProgramada() != null) {
            if (!solicitud.fechaPublicacionProgramada().isAfter(LocalDateTime.now())) {
                throw new ExcepcionNegocio("La fecha programada debe ser futura");
            }
            return programar(solicitud);
        }
        return publicarInmediata(solicitud);
    }

    @Transactional
    public RespuestaComunicacion programar(SolicitudPublicarComunicacion solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        validarDestinatario(solicitud, empresa.getId());

        Comunicacion comunicacion = new Comunicacion();
        comunicacion.setEmpresa(empresa);
        comunicacion.setTitulo(solicitud.titulo());
        comunicacion.setMensaje(solicitud.mensaje());
        comunicacion.setTipoDestinatario(solicitud.tipoDestinatario());
        comunicacion.setReferenciaDestinatarioId(solicitud.referenciaDestinatarioId());
        comunicacion.setRequiereConfirmacion(solicitud.requiereConfirmacion());
        comunicacion.setEstado(EstadoComunicacion.PROGRAMADA);
        comunicacion.setFechaPublicacionProgramada(solicitud.fechaPublicacionProgramada());

        Comunicacion guardada = repositorioComunicacion.save(comunicacion);
        servicioAuditoria.registrar("PROGRAMAR", "Comunicacion", guardada.getId(), null, guardada.getTitulo());
        return mapearComunicacion(guardada);
    }

    @Transactional
    public void publicarPendientes() {
        List<Comunicacion> pendientes = repositorioComunicacion
                .findByEstadoAndFechaPublicacionProgramadaLessThanEqualAndDeletedFalse(
                        EstadoComunicacion.PROGRAMADA, LocalDateTime.now());
        for (Comunicacion comunicacion : pendientes) {
            ejecutarPublicacion(comunicacion);
        }
    }

    @Transactional
    public RespuestaComunicacion cancelarProgramada(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Comunicacion comunicacion = obtenerEntidad(id, empresaId);
        if (comunicacion.getEstado() != EstadoComunicacion.PROGRAMADA) {
            throw new ExcepcionNegocio("Solo se pueden cancelar comunicaciones programadas");
        }
        comunicacion.setEstado(EstadoComunicacion.CANCELADA);
        Comunicacion guardada = repositorioComunicacion.save(comunicacion);
        servicioAuditoria.registrar("CANCELAR", "Comunicacion", guardada.getId(), null, guardada.getTitulo());
        return mapearComunicacion(guardada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaComunicacion> listarProgramadas() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioComunicacion
                .findByEmpresaIdAndEstadoAndDeletedFalseOrderByFechaPublicacionProgramadaAsc(
                        empresaId, EstadoComunicacion.PROGRAMADA)
                .stream().map(this::mapearComunicacion).toList();
    }

    private RespuestaComunicacion publicarInmediata(SolicitudPublicarComunicacion solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        validarDestinatario(solicitud, empresa.getId());

        Comunicacion comunicacion = new Comunicacion();
        comunicacion.setEmpresa(empresa);
        comunicacion.setTitulo(solicitud.titulo());
        comunicacion.setMensaje(solicitud.mensaje());
        comunicacion.setTipoDestinatario(solicitud.tipoDestinatario());
        comunicacion.setReferenciaDestinatarioId(solicitud.referenciaDestinatarioId());
        comunicacion.setRequiereConfirmacion(solicitud.requiereConfirmacion());
        comunicacion.setEstado(EstadoComunicacion.PUBLICADA);
        comunicacion.setFechaPublicacion(LocalDateTime.now());

        Comunicacion guardada = repositorioComunicacion.save(comunicacion);
        ejecutarPublicacion(guardada);
        return mapearComunicacion(guardada);
    }

    private void ejecutarPublicacion(Comunicacion comunicacion) {
        Empresa empresa = comunicacion.getEmpresa();
        List<Empleado> empleados = resolverEmpleadosDestinatarios(
                empresa.getId(), comunicacion.getTipoDestinatario(), comunicacion.getReferenciaDestinatarioId());

        if (empleados.isEmpty()) {
            throw new ExcepcionNegocio("No hay empleados para el destinatario seleccionado");
        }

        Set<Long> empleadosAgregados = new HashSet<>();
        for (Empleado empleado : empleados) {
            if (!empleadosAgregados.add(empleado.getId())) {
                continue;
            }
            DestinatarioComunicacion destinatario = new DestinatarioComunicacion();
            destinatario.setEmpresa(empresa);
            destinatario.setComunicacion(comunicacion);
            destinatario.setEmpleado(empleado);
            repositorioDestinatario.save(destinatario);
        }

        comunicacion.setEstado(EstadoComunicacion.PUBLICADA);
        comunicacion.setFechaPublicacion(LocalDateTime.now());
        repositorioComunicacion.save(comunicacion);

        servicioAuditoria.registrar("PUBLICAR", "Comunicacion", comunicacion.getId(), null, comunicacion.getTitulo());
        publicadorEventos.publishEvent(new EventoComunicacionPublicada(
                comunicacion.getId(), empresa.getId(), empleadosAgregados.size()));
    }

    @Transactional
    public RespuestaDestinatarioComunicacion confirmarLectura(Long destinatarioId) {
        Empresa empresa = obtenerEmpresaActual();
        DestinatarioComunicacion destinatario = obtenerDestinatario(destinatarioId, empresa.getId());
        validarPropietario(destinatario);

        LocalDateTime ahora = LocalDateTime.now();
        if (destinatario.getFechaLectura() == null) {
            destinatario.setFechaLectura(ahora);
        }
        if (destinatario.getComunicacion().isRequiereConfirmacion()
                && destinatario.getFechaConfirmacion() == null) {
            destinatario.setFechaConfirmacion(ahora);
        }

        DestinatarioComunicacion actualizado = repositorioDestinatario.save(destinatario);
        servicioAuditoria.registrar("CONFIRMAR_LECTURA", "DestinatarioComunicacion",
                actualizado.getId(), null, destinatario.getComunicacion().getTitulo());
        publicadorEventos.publishEvent(new EventoComunicacionLeida(
                actualizado.getComunicacion().getId(),
                destinatario.getEmpleado().getId(),
                empresa.getId()));
        return mapearDestinatario(actualizado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaComunicacion> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioComunicacion.findByEmpresaIdAndDeletedFalseOrderByFechaPublicacionDesc(empresaId)
                .stream().map(this::mapearComunicacion).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaComunicacion obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Comunicacion comunicacion = obtenerEntidad(id, empresaId);
        return mapearComunicacion(comunicacion);
    }

    @Transactional(readOnly = true)
    public List<RespuestaDestinatarioComunicacion> listarDestinatarios(Long comunicacionId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        obtenerEntidad(comunicacionId, empresaId);
        return repositorioDestinatario.findByComunicacionIdAndDeletedFalse(comunicacionId).stream()
                .map(this::mapearDestinatario).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaDestinatarioComunicacion> listarMisComunicaciones() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no definido en contexto", 403);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return repositorioDestinatario.findByEmpleadoIdAndDeletedFalseOrderByCreatedAtDesc(empleado.getId())
                .stream().map(this::mapearDestinatario).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaDestinatarioComunicacion obtenerDestinatarioPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return mapearDestinatario(obtenerDestinatario(id, empresaId));
    }

    private Comunicacion obtenerEntidad(Long id, Long empresaId) {
        return repositorioComunicacion.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Comunicación no encontrada", 404));
    }

    private DestinatarioComunicacion obtenerDestinatario(Long id, Long empresaId) {
        return repositorioDestinatario.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Destinatario de comunicación no encontrado", 404));
    }

    private void validarDestinatario(SolicitudPublicarComunicacion solicitud, Long empresaId) {
        switch (solicitud.tipoDestinatario()) {
            case EMPRESA -> {
                if (solicitud.referenciaDestinatarioId() != null) {
                    throw new ExcepcionNegocio("El destinatario EMPRESA no requiere referencia");
                }
            }
            case AREA -> validarArea(solicitud.referenciaDestinatarioId(), empresaId);
            case SECTOR -> validarSector(solicitud.referenciaDestinatarioId(), empresaId);
            case PUESTO -> validarPuesto(solicitud.referenciaDestinatarioId(), empresaId);
            case EMPLEADO -> servicioEmpleado.obtenerEmpleadoEntidad(
                    solicitud.referenciaDestinatarioId(), empresaId);
        }
    }

    private void validarArea(Long id, Long empresaId) {
        if (id == null) {
            throw new ExcepcionNegocio("Debe indicar el área destinatario");
        }
        repositorioArea.findById(id)
                .filter(a -> !a.isDeleted() && a.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Área no encontrada", 404));
    }

    private void validarSector(Long id, Long empresaId) {
        if (id == null) {
            throw new ExcepcionNegocio("Debe indicar el sector destinatario");
        }
        repositorioSector.findById(id)
                .filter(s -> !s.isDeleted() && s.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Sector no encontrado", 404));
    }

    private void validarPuesto(Long id, Long empresaId) {
        if (id == null) {
            throw new ExcepcionNegocio("Debe indicar el puesto destinatario");
        }
        repositorioPuesto.findById(id)
                .filter(p -> !p.isDeleted() && p.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Puesto no encontrado", 404));
    }

    private List<Empleado> resolverEmpleadosDestinatarios(Long empresaId,
                                                            TipoDestinatarioComunicacion tipo,
                                                            Long referenciaId) {
        return switch (tipo) {
            case EMPRESA -> repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId);
            case AREA -> repositorioEmpleado.findByEmpresaIdAndAreaIdAndDeletedFalse(empresaId, referenciaId);
            case SECTOR -> repositorioEmpleado.findByEmpresaIdAndSectorIdAndDeletedFalse(empresaId, referenciaId);
            case PUESTO -> repositorioEmpleado.findByEmpresaIdAndPuestoIdAndDeletedFalse(empresaId, referenciaId);
            case EMPLEADO -> List.of(servicioEmpleado.obtenerEmpleadoEntidad(referenciaId, empresaId));
        };
    }

    private void validarPropietario(DestinatarioComunicacion destinatario) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no definido en contexto", 403);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        if (!destinatario.getEmpleado().getId().equals(empleado.getId())) {
            throw new ExcepcionNegocio("Solo el destinatario puede confirmar la lectura", 403);
        }
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaComunicacion mapearComunicacion(Comunicacion comunicacion) {
        long total = repositorioDestinatario.countByComunicacionIdAndDeletedFalse(comunicacion.getId());
        long leidos = repositorioDestinatario.countByComunicacionIdAndFechaLecturaIsNotNullAndDeletedFalse(
                comunicacion.getId());
        long confirmados = repositorioDestinatario.findByComunicacionIdAndDeletedFalse(comunicacion.getId())
                .stream().filter(DestinatarioComunicacion::estaConfirmada).count();
        return new RespuestaComunicacion(
                comunicacion.getId(),
                comunicacion.getTitulo(),
                comunicacion.getMensaje(),
                comunicacion.getTipoDestinatario(),
                comunicacion.getReferenciaDestinatarioId(),
                comunicacion.isRequiereConfirmacion(),
                comunicacion.getEstado(),
                comunicacion.getFechaPublicacion(),
                comunicacion.getFechaPublicacionProgramada(),
                total,
                leidos,
                confirmados
        );
    }

    private RespuestaDestinatarioComunicacion mapearDestinatario(DestinatarioComunicacion destinatario) {
        Comunicacion comunicacion = destinatario.getComunicacion();
        Empleado empleado = destinatario.getEmpleado();
        return new RespuestaDestinatarioComunicacion(
                destinatario.getId(),
                comunicacion.getId(),
                comunicacion.getTitulo(),
                comunicacion.getMensaje(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                comunicacion.isRequiereConfirmacion(),
                comunicacion.getFechaPublicacion(),
                destinatario.getFechaLectura(),
                destinatario.getFechaConfirmacion(),
                destinatario.estaLeida(),
                destinatario.estaConfirmada()
        );
    }

    public record EventoComunicacionPublicada(Long comunicacionId, Long empresaId, int cantidadDestinatarios) {
    }

    public record EventoComunicacionLeida(Long comunicacionId, Long empleadoId, Long empresaId) {
    }
}
