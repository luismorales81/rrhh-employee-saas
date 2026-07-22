package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import com.rrhh.saas.vacacion.dominio.RangoPoliticaVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaPoliticaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPoliticaVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioPoliticaVacaciones;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ServicioPoliticaVacaciones {

    private final RepositorioPoliticaVacaciones repositorio;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioSaldoVacaciones servicioSaldoVacaciones;

    public ServicioPoliticaVacaciones(RepositorioPoliticaVacaciones repositorio,
                                      ServicioEmpresa servicioEmpresa,
                                      ServicioAuditoria servicioAuditoria,
                                      ServicioSaldoVacaciones servicioSaldoVacaciones) {
        this.repositorio = repositorio;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioSaldoVacaciones = servicioSaldoVacaciones;
    }

    @Transactional
    public RespuestaPoliticaVacaciones crear(SolicitudCrearPoliticaVacaciones solicitud) {
        Empresa empresa = obtenerEmpresaActual();

        PoliticaVacaciones politica = new PoliticaVacaciones();
        politica.setEmpresa(empresa);
        politica.setNombre(solicitud.nombre());
        politica.setDescripcion(solicitud.descripcion());
        politica.setActiva(false);

        if (solicitud.rangos() != null) {
            for (SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica rangoDto : solicitud.rangos()) {
                RangoPoliticaVacaciones rango = new RangoPoliticaVacaciones();
                rango.setPolitica(politica);
                rango.setAniosMinimos(rangoDto.aniosMinimos());
                rango.setAniosMaximos(rangoDto.aniosMaximos());
                rango.setDiasOtorgados(rangoDto.diasOtorgados());
                politica.getRangos().add(rango);
            }
        }

        PoliticaVacaciones guardada = repositorio.save(politica);
        servicioAuditoria.registrar("CREAR", "PoliticaVacaciones", guardada.getId(), null, guardada.getNombre());
        return mapear(guardada);
    }

    @Transactional
    public RespuestaPoliticaVacaciones activar(Long politicaId) {
        Empresa empresa = obtenerEmpresaActual();
        PoliticaVacaciones politica = repositorio.findByIdAndEmpresaIdAndDeletedFalse(politicaId, empresa.getId())
                .orElseThrow(() -> new ExcepcionNegocio("Política no encontrada", 404));

        if (politica.getRangos().stream().noneMatch(r -> !r.isDeleted())) {
            throw new ExcepcionNegocio("La política debe tener al menos un rango configurado");
        }

        repositorio.desactivarTodas(empresa.getId());
        politica.setActiva(true);
        PoliticaVacaciones activada = repositorio.save(politica);

        servicioSaldoVacaciones.recalcularSaldosEmpresa(empresa.getId(), activada);
        servicioAuditoria.registrar("ACTIVAR", "PoliticaVacaciones", activada.getId(), null, activada.getNombre());
        return mapear(activada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaPoliticaVacaciones> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public PoliticaVacaciones obtenerActiva(Long empresaId) {
        return repositorio.findByEmpresaIdAndActivaTrueAndDeletedFalse(empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("No hay política de vacaciones activa", 404));
    }

    public PoliticaVacaciones obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Política no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaPoliticaVacaciones mapear(PoliticaVacaciones politica) {
        List<RespuestaPoliticaVacaciones.RespuestaRangoPolitica> rangos = politica.getRangos().stream()
                .filter(r -> !r.isDeleted())
                .sorted(Comparator.comparingInt(RangoPoliticaVacaciones::getAniosMinimos))
                .map(r -> new RespuestaPoliticaVacaciones.RespuestaRangoPolitica(
                        r.getId(), r.getAniosMinimos(), r.getAniosMaximos(), r.getDiasOtorgados()))
                .toList();

        return new RespuestaPoliticaVacaciones(
                politica.getId(), politica.getNombre(), politica.getDescripcion(), politica.isActiva(), rangos);
    }
}
