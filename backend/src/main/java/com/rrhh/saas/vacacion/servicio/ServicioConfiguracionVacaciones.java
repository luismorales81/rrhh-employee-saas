package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comun.utilidad.UtilidadClienteHttp;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.vacacion.dominio.ConfiguracionVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaConfiguracionVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudActualizarConfiguracionVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioConfiguracionVacaciones;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioConfiguracionVacaciones {

    private final RepositorioConfiguracionVacaciones repositorio;
    private final ServicioEmpresa servicioEmpresa;

    public ServicioConfiguracionVacaciones(RepositorioConfiguracionVacaciones repositorio,
                                           ServicioEmpresa servicioEmpresa) {
        this.repositorio = repositorio;
        this.servicioEmpresa = servicioEmpresa;
    }

    @Transactional(readOnly = true)
    public RespuestaConfiguracionVacaciones obtener() {
        return mapear(obtenerOCrearEntidad(obtenerEmpresaActual().getId()));
    }

    @Transactional
    public RespuestaConfiguracionVacaciones actualizar(SolicitudActualizarConfiguracionVacaciones solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        ConfiguracionVacaciones config = obtenerOCrearEntidad(empresa.getId());

        if (solicitud.diasMinimosFraccion() < ConfiguracionVacaciones.PISO_LEGAL_DIAS_FRACCION) {
            throw new ExcepcionNegocio("El mínimo de días por fracción no puede ser menor a "
                    + ConfiguracionVacaciones.PISO_LEGAL_DIAS_FRACCION + " (piso legal Ley 27.802)");
        }

        config.setDiasMinimosFraccion(solicitud.diasMinimosFraccion());
        config.setAnticipacionMinimaDias(solicitud.anticipacionMinimaDias());
        config.setPermiteSolicitudSinSaldo(solicitud.permiteSolicitudSinSaldo());
        return mapear(repositorio.save(config));
    }

    @Transactional(readOnly = true)
    public ConfiguracionVacaciones obtenerEntidad(Long empresaId) {
        return obtenerOCrearEntidad(empresaId);
    }

    private ConfiguracionVacaciones obtenerOCrearEntidad(Long empresaId) {
        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)
                .orElseGet(() -> crearPorDefecto(empresaId));
    }

    private ConfiguracionVacaciones crearPorDefecto(Long empresaId) {
        ConfiguracionVacaciones config = new ConfiguracionVacaciones();
        config.setEmpresa(servicioEmpresa.obtenerEntidad(empresaId));
        config.setDiasMinimosFraccion(7);
        config.setAnticipacionMinimaDias(45);
        config.setPermiteSolicitudSinSaldo(false);
        return repositorio.save(config);
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaConfiguracionVacaciones mapear(ConfiguracionVacaciones config) {
        return new RespuestaConfiguracionVacaciones(
                config.getDiasMinimosFraccion(),
                config.getAnticipacionMinimaDias(),
                config.isPermiteSolicitudSinSaldo(),
                config.diasMinimosFraccionEfectivos()
        );
    }
}
