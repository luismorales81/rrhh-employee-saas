package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.TipoSolicitud;
import com.rrhh.saas.solicitud.dto.RespuestaTipoSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudActualizarTipoSolicitud;
import com.rrhh.saas.solicitud.dto.SolicitudCrearTipoSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioTipoSolicitud;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioTipoSolicitud {

    private final RepositorioTipoSolicitud repositorio;
    private final ServicioFlujoTrabajo servicioFlujoTrabajo;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioTipoSolicitud(RepositorioTipoSolicitud repositorio,
                                 ServicioFlujoTrabajo servicioFlujoTrabajo,
                                 ServicioEmpresa servicioEmpresa,
                                 ServicioAuditoria servicioAuditoria) {
        this.repositorio = repositorio;
        this.servicioFlujoTrabajo = servicioFlujoTrabajo;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaTipoSolicitud crear(SolicitudCrearTipoSolicitud solicitud) {
        Empresa empresa = obtenerEmpresaActual();

        TipoSolicitud tipo = new TipoSolicitud();
        tipo.setEmpresa(empresa);
        tipo.setNombre(solicitud.nombre());
        tipo.setDescripcion(solicitud.descripcion());
        tipo.setRequiereAprobacion(solicitud.requiereAprobacion());
        tipo.setRequiereAdjunto(solicitud.requiereAdjunto());

        if (solicitud.flujoTrabajoId() != null) {
            FlujoTrabajo flujo = servicioFlujoTrabajo.obtenerEntidad(solicitud.flujoTrabajoId(), empresa.getId());
            tipo.setFlujoTrabajo(flujo);
        }

        TipoSolicitud guardado = repositorio.save(tipo);
        servicioAuditoria.registrar("CREAR", "TipoSolicitud", guardado.getId(), null, guardado.getNombre());
        return mapear(guardado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaTipoSolicitud> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    public TipoSolicitud obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Tipo de solicitud no encontrado", 404));
    }

    @Transactional(readOnly = true)
    public RespuestaTipoSolicitud obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return mapear(obtenerEntidad(id, empresaId));
    }

    @Transactional
    public RespuestaTipoSolicitud actualizar(Long id, SolicitudActualizarTipoSolicitud solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        TipoSolicitud tipo = obtenerEntidad(id, empresa.getId());

        tipo.setNombre(solicitud.nombre());
        if (solicitud.descripcion() != null) {
            tipo.setDescripcion(solicitud.descripcion());
        }
        if (solicitud.requiereAprobacion() != null) {
            tipo.setRequiereAprobacion(solicitud.requiereAprobacion());
        }
        if (solicitud.requiereAdjunto() != null) {
            tipo.setRequiereAdjunto(solicitud.requiereAdjunto());
        }
        if (solicitud.flujoTrabajoId() != null) {
            FlujoTrabajo flujo = servicioFlujoTrabajo.obtenerEntidad(solicitud.flujoTrabajoId(), empresa.getId());
            tipo.setFlujoTrabajo(flujo);
        } else {
            tipo.setFlujoTrabajo(null);
        }

        TipoSolicitud guardado = repositorio.save(tipo);
        servicioAuditoria.registrar("ACTUALIZAR", "TipoSolicitud", guardado.getId(), null, guardado.getNombre());
        return mapear(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Empresa empresa = obtenerEmpresaActual();
        TipoSolicitud tipo = obtenerEntidad(id, empresa.getId());
        tipo.setDeleted(true);
        tipo.setDeletedAt(LocalDateTime.now());
        repositorio.save(tipo);
        servicioAuditoria.registrar("ELIMINAR", "TipoSolicitud", tipo.getId(), null, tipo.getNombre());
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaTipoSolicitud mapear(TipoSolicitud tipo) {
        return new RespuestaTipoSolicitud(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getDescripcion(),
                tipo.getFlujoTrabajo() != null ? tipo.getFlujoTrabajo().getId() : null,
                tipo.isRequiereAprobacion(),
                tipo.isRequiereAdjunto()
        );
    }
}
