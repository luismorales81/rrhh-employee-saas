package com.rrhh.saas.documento.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.documento.dominio.CategoriaDocumento;
import com.rrhh.saas.documento.dto.RespuestaCategoriaDocumento;
import com.rrhh.saas.documento.dto.SolicitudCrearCategoriaDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioCategoriaDocumento;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioCategoriaDocumento {

    private final RepositorioCategoriaDocumento repositorio;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioCategoriaDocumento(RepositorioCategoriaDocumento repositorio,
                                      ServicioEmpresa servicioEmpresa,
                                      ServicioAuditoria servicioAuditoria) {
        this.repositorio = repositorio;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaCategoriaDocumento crear(SolicitudCrearCategoriaDocumento solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        if (repositorio.findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), solicitud.nombre()).isPresent()) {
            throw new ExcepcionNegocio("Ya existe una categoría con ese nombre");
        }
        CategoriaDocumento categoria = new CategoriaDocumento();
        categoria.setEmpresa(empresa);
        categoria.setNombre(solicitud.nombre());
        categoria.setDescripcion(solicitud.descripcion());
        CategoriaDocumento guardada = repositorio.save(categoria);
        servicioAuditoria.registrar("CREAR", "CategoriaDocumento", guardada.getId(), null, guardada.getNombre());
        return mapear(guardada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaCategoriaDocumento> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalseOrderByNombreAsc(empresaId).stream()
                .map(this::mapear).toList();
    }

    public CategoriaDocumento obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Categoría de documento no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaCategoriaDocumento mapear(CategoriaDocumento categoria) {
        return new RespuestaCategoriaDocumento(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
