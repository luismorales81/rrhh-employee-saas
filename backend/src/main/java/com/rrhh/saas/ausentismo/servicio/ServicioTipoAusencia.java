package com.rrhh.saas.ausentismo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.ausentismo.dominio.TipoAusencia;
import com.rrhh.saas.ausentismo.dto.RespuestaTipoAusencia;
import com.rrhh.saas.ausentismo.dto.SolicitudCrearTipoAusencia;
import com.rrhh.saas.ausentismo.repositorio.RepositorioTipoAusencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioTipoAusencia {

    private final RepositorioTipoAusencia repositorio;
    private final ServicioEmpresa servicioEmpresa;

    public ServicioTipoAusencia(RepositorioTipoAusencia repositorio, ServicioEmpresa servicioEmpresa) {
        this.repositorio = repositorio;
        this.servicioEmpresa = servicioEmpresa;
    }

    @Transactional
    public void crearTiposPorDefecto(Empresa empresa) {
        crearSiNoExiste(empresa, "Enfermedad", "Ausencia por enfermedad", true);
        crearSiNoExiste(empresa, "Llegada tarde", "Llegada fuera de horario", false);
        crearSiNoExiste(empresa, "Falta injustificada", "Inasistencia sin justificación", false);
        crearSiNoExiste(empresa, "Falta justificada", "Inasistencia con justificación", true);
        crearSiNoExiste(empresa, "Permiso personal", "Permiso personal", false);
    }

    @Transactional
    public RespuestaTipoAusencia crear(SolicitudCrearTipoAusencia solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        repositorio.findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), solicitud.nombre())
                .ifPresent(t -> { throw new ExcepcionNegocio("Ya existe un tipo de ausencia con ese nombre"); });

        TipoAusencia tipo = new TipoAusencia();
        tipo.setEmpresa(empresa);
        tipo.setNombre(solicitud.nombre());
        tipo.setDescripcion(solicitud.descripcion());
        tipo.setRequiereCertificado(solicitud.requiereCertificado());
        return mapear(repositorio.save(tipo));
    }

    @Transactional(readOnly = true)
    public List<RespuestaTipoAusencia> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    public TipoAusencia obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Tipo de ausencia no encontrado", 404));
    }

    private void crearSiNoExiste(Empresa empresa, String nombre, String descripcion, boolean requiereCertificado) {
        if (repositorio.findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), nombre).isPresent()) {
            return;
        }
        TipoAusencia tipo = new TipoAusencia();
        tipo.setEmpresa(empresa);
        tipo.setNombre(nombre);
        tipo.setDescripcion(descripcion);
        tipo.setRequiereCertificado(requiereCertificado);
        repositorio.save(tipo);
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaTipoAusencia mapear(TipoAusencia tipo) {
        return new RespuestaTipoAusencia(
                tipo.getId(), tipo.getNombre(), tipo.getDescripcion(), tipo.isRequiereCertificado());
    }
}
