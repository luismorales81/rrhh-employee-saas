package com.rrhh.saas.empleado.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dominio.EstadoEmpleado;
import com.rrhh.saas.empleado.dominio.LegajoEmpleado;
import com.rrhh.saas.empleado.dto.RespuestaEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudActualizarEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudCrearEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioLegajoEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import com.rrhh.saas.organizacion.dominio.Area;
import com.rrhh.saas.organizacion.dominio.Gerencia;
import com.rrhh.saas.organizacion.dominio.Puesto;
import com.rrhh.saas.organizacion.dominio.Sector;
import com.rrhh.saas.organizacion.repositorio.RepositorioArea;
import com.rrhh.saas.organizacion.repositorio.RepositorioGerencia;
import com.rrhh.saas.organizacion.repositorio.RepositorioPuesto;
import com.rrhh.saas.organizacion.repositorio.RepositorioSector;
import com.rrhh.saas.offboarding.servicio.ServicioOffboarding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioEmpleado {

    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioLegajoEmpleado repositorioLegajo;
    private final RepositorioGerencia repositorioGerencia;
    private final RepositorioArea repositorioArea;
    private final RepositorioSector repositorioSector;
    private final RepositorioPuesto repositorioPuesto;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioUsuario servicioUsuario;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;
    private final ServicioOffboarding servicioOffboarding;

    public ServicioEmpleado(RepositorioEmpleado repositorioEmpleado,
                            RepositorioLegajoEmpleado repositorioLegajo,
                            RepositorioGerencia repositorioGerencia,
                            RepositorioArea repositorioArea,
                            RepositorioSector repositorioSector,
                            RepositorioPuesto repositorioPuesto,
                            ServicioEmpresa servicioEmpresa,
                            ServicioUsuario servicioUsuario,
                            ServicioAuditoria servicioAuditoria,
                            ApplicationEventPublisher publicadorEventos,
                            @Lazy ServicioOffboarding servicioOffboarding) {
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioLegajo = repositorioLegajo;
        this.repositorioGerencia = repositorioGerencia;
        this.repositorioArea = repositorioArea;
        this.repositorioSector = repositorioSector;
        this.repositorioPuesto = repositorioPuesto;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioUsuario = servicioUsuario;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
        this.servicioOffboarding = servicioOffboarding;
    }

    @Transactional
    public RespuestaEmpleado crear(SolicitudCrearEmpleado solicitud) {
        Empresa empresa = obtenerEmpresaActual();

        Empleado empleado = new Empleado();
        empleado.setEmpresa(empresa);
        empleado.setNombre(solicitud.nombre());
        empleado.setApellido(solicitud.apellido());
        empleado.setNumeroEmpleado(solicitud.numeroEmpleado());
        empleado.setDni(solicitud.dni());
        empleado.setCuil(solicitud.cuil());
        empleado.setFechaNacimiento(solicitud.fechaNacimiento());
        empleado.setFechaIngreso(solicitud.fechaIngreso());
        empleado.setTelefono(solicitud.telefono());
        empleado.setEmailPersonal(solicitud.emailPersonal());
        empleado.setEmailLaboral(solicitud.emailLaboral());
        empleado.setEstado(EstadoEmpleado.ACTIVO);

        if (solicitud.supervisorId() != null) {
            Empleado supervisor = obtenerEmpleadoEntidad(solicitud.supervisorId(), empresa.getId());
            empleado.setSupervisor(supervisor);
        }
        if (solicitud.gerenciaId() != null) {
            empleado.setGerencia(obtenerGerencia(solicitud.gerenciaId(), empresa.getId()));
        }
        if (solicitud.areaId() != null) {
            empleado.setArea(obtenerArea(solicitud.areaId(), empresa.getId()));
        }
        if (solicitud.sectorId() != null) {
            empleado.setSector(obtenerSector(solicitud.sectorId(), empresa.getId()));
        }
        if (solicitud.puestoId() != null) {
            empleado.setPuesto(obtenerPuesto(solicitud.puestoId(), empresa.getId()));
        }
        if (solicitud.usuarioId() != null) {
            Usuario usuario = servicioUsuario.obtenerEntidad(solicitud.usuarioId());
            if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresa.getId())) {
                throw new ExcepcionNegocio("El usuario no pertenece a la empresa");
            }
            empleado.setUsuario(usuario);
        }

        Empleado guardado = repositorioEmpleado.save(empleado);

        LegajoEmpleado legajo = new LegajoEmpleado();
        legajo.setEmpleado(guardado);
        LegajoEmpleado legajoGuardado = repositorioLegajo.save(legajo);

        servicioAuditoria.registrar("CREAR", "Empleado", guardado.getId(), null,
                guardado.getNombre() + " " + guardado.getApellido());

        publicadorEventos.publishEvent(new EventoEmpleadoCreado(guardado.getId(), empresa.getId()));

        return mapear(guardado, legajoGuardado.getId());
    }

    @Transactional
    public RespuestaEmpleado actualizar(Long id, SolicitudActualizarEmpleado solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = obtenerEmpleadoEntidad(id, empresa.getId());

        empleado.setNombre(solicitud.nombre());
        empleado.setApellido(solicitud.apellido());
        empleado.setNumeroEmpleado(solicitud.numeroEmpleado());
        empleado.setDni(solicitud.dni());
        empleado.setCuil(solicitud.cuil());
        empleado.setFechaNacimiento(solicitud.fechaNacimiento());
        empleado.setFechaIngreso(solicitud.fechaIngreso());
        empleado.setTelefono(solicitud.telefono());
        empleado.setEmailPersonal(solicitud.emailPersonal());
        empleado.setEmailLaboral(solicitud.emailLaboral());

        if (solicitud.supervisorId() != null) {
            empleado.setSupervisor(obtenerEmpleadoEntidad(solicitud.supervisorId(), empresa.getId()));
        } else {
            empleado.setSupervisor(null);
        }
        if (solicitud.gerenciaId() != null) {
            empleado.setGerencia(obtenerGerencia(solicitud.gerenciaId(), empresa.getId()));
        } else {
            empleado.setGerencia(null);
        }
        if (solicitud.areaId() != null) {
            empleado.setArea(obtenerArea(solicitud.areaId(), empresa.getId()));
        } else {
            empleado.setArea(null);
        }
        if (solicitud.sectorId() != null) {
            empleado.setSector(obtenerSector(solicitud.sectorId(), empresa.getId()));
        } else {
            empleado.setSector(null);
        }
        if (solicitud.puestoId() != null) {
            empleado.setPuesto(obtenerPuesto(solicitud.puestoId(), empresa.getId()));
        } else {
            empleado.setPuesto(null);
        }
        if (solicitud.usuarioId() != null) {
            Usuario usuario = servicioUsuario.obtenerEntidad(solicitud.usuarioId());
            if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresa.getId())) {
                throw new ExcepcionNegocio("El usuario no pertenece a la empresa");
            }
            empleado.setUsuario(usuario);
        }

        Empleado guardado = repositorioEmpleado.save(empleado);
        Long legajoId = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(id)
                .map(LegajoEmpleado::getId).orElse(null);

        servicioAuditoria.registrar("ACTUALIZAR", "Empleado", guardado.getId(), null,
                guardado.getNombre() + " " + guardado.getApellido());

        return mapear(guardado, legajoId);
    }

    @Transactional
    public void desactivar(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = obtenerEmpleadoEntidad(id, empresa.getId());

        servicioOffboarding.generarTareasAlDesactivar(id);

        empleado.setEstado(EstadoEmpleado.DESVINCULADO);
        empleado.setFechaBaja(LocalDate.now());
        repositorioEmpleado.save(empleado);

        servicioAuditoria.registrar("DESACTIVAR", "Empleado", empleado.getId(), null,
                empleado.getNombre() + " " + empleado.getApellido());
    }

    @Transactional(readOnly = true)
    public List<RespuestaEmpleado> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .map(e -> {
                    Long legajoId = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(e.getId())
                            .map(LegajoEmpleado::getId).orElse(null);
                    return mapear(e, legajoId);
                }).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaEmpleado obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = obtenerEmpleadoEntidad(id, empresaId);
        Long legajoId = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(id)
                .map(LegajoEmpleado::getId).orElse(null);
        return mapear(empleado, legajoId);
    }

    public Empleado obtenerEmpleadoEntidad(Long id, Long empresaId) {
        return repositorioEmpleado.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Empleado no encontrado", 404));
    }

    @Transactional(readOnly = true)
    public RespuestaEmpleado obtenerPropio() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = obtenerPorUsuarioId(usuarioId);
        Long legajoId = repositorioLegajo.findByEmpleadoIdAndDeletedFalse(empleado.getId())
                .map(LegajoEmpleado::getId).orElse(null);
        return mapear(empleado, legajoId);
    }

    public Empleado obtenerPorUsuarioId(Long usuarioId) {
        return repositorioEmpleado.findByUsuarioIdAndDeletedFalse(usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Empleado no encontrado para el usuario", 404));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private Gerencia obtenerGerencia(Long id, Long empresaId) {
        return repositorioGerencia.findById(id)
                .filter(g -> !g.isDeleted() && g.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Gerencia no encontrada", 404));
    }

    private Area obtenerArea(Long id, Long empresaId) {
        return repositorioArea.findById(id)
                .filter(a -> !a.isDeleted() && a.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Área no encontrada", 404));
    }

    private Sector obtenerSector(Long id, Long empresaId) {
        return repositorioSector.findById(id)
                .filter(s -> !s.isDeleted() && s.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Sector no encontrado", 404));
    }

    private Puesto obtenerPuesto(Long id, Long empresaId) {
        return repositorioPuesto.findById(id)
                .filter(p -> !p.isDeleted() && p.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Puesto no encontrado", 404));
    }

    private RespuestaEmpleado mapear(Empleado empleado, Long legajoId) {
        return new RespuestaEmpleado(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getNumeroEmpleado(),
                empleado.getDni(),
                empleado.getCuil(),
                empleado.getFechaIngreso(),
                empleado.getEstado(),
                empleado.getEmailLaboral(),
                empleado.getUsuario() != null ? empleado.getUsuario().getId() : null,
                empleado.getSupervisor() != null ? empleado.getSupervisor().getId() : null,
                empleado.getGerencia() != null ? empleado.getGerencia().getId() : null,
                empleado.getArea() != null ? empleado.getArea().getId() : null,
                empleado.getSector() != null ? empleado.getSector().getId() : null,
                empleado.getPuesto() != null ? empleado.getPuesto().getId() : null,
                legajoId
        );
    }

    public record EventoEmpleadoCreado(Long empleadoId, Long empresaId) {
    }
}
