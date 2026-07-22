package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.RegistroAuditoria;
import com.rrhh.saas.nucleo.repositorio.RepositorioRegistroAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioAuditoria {

    private final RepositorioRegistroAuditoria repositorio;

    public ServicioAuditoria(RepositorioRegistroAuditoria repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void registrar(String accion, String nombreEntidad, Long entidadId,
                          String valoresAnteriores, String valoresNuevos) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setEmpresaId(ContextoTenant.obtenerEmpresaId());
        registro.setUsuarioId(ContextoTenant.obtenerUsuarioId());
        registro.setAccion(accion);
        registro.setNombreEntidad(nombreEntidad);
        registro.setEntidadId(entidadId);
        registro.setValoresAnteriores(valoresAnteriores);
        registro.setValoresNuevos(valoresNuevos);
        repositorio.save(registro);
    }
}
