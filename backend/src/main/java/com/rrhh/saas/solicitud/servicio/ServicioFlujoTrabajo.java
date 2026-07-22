package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.PasoFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.RespuestaFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudActualizarFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudCrearFlujoTrabajo;
import com.rrhh.saas.solicitud.repositorio.RepositorioTipoSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioFlujoTrabajo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicioFlujoTrabajo {

    private final RepositorioFlujoTrabajo repositorioFlujo;
    private final RepositorioTipoSolicitud repositorioTipoSolicitud;
    private final RepositorioRol repositorioRol;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioFlujoTrabajo(RepositorioFlujoTrabajo repositorioFlujo,
                                RepositorioTipoSolicitud repositorioTipoSolicitud,
                                RepositorioRol repositorioRol,
                                RepositorioUsuario repositorioUsuario,
                                ServicioEmpresa servicioEmpresa,
                                ServicioAuditoria servicioAuditoria) {
        this.repositorioFlujo = repositorioFlujo;
        this.repositorioTipoSolicitud = repositorioTipoSolicitud;
        this.repositorioRol = repositorioRol;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaFlujoTrabajo crear(SolicitudCrearFlujoTrabajo solicitud) {
        Empresa empresa = obtenerEmpresaActual();

        FlujoTrabajo flujo = new FlujoTrabajo();
        flujo.setEmpresa(empresa);
        flujo.setNombre(solicitud.nombre());
        flujo.setDescripcion(solicitud.descripcion());
        flujo.setActivo(true);

        FlujoTrabajo guardado = repositorioFlujo.save(flujo);

        if (solicitud.pasos() != null) {
            for (SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo pasoDto : solicitud.pasos()) {
                PasoFlujoTrabajo paso = new PasoFlujoTrabajo();
                paso.setFlujoTrabajo(guardado);
                paso.setOrden(pasoDto.orden());
                paso.setNombre(pasoDto.nombre());
                paso.setTipoPaso(pasoDto.tipoPaso());
                if (pasoDto.rolId() != null) {
                    Rol rol = repositorioRol.findByIdAndEmpresaIdAndDeletedFalse(pasoDto.rolId(), empresa.getId())
                            .orElseThrow(() -> new ExcepcionNegocio("Rol no encontrado"));
                    paso.setRol(rol);
                }
                if (pasoDto.usuarioId() != null) {
                    Usuario usuario = repositorioUsuario.findById(pasoDto.usuarioId())
                            .filter(u -> !u.isDeleted() && u.getEmpresa() != null
                                    && u.getEmpresa().getId().equals(empresa.getId()))
                            .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado"));
                    paso.setUsuario(usuario);
                }
                guardado.getPasos().add(paso);
            }
            guardado = repositorioFlujo.save(guardado);
        }

        FlujoTrabajo resultado = repositorioFlujo.buscarConPasos(guardado.getId(), empresa.getId())
                .orElse(guardado);

        servicioAuditoria.registrar("CREAR", "FlujoTrabajo", resultado.getId(), null, resultado.getNombre());
        return mapear(resultado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaFlujoTrabajo> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioFlujo.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .map(f -> repositorioFlujo.buscarConPasos(f.getId(), empresaId).orElse(f))
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaFlujoTrabajo obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        FlujoTrabajo flujo = repositorioFlujo.buscarConPasos(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Flujo de trabajo no encontrado", 404));
        return mapear(flujo);
    }

    public FlujoTrabajo obtenerEntidad(Long id, Long empresaId) {
        return repositorioFlujo.buscarConPasos(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Flujo de trabajo no encontrado", 404));
    }

    @Transactional
    public RespuestaFlujoTrabajo actualizar(Long id, SolicitudActualizarFlujoTrabajo solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        FlujoTrabajo flujo = obtenerEntidad(id, empresa.getId());

        flujo.setNombre(solicitud.nombre());
        flujo.setDescripcion(solicitud.descripcion());

        flujo.getPasos().stream()
                .filter(p -> !p.isDeleted())
                .forEach(p -> {
                    p.setDeleted(true);
                    p.setDeletedAt(LocalDateTime.now());
                    p.setOrden(-Math.abs(p.getOrden()) - p.getId().intValue());
                });
        repositorioFlujo.saveAndFlush(flujo);

        if (solicitud.pasos() != null) {
            for (SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo pasoDto : solicitud.pasos()) {
                PasoFlujoTrabajo paso = new PasoFlujoTrabajo();
                paso.setFlujoTrabajo(flujo);
                paso.setOrden(pasoDto.orden());
                paso.setNombre(pasoDto.nombre());
                paso.setTipoPaso(pasoDto.tipoPaso());
                if (pasoDto.rolId() != null) {
                    Rol rol = repositorioRol.findByIdAndEmpresaIdAndDeletedFalse(pasoDto.rolId(), empresa.getId())
                            .orElseThrow(() -> new ExcepcionNegocio("Rol no encontrado"));
                    paso.setRol(rol);
                }
                if (pasoDto.usuarioId() != null) {
                    Usuario usuario = repositorioUsuario.findById(pasoDto.usuarioId())
                            .filter(u -> !u.isDeleted() && u.getEmpresa() != null
                                    && u.getEmpresa().getId().equals(empresa.getId()))
                            .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado"));
                    paso.setUsuario(usuario);
                }
                flujo.getPasos().add(paso);
            }
        }

        FlujoTrabajo guardado = repositorioFlujo.save(flujo);
        servicioAuditoria.registrar("ACTUALIZAR", "FlujoTrabajo", guardado.getId(), null, guardado.getNombre());
        return mapear(repositorioFlujo.buscarConPasos(guardado.getId(), empresa.getId()).orElse(guardado));
    }

    @Transactional
    public void eliminar(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        FlujoTrabajo flujo = obtenerEntidad(id, empresa.getId());
        if (repositorioTipoSolicitud.existsByFlujoTrabajoIdAndDeletedFalse(id)) {
            throw new ExcepcionNegocio("No se puede eliminar: hay tipos de solicitud que usan este flujo");
        }
        flujo.setDeleted(true);
        flujo.setDeletedAt(LocalDateTime.now());
        flujo.setActivo(false);
        repositorioFlujo.save(flujo);
        servicioAuditoria.registrar("ELIMINAR", "FlujoTrabajo", flujo.getId(), null, flujo.getNombre());
    }

    @Transactional
    public RespuestaFlujoTrabajo alternarActivo(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        FlujoTrabajo flujo = obtenerEntidad(id, empresa.getId());
        flujo.setActivo(!flujo.isActivo());
        FlujoTrabajo guardado = repositorioFlujo.save(flujo);
        servicioAuditoria.registrar("ACTIVAR", "FlujoTrabajo", guardado.getId(), null,
                guardado.isActivo() ? "activado" : "desactivado");
        return mapear(repositorioFlujo.buscarConPasos(guardado.getId(), empresa.getId()).orElse(guardado));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaFlujoTrabajo mapear(FlujoTrabajo flujo) {
        List<RespuestaFlujoTrabajo.RespuestaPasoFlujo> pasos = flujo.getPasos().stream()
                .filter(p -> !p.isDeleted())
                .sorted(Comparator.comparingInt(PasoFlujoTrabajo::getOrden))
                .map(p -> new RespuestaFlujoTrabajo.RespuestaPasoFlujo(
                        p.getId(), p.getOrden(), p.getNombre(), p.getTipoPaso(),
                        p.getRol() != null ? p.getRol().getId() : null,
                        p.getUsuario() != null ? p.getUsuario().getId() : null))
                .toList();

        return new RespuestaFlujoTrabajo(
                flujo.getId(), flujo.getNombre(), flujo.getDescripcion(), flujo.isActivo(), pasos);
    }
}
