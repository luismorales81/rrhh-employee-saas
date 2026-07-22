package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.seguridad.UtilidadContextoSeguridad;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dto.RespuestaEmpresa;
import com.rrhh.saas.nucleo.dto.SolicitudActualizarEmpresa;
import com.rrhh.saas.nucleo.dto.SolicitudCrearEmpresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioEmpresa {

    private final RepositorioEmpresa repositorioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioInicializacionEmpresa servicioInicializacionEmpresa;

    public ServicioEmpresa(RepositorioEmpresa repositorioEmpresa, ServicioAuditoria servicioAuditoria,
                           ServicioInicializacionEmpresa servicioInicializacionEmpresa) {
        this.repositorioEmpresa = repositorioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioInicializacionEmpresa = servicioInicializacionEmpresa;
    }

    @Transactional
    public RespuestaEmpresa crear(SolicitudCrearEmpresa solicitud) {
        Empresa empresa = new Empresa();
        empresa.setNombre(solicitud.nombre());
        empresa.setRazonSocial(solicitud.razonSocial());
        empresa.setCuit(solicitud.cuit());
        empresa.setEmail(solicitud.email());
        empresa.setTelefono(solicitud.telefono());
        empresa.setEstado("ACTIVA");
        Empresa guardada = repositorioEmpresa.save(empresa);
        servicioInicializacionEmpresa.crearRolesPorDefecto(guardada);

        servicioAuditoria.registrar("CREAR", "Empresa", guardada.getId(), null,
                "nombre=" + guardada.getNombre());

        return mapear(guardada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaEmpresa> listar() {
        if (UtilidadContextoSeguridad.esSuperAdminPlataforma()) {
            return repositorioEmpresa.findByDeletedFalse().stream().map(this::mapear).toList();
        }
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return List.of(obtenerPorId(empresaId));
    }

    @Transactional
    public RespuestaEmpresa actualizar(Long id, SolicitudActualizarEmpresa solicitud) {
        Empresa empresa = obtenerEntidadParaSuperAdmin(id);
        empresa.setNombre(solicitud.nombre());
        empresa.setRazonSocial(solicitud.razonSocial());
        empresa.setCuit(solicitud.cuit());
        empresa.setEmail(solicitud.email());
        empresa.setTelefono(solicitud.telefono());
        Empresa guardada = repositorioEmpresa.save(empresa);

        servicioAuditoria.registrar("ACTUALIZAR", "Empresa", guardada.getId(), null,
                "nombre=" + guardada.getNombre());

        return mapear(guardada);
    }

    @Transactional
    public void desactivar(Long id) {
        Empresa empresa = obtenerEntidadParaSuperAdmin(id);
        empresa.setEstado("INACTIVA");
        empresa.setDeleted(true);
        empresa.setDeletedAt(LocalDateTime.now());
        repositorioEmpresa.save(empresa);

        servicioAuditoria.registrar("DESACTIVAR", "Empresa", empresa.getId(), null,
                "nombre=" + empresa.getNombre());
    }

    @Transactional(readOnly = true)
    public RespuestaEmpresa obtenerPorId(Long id) {
        if (!UtilidadContextoSeguridad.esSuperAdminPlataforma()) {
            Long empresaContexto = ContextoTenant.obtenerEmpresaId();
            if (empresaContexto == null || !empresaContexto.equals(id)) {
                throw new ExcepcionNegocio("Empresa no encontrada", 404);
            }
        }
        Empresa empresa = repositorioEmpresa.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ExcepcionNegocio("Empresa no encontrada", 404));
        return mapear(empresa);
    }

    public Empresa obtenerEntidad(Long id) {
        return repositorioEmpresa.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ExcepcionNegocio("Empresa no encontrada", 404));
    }

    private Empresa obtenerEntidadParaSuperAdmin(Long id) {
        if (!UtilidadContextoSeguridad.esSuperAdminPlataforma()) {
            throw new ExcepcionNegocio("Operación solo disponible para administrador de plataforma", 403);
        }
        return obtenerEntidad(id);
    }

    private RespuestaEmpresa mapear(Empresa empresa) {
        return new RespuestaEmpresa(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getRazonSocial(),
                empresa.getCuit(),
                empresa.getEmail(),
                empresa.getTelefono(),
                empresa.getEstado()
        );
    }
}
