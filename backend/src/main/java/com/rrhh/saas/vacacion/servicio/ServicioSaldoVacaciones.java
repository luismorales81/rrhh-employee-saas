package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import com.rrhh.saas.vacacion.dominio.SaldoVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaSaldoVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioPoliticaVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioSaldoVacaciones;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioSaldoVacaciones {

    private final RepositorioSaldoVacaciones repositorio;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioPoliticaVacaciones repositorioPolitica;
    private final ServicioRecalculoSaldoVacaciones servicioRecalculo;
    private final ServicioAuditoria servicioAuditoria;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioSaldoVacaciones(RepositorioSaldoVacaciones repositorio,
                                   RepositorioEmpleado repositorioEmpleado,
                                   RepositorioPoliticaVacaciones repositorioPolitica,
                                   ServicioRecalculoSaldoVacaciones servicioRecalculo,
                                   ServicioAuditoria servicioAuditoria,
                                   ApplicationEventPublisher publicadorEventos) {
        this.repositorio = repositorio;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioPolitica = repositorioPolitica;
        this.servicioRecalculo = servicioRecalculo;
        this.servicioAuditoria = servicioAuditoria;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional
    public SaldoVacaciones inicializarSaldo(Empleado empleado, PoliticaVacaciones politica) {
        int anio = LocalDate.now().getYear();
        SaldoVacaciones saldo = repositorio.findByEmpleadoIdAndAnioAndDeletedFalse(empleado.getId(), anio)
                .orElseGet(SaldoVacaciones::new);

        servicioRecalculo.aplicarRecalculo(saldo, empleado, politica, anio);
        SaldoVacaciones guardado = repositorio.save(saldo);
        publicadorEventos.publishEvent(new EventoSaldoVacacionesActualizado(
                empleado.getId(), empleado.getEmpresa().getId()));
        return guardado;
    }

    @Transactional
    public void recalcularSaldosEmpresa(Long empresaId, PoliticaVacaciones politica) {
        List<Empleado> empleados = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId);
        for (Empleado empleado : empleados) {
            inicializarSaldo(empleado, politica);
        }
    }

    @Transactional
    public void reservarDias(Long empleadoId, int anio, BigDecimal dias) {
        SaldoVacaciones saldo = obtenerSaldoEntidad(empleadoId, anio);
        if (saldo.getDiasDisponibles().compareTo(dias) < 0) {
            throw new ExcepcionNegocio("Saldo insuficiente de vacaciones");
        }
        saldo.setDiasPendientes(saldo.getDiasPendientes().add(dias));
        saldo.recalcularDisponibles();
        repositorio.save(saldo);
        servicioAuditoria.registrar("RESERVAR", "SaldoVacaciones", saldo.getId(), null, dias.toString());
    }

    @Transactional
    public void confirmarDias(Long empleadoId, int anio, BigDecimal dias) {
        SaldoVacaciones saldo = obtenerSaldoEntidad(empleadoId, anio);
        saldo.setDiasPendientes(saldo.getDiasPendientes().subtract(dias));
        saldo.setDiasTomados(saldo.getDiasTomados().add(dias));
        saldo.recalcularDisponibles();
        repositorio.save(saldo);
        publicadorEventos.publishEvent(new EventoSaldoVacacionesActualizado(
                empleadoId, saldo.getEmpleado().getEmpresa().getId()));
        servicioAuditoria.registrar("CONFIRMAR", "SaldoVacaciones", saldo.getId(), null, dias.toString());
    }

    @Transactional
    public void liberarDiasPendientes(Long empleadoId, int anio, BigDecimal dias) {
        SaldoVacaciones saldo = obtenerSaldoEntidad(empleadoId, anio);
        saldo.setDiasPendientes(saldo.getDiasPendientes().subtract(dias));
        saldo.recalcularDisponibles();
        repositorio.save(saldo);
        publicadorEventos.publishEvent(new EventoSaldoVacacionesActualizado(
                empleadoId, saldo.getEmpleado().getEmpresa().getId()));
        servicioAuditoria.registrar("LIBERAR", "SaldoVacaciones", saldo.getId(), null, dias.toString());
    }

    @Transactional
    public void ajustarManual(Long empleadoId, int anio, BigDecimal dias, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ExcepcionNegocio("El motivo del ajuste manual es obligatorio");
        }
        SaldoVacaciones saldo = obtenerSaldoEntidad(empleadoId, anio);
        saldo.setAjusteManual(saldo.getAjusteManual().add(dias));
        saldo.setAjusteManualMotivo(motivo);
        saldo.recalcularDisponibles();
        repositorio.save(saldo);
        servicioAuditoria.registrar("AJUSTE_MANUAL", "SaldoVacaciones", saldo.getId(), null, motivo);
    }

    @Transactional(readOnly = true)
    public RespuestaSaldoVacaciones obtenerSaldo(Long empleadoId, Integer anio) {
        int anioConsulta = anio != null ? anio : LocalDate.now().getYear();
        SaldoVacaciones saldo = repositorio.findByEmpleadoIdAndAnioAndDeletedFalse(empleadoId, anioConsulta)
                .orElseThrow(() -> new ExcepcionNegocio("Saldo de vacaciones no encontrado para el año " + anioConsulta, 404));
        return mapear(saldo);
    }

    @Transactional(readOnly = true)
    public List<RespuestaSaldoVacaciones> obtenerHistorialSaldoPropio() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = repositorioEmpleado.findByUsuarioIdAndDeletedFalse(usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Empleado no encontrado para el usuario", 404));
        return obtenerHistorialSaldo(empleado.getId());
    }

    @Transactional(readOnly = true)
    public List<RespuestaSaldoVacaciones> obtenerHistorialSaldo(Long empleadoId) {
        return repositorio.findByEmpleadoIdAndDeletedFalseOrderByAnioDesc(empleadoId).stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaSaldoVacaciones obtenerSaldoPropio(Integer anio) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = repositorioEmpleado.findByUsuarioIdAndDeletedFalse(usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Empleado no encontrado para el usuario", 404));
        return obtenerSaldo(empleado.getId(), anio);
    }

    @Transactional(readOnly = true)
    public List<RespuestaSaldoVacaciones> listarPorEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpleadoEmpresaIdAndDeletedFalse(empresaId).stream().map(this::mapear).toList();
    }

    public SaldoVacaciones obtenerSaldoEntidad(Long empleadoId, int anio) {
        return repositorio.findByEmpleadoIdAndAnioAndDeletedFalse(empleadoId, anio)
                .orElseThrow(() -> new ExcepcionNegocio("Saldo de vacaciones no encontrado", 404));
    }

    @Transactional
    public SaldoVacaciones obtenerOCrearSaldoAnio(Empleado empleado, int anio) {
        return repositorio.findByEmpleadoIdAndAnioAndDeletedFalse(empleado.getId(), anio)
                .orElseGet(() -> {
                    PoliticaVacaciones politica = repositorioPolitica
                            .findByEmpresaIdAndActivaTrueAndDeletedFalse(empleado.getEmpresa().getId())
                            .orElseThrow(() -> new ExcepcionNegocio("No hay política de vacaciones activa", 404));
                    SaldoVacaciones nuevo = servicioRecalculo.crearSaldoInicial(empleado, politica);
                    nuevo.setAnio(anio);
                    servicioRecalculo.aplicarRecalculo(nuevo, empleado, politica, anio);
                    return repositorio.save(nuevo);
                });
    }

    private RespuestaSaldoVacaciones mapear(SaldoVacaciones saldo) {
        var empleado = saldo.getEmpleado();
        Long politicaId = saldo.getPolitica() != null ? saldo.getPolitica().getId() : null;
        String politicaNombre = saldo.getPolitica() != null ? saldo.getPolitica().getNombre() : null;
        return new RespuestaSaldoVacaciones(
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                saldo.getAnio(),
                saldo.getAntiguedadAnios(),
                saldo.getDiasTotales(),
                saldo.getDiasDisponibles(),
                saldo.getDiasPendientes(),
                saldo.getDiasTomados(),
                politicaId,
                politicaNombre
        );
    }

    public record EventoSaldoVacacionesActualizado(Long empleadoId, Long empresaId) {
    }
}
