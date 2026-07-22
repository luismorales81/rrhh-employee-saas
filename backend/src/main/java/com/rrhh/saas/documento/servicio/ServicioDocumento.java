package com.rrhh.saas.documento.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.documento.dominio.CategoriaDocumento;
import com.rrhh.saas.documento.dominio.Documento;
import com.rrhh.saas.documento.dto.RespuestaDocumento;
import com.rrhh.saas.documento.dto.SolicitudCrearDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioDocumento;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioDocumento {

    private final RepositorioDocumento repositorio;
    private final ServicioCategoriaDocumento servicioCategoriaDocumento;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioDocumento(RepositorioDocumento repositorio,
                             ServicioCategoriaDocumento servicioCategoriaDocumento,
                             ServicioEmpresa servicioEmpresa,
                             ServicioAuditoria servicioAuditoria) {
        this.repositorio = repositorio;
        this.servicioCategoriaDocumento = servicioCategoriaDocumento;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public RespuestaDocumento crear(SolicitudCrearDocumento solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        CategoriaDocumento categoria = servicioCategoriaDocumento.obtenerEntidad(solicitud.categoriaId(), empresa.getId());

        Documento documento = new Documento();
        documento.setEmpresa(empresa);
        documento.setCategoria(categoria);
        documento.setTitulo(solicitud.titulo());
        documento.setDescripcion(solicitud.descripcion());
        documento.setVersion(solicitud.version() != null ? solicitud.version() : "1.0");
        documento.setUrlArchivo(solicitud.urlArchivo());
        documento.setHashArchivo(solicitud.hashArchivo());
        documento.setRequiereFirma(solicitud.requiereFirma());
        documento.setActivo(true);

        Documento guardado = repositorio.save(documento);
        servicioAuditoria.registrar("CREAR", "Documento", guardado.getId(), null, guardado.getTitulo());
        return mapear(guardado);
    }

    @Transactional(readOnly = true)
    public List<RespuestaDocumento> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalseOrderByTituloAsc(empresaId).stream()
                .map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaDocumento obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return mapear(obtenerEntidad(id, empresaId));
    }

    public Documento obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Documento no encontrado", 404));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaDocumento mapear(Documento documento) {
        return new RespuestaDocumento(
                documento.getId(),
                documento.getCategoria().getId(),
                documento.getCategoria().getNombre(),
                documento.getTitulo(),
                documento.getDescripcion(),
                documento.getVersion(),
                documento.getUrlArchivo(),
                documento.getHashArchivo(),
                documento.isRequiereFirma(),
                documento.isActivo()
        );
    }
}
