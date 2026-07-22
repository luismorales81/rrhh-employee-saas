package com.rrhh.saas.organizacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.organizacion.dominio.Area;
import com.rrhh.saas.organizacion.dominio.Gerencia;
import com.rrhh.saas.organizacion.dominio.Puesto;
import com.rrhh.saas.organizacion.dominio.Sector;
import com.rrhh.saas.organizacion.dto.RespuestaArea;
import com.rrhh.saas.organizacion.dto.RespuestaGerencia;
import com.rrhh.saas.organizacion.dto.RespuestaPuesto;
import com.rrhh.saas.organizacion.dto.RespuestaSector;
import com.rrhh.saas.organizacion.dto.SolicitudActualizarArea;
import com.rrhh.saas.organizacion.dto.SolicitudActualizarGerencia;
import com.rrhh.saas.organizacion.dto.SolicitudActualizarPuesto;
import com.rrhh.saas.organizacion.dto.SolicitudCrearArea;
import com.rrhh.saas.organizacion.dto.SolicitudCrearGerencia;
import com.rrhh.saas.organizacion.dto.SolicitudCrearPuesto;
import com.rrhh.saas.organizacion.repositorio.RepositorioArea;
import com.rrhh.saas.organizacion.repositorio.RepositorioGerencia;
import com.rrhh.saas.organizacion.repositorio.RepositorioPuesto;
import com.rrhh.saas.organizacion.repositorio.RepositorioSector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioOrganizacion {

    private final RepositorioGerencia repositorioGerencia;
    private final RepositorioArea repositorioArea;
    private final RepositorioPuesto repositorioPuesto;
    private final RepositorioSector repositorioSector;
    private final RepositorioEmpleado repositorioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioOrganizacion(RepositorioGerencia repositorioGerencia,
                                RepositorioArea repositorioArea,
                                RepositorioPuesto repositorioPuesto,
                                RepositorioSector repositorioSector,
                                RepositorioEmpleado repositorioEmpleado,
                                ServicioEmpresa servicioEmpresa,
                                ServicioAuditoria servicioAuditoria) {
        this.repositorioGerencia = repositorioGerencia;
        this.repositorioArea = repositorioArea;
        this.repositorioPuesto = repositorioPuesto;
        this.repositorioSector = repositorioSector;
        this.repositorioEmpleado = repositorioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaGerencia crearGerencia(SolicitudCrearGerencia solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Gerencia gerencia = new Gerencia();
        gerencia.setEmpresa(empresa);
        gerencia.setNombre(solicitud.nombre());
        gerencia.setDescripcion(solicitud.descripcion());
        Gerencia guardada = repositorioGerencia.save(gerencia);
        servicioAuditoria.registrar("CREAR", "Gerencia", guardada.getId(), null, guardada.getNombre());
        return mapearGerencia(guardada);
    }

    @Transactional
    public RespuestaGerencia actualizarGerencia(Long id, SolicitudActualizarGerencia solicitud) {
        Gerencia gerencia = obtenerGerencia(id);
        if (solicitud.nombre() != null) {
            gerencia.setNombre(solicitud.nombre());
        }
        if (solicitud.descripcion() != null) {
            gerencia.setDescripcion(solicitud.descripcion());
        }
        Gerencia guardada = repositorioGerencia.save(gerencia);
        servicioAuditoria.registrar("ACTUALIZAR", "Gerencia", guardada.getId(), null, guardada.getNombre());
        return mapearGerencia(guardada);
    }

    @Transactional
    public void eliminarGerencia(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        Gerencia gerencia = obtenerGerencia(id);
        if (repositorioArea.existsByEmpresaIdAndGerenciaIdAndDeletedFalse(empresa.getId(), id)) {
            throw new ExcepcionNegocio("No se puede eliminar una gerencia con áreas asociadas", 400);
        }
        if (repositorioEmpleado.countByEmpresaIdAndGerenciaIdAndDeletedFalse(empresa.getId(), id) > 0) {
            throw new ExcepcionNegocio("No se puede eliminar una gerencia con empleados asignados", 400);
        }
        marcarEliminada(gerencia);
        servicioAuditoria.registrar("ELIMINAR", "Gerencia", gerencia.getId(), null, gerencia.getNombre());
    }

    @Transactional
    public RespuestaArea crearArea(SolicitudCrearArea solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Area area = new Area();
        area.setEmpresa(empresa);
        area.setNombre(solicitud.nombre());
        area.setDescripcion(solicitud.descripcion());
        if (solicitud.gerenciaId() != null) {
            area.setGerencia(obtenerGerencia(solicitud.gerenciaId()));
        }
        Area guardada = repositorioArea.save(area);
        servicioAuditoria.registrar("CREAR", "Area", guardada.getId(), null, guardada.getNombre());
        return mapearArea(guardada);
    }

    @Transactional
    public RespuestaArea actualizarArea(Long id, SolicitudActualizarArea solicitud) {
        Area area = obtenerArea(id);
        if (solicitud.nombre() != null) {
            area.setNombre(solicitud.nombre());
        }
        if (solicitud.descripcion() != null) {
            area.setDescripcion(solicitud.descripcion());
        }
        if (solicitud.gerenciaId() != null) {
            area.setGerencia(obtenerGerencia(solicitud.gerenciaId()));
        }
        Area guardada = repositorioArea.save(area);
        servicioAuditoria.registrar("ACTUALIZAR", "Area", guardada.getId(), null, guardada.getNombre());
        return mapearArea(guardada);
    }

    @Transactional
    public void eliminarArea(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        Area area = obtenerArea(id);
        if (repositorioEmpleado.countByEmpresaIdAndAreaIdAndDeletedFalse(empresa.getId(), id) > 0) {
            throw new ExcepcionNegocio("No se puede eliminar un área con empleados asignados", 400);
        }
        marcarEliminada(area);
        servicioAuditoria.registrar("ELIMINAR", "Area", area.getId(), null, area.getNombre());
    }

    @Transactional
    public RespuestaPuesto crearPuesto(SolicitudCrearPuesto solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa);
        puesto.setNombre(solicitud.nombre());
        puesto.setNivel(solicitud.nivel());
        puesto.setDescripcion(solicitud.descripcion());
        Puesto guardado = repositorioPuesto.save(puesto);
        servicioAuditoria.registrar("CREAR", "Puesto", guardado.getId(), null, guardado.getNombre());
        return mapearPuesto(guardado);
    }

    @Transactional
    public RespuestaPuesto actualizarPuesto(Long id, SolicitudActualizarPuesto solicitud) {
        Puesto puesto = obtenerPuesto(id);
        if (solicitud.nombre() != null) {
            puesto.setNombre(solicitud.nombre());
        }
        if (solicitud.nivel() != null) {
            puesto.setNivel(solicitud.nivel());
        }
        if (solicitud.descripcion() != null) {
            puesto.setDescripcion(solicitud.descripcion());
        }
        Puesto guardado = repositorioPuesto.save(puesto);
        servicioAuditoria.registrar("ACTUALIZAR", "Puesto", guardado.getId(), null, guardado.getNombre());
        return mapearPuesto(guardado);
    }

    @Transactional
    public void eliminarPuesto(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        Puesto puesto = obtenerPuesto(id);
        if (repositorioEmpleado.countByEmpresaIdAndPuestoIdAndDeletedFalse(empresa.getId(), id) > 0) {
            throw new ExcepcionNegocio("No se puede eliminar un puesto con empleados asignados", 400);
        }
        marcarEliminada(puesto);
        servicioAuditoria.registrar("ELIMINAR", "Puesto", puesto.getId(), null, puesto.getNombre());
    }

    @Transactional(readOnly = true)
    public List<RespuestaGerencia> listarGerencias() {
        return repositorioGerencia.findByEmpresaIdAndDeletedFalse(obtenerEmpresaActual().getId())
                .stream().map(this::mapearGerencia).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaArea> listarAreas() {
        return repositorioArea.findByEmpresaIdAndDeletedFalse(obtenerEmpresaActual().getId())
                .stream().map(this::mapearArea).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaPuesto> listarPuestos() {
        return repositorioPuesto.findByEmpresaIdAndDeletedFalse(obtenerEmpresaActual().getId())
                .stream().map(this::mapearPuesto).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaSector> listarSectores() {
        return repositorioSector.findByEmpresaIdAndDeletedFalse(obtenerEmpresaActual().getId())
                .stream().map(s -> new RespuestaSector(
                        s.getId(),
                        s.getNombre(),
                        s.getArea() != null ? s.getArea().getId() : null
                )).toList();
    }

    private RespuestaGerencia mapearGerencia(Gerencia gerencia) {
        return new RespuestaGerencia(gerencia.getId(), gerencia.getNombre(), gerencia.getDescripcion());
    }

    private RespuestaArea mapearArea(Area area) {
        Long gerenciaId = area.getGerencia() != null ? area.getGerencia().getId() : null;
        String gerenciaNombre = area.getGerencia() != null ? area.getGerencia().getNombre() : null;
        return new RespuestaArea(area.getId(), area.getNombre(), area.getDescripcion(), gerenciaId, gerenciaNombre);
    }

    private RespuestaPuesto mapearPuesto(Puesto puesto) {
        return new RespuestaPuesto(puesto.getId(), puesto.getNombre(), puesto.getNivel(), puesto.getDescripcion());
    }

    private Gerencia obtenerGerencia(Long id) {
        Long empresaId = obtenerEmpresaActual().getId();
        return repositorioGerencia.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Gerencia no encontrada", 404));
    }

    private Area obtenerArea(Long id) {
        Long empresaId = obtenerEmpresaActual().getId();
        return repositorioArea.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Área no encontrada", 404));
    }

    private Puesto obtenerPuesto(Long id) {
        Long empresaId = obtenerEmpresaActual().getId();
        return repositorioPuesto.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Puesto no encontrado", 404));
    }

    private void marcarEliminada(com.rrhh.saas.comun.entidad.EntidadAuditable entidad) {
        entidad.setDeleted(true);
        entidad.setDeletedAt(LocalDateTime.now());
        if (entidad instanceof Gerencia gerencia) {
            repositorioGerencia.save(gerencia);
        } else if (entidad instanceof Area area) {
            repositorioArea.save(area);
        } else if (entidad instanceof Puesto puesto) {
            repositorioPuesto.save(puesto);
        }
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }
}
