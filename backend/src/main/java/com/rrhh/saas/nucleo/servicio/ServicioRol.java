package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Permiso;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dto.RespuestaRol;
import com.rrhh.saas.nucleo.dto.SolicitudActualizarRol;
import com.rrhh.saas.nucleo.dto.SolicitudCrearRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioRol {

    private final RepositorioRol repositorioRol;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioPermiso servicioPermiso;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioRol(RepositorioRol repositorioRol, RepositorioUsuario repositorioUsuario,
                       ServicioEmpresa servicioEmpresa,
                       ServicioPermiso servicioPermiso, ServicioAuditoria servicioAuditoria) {
        this.repositorioRol = repositorioRol;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioPermiso = servicioPermiso;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaRol crear(SolicitudCrearRol solicitud) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }

        Empresa empresa = servicioEmpresa.obtenerEntidad(empresaId);
        repositorioRol.findByEmpresaIdAndNombreAndDeletedFalse(empresaId, solicitud.nombre())
                .ifPresent(r -> { throw new ExcepcionNegocio("Ya existe un rol con ese nombre"); });

        Rol rol = new Rol();
        rol.setEmpresa(empresa);
        rol.setNombre(solicitud.nombre());
        rol.setDescripcion(solicitud.descripcion());
        rol.setPermisos(servicioPermiso.obtenerPorIds(solicitud.permisosIds()));

        Rol guardado = repositorioRol.save(rol);
        servicioAuditoria.registrar("CREAR", "Rol", guardado.getId(), null, guardado.getNombre());
        return mapear(guardado);
    }

    @Transactional(readOnly = true)
    public RespuestaRol obtenerPorId(Long id) {
        return mapear(obtenerEntidad(id));
    }

    @Transactional
    public RespuestaRol actualizar(Long id, SolicitudActualizarRol solicitud) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Rol rol = obtenerEntidad(id);

        repositorioRol.findByEmpresaIdAndNombreAndDeletedFalse(empresaId, solicitud.nombre())
                .filter(r -> !r.getId().equals(id))
                .ifPresent(r -> { throw new ExcepcionNegocio("Ya existe un rol con ese nombre"); });

        rol.setNombre(solicitud.nombre());
        rol.setDescripcion(solicitud.descripcion());
        rol.setPermisos(servicioPermiso.obtenerPorIds(solicitud.permisosIds()));

        Rol guardado = repositorioRol.save(rol);
        servicioAuditoria.registrar("ACTUALIZAR", "Rol", guardado.getId(), null, guardado.getNombre());
        return mapear(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Rol rol = obtenerEntidad(id);

        if (!repositorioUsuario.buscarPorRol(empresaId, id).isEmpty()) {
            throw new ExcepcionNegocio("No se puede eliminar un rol con usuarios asignados");
        }

        rol.setDeleted(true);
        rol.setDeletedAt(LocalDateTime.now());
        repositorioRol.save(rol);

        servicioAuditoria.registrar("ELIMINAR", "Rol", rol.getId(), null, rol.getNombre());
    }

    @Transactional(readOnly = true)
    public List<RespuestaRol> listarPorEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioRol.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    public Rol obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioRol.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Rol no encontrado", 404));
    }

    private RespuestaRol mapear(Rol rol) {
        List<String> permisos = rol.getPermisos().stream().map(Permiso::getCodigo).toList();
        return new RespuestaRol(rol.getId(), rol.getNombre(), rol.getDescripcion(), permisos);
    }
}
