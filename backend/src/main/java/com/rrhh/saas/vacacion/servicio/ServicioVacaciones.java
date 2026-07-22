package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.excepcion.ExcepcionValidacionVacaciones;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import com.rrhh.saas.solicitud.dominio.AccionPaso;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.solicitud.dominio.FlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.Solicitud;
import com.rrhh.saas.solicitud.dominio.TipoSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioTipoSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioMotorWorkflow;
import com.rrhh.saas.solicitud.servicio.ServicioSolicitud;
import com.rrhh.saas.vacacion.dominio.ConfiguracionVacaciones;
import com.rrhh.saas.vacacion.dominio.DiaSemana;
import com.rrhh.saas.vacacion.dominio.SaldoVacaciones;
import com.rrhh.saas.vacacion.dominio.SolicitudVacaciones;
import com.rrhh.saas.vacacion.dominio.TipoEventoSolicitudVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaDetalleSolicitudVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaEventoCalendarioVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaSimularFechaVacaciones;
import com.rrhh.saas.vacacion.dto.RespuestaSolicitudVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudAprobarVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudContrapropuestaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudRechazarVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudSimularFechaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudVacacionesDto;
import com.rrhh.saas.vacacion.repositorio.RepositorioSolicitudVacaciones;
import com.rrhh.saas.vacacion.validacion.ValidacionFechaInicio;
import com.rrhh.saas.vacacion.validacion.ValidacionSolicitudVacaciones;
import com.rrhh.saas.vacacion.validacion.ValidadorFechaInicioVacaciones;
import com.rrhh.saas.vacacion.validacion.ValidadorSolicitudVacaciones;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioVacaciones {

    private static final String NOMBRE_TIPO_VACACIONES = "Vacaciones";

    private final RepositorioSolicitud repositorioSolicitud;
    private final RepositorioSolicitudVacaciones repositorioSolicitudVacaciones;
    private final RepositorioTipoSolicitud repositorioTipoSolicitud;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioUsuario servicioUsuario;
    private final ServicioSaldoVacaciones servicioSaldoVacaciones;
    private final ServicioMotorWorkflow servicioMotorWorkflow;
    private final ServicioSolicitud servicioSolicitud;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioPatronDescanso servicioPatronDescanso;
    private final ServicioConfiguracionVacaciones servicioConfiguracionVacaciones;
    private final ServicioEventoSolicitudVacaciones servicioEventoSolicitudVacaciones;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioVacaciones(RepositorioSolicitud repositorioSolicitud,
                              RepositorioSolicitudVacaciones repositorioSolicitudVacaciones,
                              RepositorioTipoSolicitud repositorioTipoSolicitud,
                              ServicioEmpleado servicioEmpleado,
                              ServicioEmpresa servicioEmpresa,
                              ServicioUsuario servicioUsuario,
                              ServicioSaldoVacaciones servicioSaldoVacaciones,
                              ServicioMotorWorkflow servicioMotorWorkflow,
                              ServicioSolicitud servicioSolicitud,
                              ServicioAuditoria servicioAuditoria,
                              ServicioPatronDescanso servicioPatronDescanso,
                              ServicioConfiguracionVacaciones servicioConfiguracionVacaciones,
                              ServicioEventoSolicitudVacaciones servicioEventoSolicitudVacaciones,
                              ApplicationEventPublisher publicadorEventos) {
        this.repositorioSolicitud = repositorioSolicitud;
        this.repositorioSolicitudVacaciones = repositorioSolicitudVacaciones;
        this.repositorioTipoSolicitud = repositorioTipoSolicitud;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioUsuario = servicioUsuario;
        this.servicioSaldoVacaciones = servicioSaldoVacaciones;
        this.servicioMotorWorkflow = servicioMotorWorkflow;
        this.servicioSolicitud = servicioSolicitud;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioPatronDescanso = servicioPatronDescanso;
        this.servicioConfiguracionVacaciones = servicioConfiguracionVacaciones;
        this.servicioEventoSolicitudVacaciones = servicioEventoSolicitudVacaciones;
        this.publicadorEventos = publicadorEventos;
    }

    @Transactional(readOnly = true)
    public RespuestaSimularFechaVacaciones simularFecha(SolicitudSimularFechaVacaciones solicitud) {
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        List<DiaSemana> patron = servicioPatronDescanso.obtenerDiasDescansoVigentes(
                empleado.getId(), solicitud.fechaInicioDeseada());
        LocalDate fin = solicitud.fechaInicioDeseada().plusDays(solicitud.diasDeseados() - 1L);
        ValidacionFechaInicio validacion = ValidadorFechaInicioVacaciones.validar(
                solicitud.fechaInicioDeseada(), patron);
        return new RespuestaSimularFechaVacaciones(
                validacion.esValida(),
                solicitud.fechaInicioDeseada(),
                fin,
                validacion.fechaSugerida(),
                validacion.motivo()
        );
    }

    @Transactional
    public RespuestaSolicitudVacaciones solicitar(SolicitudVacacionesDto solicitudDto) {
        return solicitar(solicitudDto, null);
    }

    @Transactional
    public RespuestaSolicitudVacaciones solicitar(SolicitudVacacionesDto solicitudDto,
                                                  HttpServletRequest peticion) {
        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        int anio = solicitudDto.fechaInicio().getYear();

        ResultadoValidacion validacion = ejecutarValidaciones(
                empleado, solicitudDto.fechaInicio(), solicitudDto.fechaFin(), anio, null);
        if (!validacion.esValida()) {
            throw new ExcepcionValidacionVacaciones(validacion.errores(), validacion.advertencias());
        }

        int dias = CalculadorVacaciones.calcularDiasEntreFechas(
                solicitudDto.fechaInicio(), solicitudDto.fechaFin());
        BigDecimal diasSolicitados = BigDecimal.valueOf(dias);
        SaldoVacaciones saldo = servicioSaldoVacaciones.obtenerOCrearSaldoAnio(empleado, anio);
        servicioSaldoVacaciones.reservarDias(empleado.getId(), anio, diasSolicitados);

        TipoSolicitud tipo = repositorioTipoSolicitud
                .findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), NOMBRE_TIPO_VACACIONES)
                .orElseThrow(() -> new ExcepcionNegocio(
                        "Configure un tipo de solicitud llamado 'Vacaciones' antes de solicitar"));

        Solicitud solicitud = new Solicitud();
        solicitud.setEmpresa(empresa);
        solicitud.setEmpleado(empleado);
        solicitud.setTipoSolicitud(tipo);
        solicitud.setMotivo(solicitudDto.motivo());
        solicitud.setEstado(EstadoSolicitud.BORRADOR);

        Solicitud guardada = repositorioSolicitud.save(solicitud);

        SolicitudVacaciones solicitudVacaciones = new SolicitudVacaciones();
        solicitudVacaciones.setSolicitud(guardada);
        solicitudVacaciones.setSaldoVacaciones(saldo);
        solicitudVacaciones.setFechaInicio(solicitudDto.fechaInicio());
        solicitudVacaciones.setFechaFin(solicitudDto.fechaFin());
        solicitudVacaciones.setDiasSolicitados(diasSolicitados);
        SolicitudVacaciones vacacionesGuardada = repositorioSolicitudVacaciones.save(solicitudVacaciones);

        EstadoSolicitud estadoAnterior = EstadoSolicitud.BORRADOR;
        if (tipo.isRequiereAprobacion() && tipo.getFlujoTrabajo() != null) {
            FlujoTrabajo flujo = tipo.getFlujoTrabajo();
            servicioMotorWorkflow.iniciar(guardada, flujo);
            guardada.setEstado(EstadoSolicitud.EN_REVISION);
            repositorioSolicitud.save(guardada);
        } else {
            guardada.setEstado(EstadoSolicitud.APROBADO);
            repositorioSolicitud.save(guardada);
            solicitudVacaciones.setFechaInicioAprobada(solicitudDto.fechaInicio());
            solicitudVacaciones.setFechaFinAprobada(solicitudDto.fechaFin());
            repositorioSolicitudVacaciones.save(solicitudVacaciones);
            servicioSaldoVacaciones.confirmarDias(empleado.getId(), anio, diasSolicitados);
        }

        servicioEventoSolicitudVacaciones.registrar(
                vacacionesGuardada, TipoEventoSolicitudVacaciones.SOLICITUD_CREADA,
                estadoAnterior, guardada.getEstado(), ContextoTenant.obtenerUsuarioId(),
                solicitudDto.motivo(), peticion);

        servicioAuditoria.registrar("SOLICITAR", "SolicitudVacaciones", vacacionesGuardada.getId(),
                null, diasSolicitados.toString());
        publicadorEventos.publishEvent(new EventoVacacionesSolicitadas(
                guardada.getId(), empleado.getId(), empresa.getId(), diasSolicitados));

        return mapear(vacacionesGuardada, guardada, validacion.advertencias());
    }

    @Transactional
    public RespuestaSolicitudVacaciones aprobar(Long solicitudVacacionesId,
                                                SolicitudAprobarVacaciones solicitud,
                                                HttpServletRequest peticion) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesEmpresa(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();
        EstadoSolicitud estadoAnterior = sol.getEstado();

        LocalDate inicio = solicitud.fechaInicioAprobada() != null
                ? solicitud.fechaInicioAprobada() : sv.getFechaInicio();
        LocalDate fin = solicitud.fechaFinAprobada() != null
                ? solicitud.fechaFinAprobada() : sv.getFechaFin();
        int anio = inicio.getYear();

        ResultadoValidacion validacion = ejecutarValidaciones(
                sol.getEmpleado(), inicio, fin, anio, sol.getId());
        if (!validacion.esValida()) {
            throw new ExcepcionValidacionVacaciones(validacion.errores(), validacion.advertencias());
        }

        BigDecimal diasNuevos = BigDecimal.valueOf(CalculadorVacaciones.calcularDiasEntreFechas(inicio, fin));
        BigDecimal diasAnteriores = sv.getDiasSolicitados();
        if (diasNuevos.compareTo(diasAnteriores) != 0) {
            servicioSaldoVacaciones.liberarDiasPendientes(sol.getEmpleado().getId(), anio, diasAnteriores);
            servicioSaldoVacaciones.reservarDias(sol.getEmpleado().getId(), anio, diasNuevos);
            sv.setDiasSolicitados(diasNuevos);
        }

        sv.setFechaInicioAprobada(inicio);
        sv.setFechaFinAprobada(fin);
        repositorioSolicitudVacaciones.save(sv);

        Usuario usuario = servicioUsuario.obtenerEntidad(ContextoTenant.obtenerUsuarioId());
        servicioMotorWorkflow.procesarAccion(
                sol.getId(), AccionPaso.APROBAR, solicitud.comentarios(), usuario);

        servicioEventoSolicitudVacaciones.registrar(
                sv, TipoEventoSolicitudVacaciones.APROBADA,
                estadoAnterior, EstadoSolicitud.APROBADO, usuario.getId(),
                solicitud.comentarios(), peticion);

        return mapear(sv, repositorioSolicitud.findById(sol.getId()).orElse(sol), validacion.advertencias());
    }

    @Transactional
    public RespuestaSolicitudVacaciones contraproponer(Long solicitudVacacionesId,
                                                       SolicitudContrapropuestaVacaciones solicitud,
                                                       HttpServletRequest peticion) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesEmpresa(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();
        EstadoSolicitud estadoAnterior = sol.getEstado();

        if (solicitud.fechaInicioContrapropuesta() == null || solicitud.fechaFinContrapropuesta() == null) {
            throw new ExcepcionNegocio("Debe indicar las fechas de la contrapropuesta");
        }

        sv.setFechaInicioContrapropuesta(solicitud.fechaInicioContrapropuesta());
        sv.setFechaFinContrapropuesta(solicitud.fechaFinContrapropuesta());
        sv.setMotivoContrapropuesta(solicitud.motivo());
        repositorioSolicitudVacaciones.save(sv);

        Usuario usuario = servicioUsuario.obtenerEntidad(ContextoTenant.obtenerUsuarioId());
        servicioMotorWorkflow.procesarAccion(
                sol.getId(), AccionPaso.SOLICITAR_CAMBIOS, solicitud.motivo(), usuario);

        servicioEventoSolicitudVacaciones.registrar(
                sv, TipoEventoSolicitudVacaciones.CONTRAPROPUESTA,
                estadoAnterior, EstadoSolicitud.CAMBIOS_SOLICITADOS, usuario.getId(),
                solicitud.motivo(), peticion);

        publicadorEventos.publishEvent(new EventoVacacionesContrapropuesta(
                sol.getId(), sol.getEmpleado().getId(), sol.getEmpresa().getId()));

        return mapear(sv, repositorioSolicitud.findById(sol.getId()).orElse(sol), List.of());
    }

    @Transactional
    public RespuestaSolicitudVacaciones rechazar(Long solicitudVacacionesId,
                                                 SolicitudRechazarVacaciones solicitud,
                                                 HttpServletRequest peticion) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesEmpresa(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();
        EstadoSolicitud estadoAnterior = sol.getEstado();

        sv.setMotivoRechazo(solicitud.motivoRechazo());
        repositorioSolicitudVacaciones.save(sv);

        Usuario usuario = servicioUsuario.obtenerEntidad(ContextoTenant.obtenerUsuarioId());
        servicioMotorWorkflow.procesarAccion(
                sol.getId(), AccionPaso.RECHAZAR, solicitud.motivoRechazo(), usuario);

        servicioEventoSolicitudVacaciones.registrar(
                sv, TipoEventoSolicitudVacaciones.RECHAZADA,
                estadoAnterior, EstadoSolicitud.RECHAZADO, usuario.getId(),
                solicitud.motivoRechazo(), peticion);

        publicadorEventos.publishEvent(new EventoVacacionesRechazadas(
                sol.getId(), sol.getEmpleado().getId(), sol.getEmpresa().getId()));

        return mapear(sv, repositorioSolicitud.findById(sol.getId()).orElse(sol), List.of());
    }

    @Transactional
    public RespuestaSolicitudVacaciones aceptarContrapropuesta(Long solicitudVacacionesId,
                                                               HttpServletRequest peticion) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesPropia(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();

        if (sol.getEstado() != EstadoSolicitud.CAMBIOS_SOLICITADOS) {
            throw new ExcepcionNegocio("No hay contrapropuesta pendiente para aceptar");
        }
        if (sv.getFechaInicioContrapropuesta() == null || sv.getFechaFinContrapropuesta() == null) {
            throw new ExcepcionNegocio("La contrapropuesta no tiene fechas definidas");
        }

        EstadoSolicitud estadoAnterior = sol.getEstado();
        int anio = sv.getFechaInicioContrapropuesta().getYear();
        BigDecimal diasAnteriores = sv.getDiasSolicitados();
        LocalDate nuevoInicio = sv.getFechaInicioContrapropuesta();
        LocalDate nuevoFin = sv.getFechaFinContrapropuesta();

        ResultadoValidacion validacion = ejecutarValidaciones(
                sol.getEmpleado(), nuevoInicio, nuevoFin, anio, sol.getId());
        if (!validacion.esValida()) {
            throw new ExcepcionValidacionVacaciones(validacion.errores(), validacion.advertencias());
        }

        BigDecimal diasNuevos = BigDecimal.valueOf(CalculadorVacaciones.calcularDiasEntreFechas(nuevoInicio, nuevoFin));
        servicioSaldoVacaciones.liberarDiasPendientes(sol.getEmpleado().getId(), anio, diasAnteriores);
        servicioSaldoVacaciones.reservarDias(sol.getEmpleado().getId(), anio, diasNuevos);

        sv.setFechaInicio(nuevoInicio);
        sv.setFechaFin(nuevoFin);
        sv.setDiasSolicitados(diasNuevos);
        sv.setFechaInicioContrapropuesta(null);
        sv.setFechaFinContrapropuesta(null);
        sv.setMotivoContrapropuesta(null);
        repositorioSolicitudVacaciones.save(sv);

        sol.setEstado(EstadoSolicitud.EN_REVISION);
        repositorioSolicitud.save(sol);

        servicioEventoSolicitudVacaciones.registrar(
                sv, TipoEventoSolicitudVacaciones.CONTRAPROPUESTA_ACEPTADA,
                estadoAnterior, EstadoSolicitud.EN_REVISION, ContextoTenant.obtenerUsuarioId(),
                "Contrapropuesta aceptada por el empleado", peticion);

        return mapear(sv, sol, validacion.advertencias());
    }

    @Transactional
    public RespuestaSolicitudVacaciones cancelar(Long solicitudVacacionesId, HttpServletRequest peticion) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesPropia(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();
        EstadoSolicitud estadoAnterior = sol.getEstado();

        servicioSolicitud.cancelar(sol.getId());

        servicioEventoSolicitudVacaciones.registrar(
                sv, TipoEventoSolicitudVacaciones.CANCELADA,
                estadoAnterior, EstadoSolicitud.CANCELADO, ContextoTenant.obtenerUsuarioId(),
                null, peticion);

        return mapear(sv, repositorioSolicitud.findById(sol.getId()).orElse(sol), List.of());
    }

    @Transactional(readOnly = true)
    public RespuestaDetalleSolicitudVacaciones obtenerDetalle(Long solicitudVacacionesId) {
        SolicitudVacaciones sv = obtenerSolicitudVacacionesEmpresa(solicitudVacacionesId);
        Solicitud sol = sv.getSolicitud();
        List<DiaSemana> patron = servicioPatronDescanso.obtenerDiasDescansoVigentes(
                sol.getEmpleado().getId(), sv.getFechaInicio());
        ValidacionFechaInicio validacionFecha = ValidadorFechaInicioVacaciones.validar(
                sv.getFechaInicio(), patron);
        ResultadoValidacion validacion = ejecutarValidaciones(
                sol.getEmpleado(), sv.getFechaInicio(), sv.getFechaFin(),
                sv.getFechaInicio().getYear(), sol.getId());

        return new RespuestaDetalleSolicitudVacaciones(
                mapear(sv, sol, validacion.advertencias()),
                validacionFecha.esValida(),
                validacionFecha.fechaSugerida(),
                validacionFecha.motivo(),
                validacion.errores(),
                validacion.advertencias()
        );
    }

    @Transactional(readOnly = true)
    public List<RespuestaSolicitudVacaciones> listarPorEmpresa(EstadoSolicitud estado) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        List<SolicitudVacaciones> lista = estado == null
                ? repositorioSolicitudVacaciones.findByEmpresaId(empresaId)
                : repositorioSolicitudVacaciones.findByEmpresaIdAndEstado(empresaId, estado);
        return lista.stream()
                .map(sv -> mapear(sv, sv.getSolicitud(), List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaEventoCalendarioVacaciones> obtenerCalendario(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ExcepcionNegocio("La fecha hasta debe ser posterior a la fecha desde");
        }
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Long empleadoId = null;
        if (!tienePermiso("vacacion.ver")) {
            Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
            empleadoId = empleado.getId();
        }
        return repositorioSolicitudVacaciones.findByRangoFechas(empresaId, desde, hasta, empleadoId).stream()
                .map(sv -> {
                    var solicitud = sv.getSolicitud();
                    var empleado = solicitud.getEmpleado();
                    return new RespuestaEventoCalendarioVacaciones(
                            solicitud.getId(),
                            empleado.getId(),
                            empleado.getNombre() + " " + empleado.getApellido(),
                            sv.fechaInicioEfectiva(),
                            sv.fechaFinEfectiva(),
                            solicitud.getEstado(),
                            sv.getDiasSolicitados()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaSolicitudVacaciones> listarPropias() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return repositorioSolicitudVacaciones.findByEmpleadoId(empleado.getId()).stream()
                .map(sv -> mapear(sv, sv.getSolicitud(), List.of()))
                .toList();
    }

    public void procesarAprobacion(Long solicitudId) {
        SolicitudVacaciones solicitudVacaciones = repositorioSolicitudVacaciones.findBySolicitudId(solicitudId)
                .orElse(null);
        if (solicitudVacaciones == null) {
            return;
        }
        Long empleadoId = solicitudVacaciones.getSolicitud().getEmpleado().getId();
        int anio = solicitudVacaciones.fechaInicioEfectiva().getYear();
        servicioSaldoVacaciones.confirmarDias(empleadoId, anio, solicitudVacaciones.getDiasSolicitados());
        publicadorEventos.publishEvent(new EventoVacacionesAprobadas(
                solicitudId, empleadoId, solicitudVacaciones.getSolicitud().getEmpresa().getId()));
    }

    public void procesarRechazoOCancelacion(Long solicitudId) {
        SolicitudVacaciones solicitudVacaciones = repositorioSolicitudVacaciones.findBySolicitudId(solicitudId)
                .orElse(null);
        if (solicitudVacaciones == null) {
            return;
        }
        Solicitud solicitud = solicitudVacaciones.getSolicitud();
        if (solicitud.getEstado() == EstadoSolicitud.APROBADO) {
            return;
        }
        Long empleadoId = solicitud.getEmpleado().getId();
        int anio = solicitudVacaciones.getFechaInicio().getYear();
        servicioSaldoVacaciones.liberarDiasPendientes(empleadoId, anio, solicitudVacaciones.getDiasSolicitados());
    }

    private ResultadoValidacion ejecutarValidaciones(Empleado empleado, LocalDate inicio, LocalDate fin,
                                                    int anio, Long solicitudIdExcluir) {
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        List<DiaSemana> patron = servicioPatronDescanso.obtenerDiasDescansoVigentes(empleado.getId(), inicio);
        ValidacionFechaInicio validacionFecha = ValidadorFechaInicioVacaciones.validar(inicio, patron);
        if (!validacionFecha.esValida()) {
            errores.add(validacionFecha.motivo());
        }

        ConfiguracionVacaciones config = servicioConfiguracionVacaciones.obtenerEntidad(empleado.getEmpresa().getId());
        SaldoVacaciones saldo = servicioSaldoVacaciones.obtenerOCrearSaldoAnio(empleado, anio);
        BigDecimal saldoParaValidar = saldo.getDiasDisponibles();
        if (solicitudIdExcluir != null) {
            var svOpt = repositorioSolicitudVacaciones.findBySolicitudId(solicitudIdExcluir);
            if (svOpt.isPresent()) {
                saldoParaValidar = saldoParaValidar.add(svOpt.get().getDiasSolicitados());
            }
        }
        List<ValidadorSolicitudVacaciones.PeriodoSolicitudExistente> existentes =
                construirPeriodosExistentes(empleado.getId(), anio);

        ValidacionSolicitudVacaciones validacionSolicitud = ValidadorSolicitudVacaciones.validar(
                new ValidadorSolicitudVacaciones.DatosSolicitud(inicio, fin),
                saldoParaValidar,
                config,
                existentes,
                LocalDate.now(),
                solicitudIdExcluir);

        errores.addAll(validacionSolicitud.errores());
        advertencias.addAll(validacionSolicitud.advertencias());

        return new ResultadoValidacion(errores.isEmpty(), errores, advertencias);
    }

    private List<ValidadorSolicitudVacaciones.PeriodoSolicitudExistente> construirPeriodosExistentes(
            Long empleadoId, int anio) {
        return repositorioSolicitudVacaciones.findByEmpleadoIdAndAnio(empleadoId, anio).stream()
                .map(sv -> new ValidadorSolicitudVacaciones.PeriodoSolicitudExistente(
                        sv.getFechaInicio(),
                        sv.getFechaFin(),
                        sv.getSolicitud().getEstado(),
                        sv.getSolicitud().getId()))
                .toList();
    }

    private SolicitudVacaciones obtenerSolicitudVacacionesEmpresa(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioSolicitudVacaciones.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Solicitud de vacaciones no encontrada", 404));
    }

    private SolicitudVacaciones obtenerSolicitudVacacionesPropia(Long id) {
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());
        SolicitudVacaciones sv = repositorioSolicitudVacaciones.findById(id)
                .orElseThrow(() -> new ExcepcionNegocio("Solicitud de vacaciones no encontrada", 404));
        if (!sv.getSolicitud().getEmpleado().getId().equals(empleado.getId())) {
            throw new ExcepcionNegocio("No puede operar sobre solicitudes de otro empleado", 403);
        }
        return sv;
    }

    private boolean tienePermiso(String permiso) {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            return false;
        }
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permiso::equals);
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaSolicitudVacaciones mapear(SolicitudVacaciones sv, Solicitud solicitud,
                                                List<String> advertencias) {
        var empleado = solicitud.getEmpleado();
        return new RespuestaSolicitudVacaciones(
                solicitud.getId(),
                sv.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                sv.getFechaInicio(),
                sv.getFechaFin(),
                sv.getDiasSolicitados(),
                solicitud.getEstado(),
                solicitud.getMotivo(),
                sv.getFechaInicioContrapropuesta(),
                sv.getFechaFinContrapropuesta(),
                sv.getMotivoContrapropuesta(),
                sv.getFechaInicioAprobada(),
                sv.getFechaFinAprobada(),
                advertencias
        );
    }

    private record ResultadoValidacion(boolean esValida, List<String> errores, List<String> advertencias) {
    }

    public record EventoVacacionesSolicitadas(
            Long solicitudId, Long empleadoId, Long empresaId, BigDecimal dias) {
    }

    public record EventoVacacionesAprobadas(Long solicitudId, Long empleadoId, Long empresaId) {
    }

    public record EventoVacacionesRechazadas(Long solicitudId, Long empleadoId, Long empresaId) {
    }

    public record EventoVacacionesContrapropuesta(Long solicitudId, Long empleadoId, Long empresaId) {
    }
}
