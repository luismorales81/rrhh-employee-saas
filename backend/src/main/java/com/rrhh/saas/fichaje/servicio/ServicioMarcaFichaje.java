package com.rrhh.saas.fichaje.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comun.utilidad.UtilidadClienteHttp;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.fichaje.dominio.ConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dominio.EstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.MarcaFichaje;
import com.rrhh.saas.fichaje.dominio.MotivoEstadoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.PoliticaFichajeEmpresa;
import com.rrhh.saas.fichaje.dominio.TipoMarcaFichaje;
import com.rrhh.saas.fichaje.dominio.ZonaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaEstadoFichajeDia;
import com.rrhh.saas.fichaje.dto.RespuestaMarcaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaResumenFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.SolicitudMarcarFichaje;
import com.rrhh.saas.fichaje.repositorio.RepositorioMarcaFichaje;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServicioMarcaFichaje {

    private final RepositorioMarcaFichaje repositorioMarca;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioConfiguracionFichaje servicioConfiguracion;
    private final ServicioPoliticaFichaje servicioPolitica;
    private final ServicioZonaFichaje servicioZona;
    private final ServicioGeocerca servicioGeocerca;
    private final ServicioAuditoria servicioAuditoria;
    private final RepositorioUsuario repositorioUsuario;

    public ServicioMarcaFichaje(RepositorioMarcaFichaje repositorioMarca,
                                ServicioEmpleado servicioEmpleado,
                                ServicioConfiguracionFichaje servicioConfiguracion,
                                ServicioPoliticaFichaje servicioPolitica,
                                ServicioZonaFichaje servicioZona,
                                ServicioGeocerca servicioGeocerca,
                                ServicioAuditoria servicioAuditoria,
                                RepositorioUsuario repositorioUsuario) {
        this.repositorioMarca = repositorioMarca;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioConfiguracion = servicioConfiguracion;
        this.servicioPolitica = servicioPolitica;
        this.servicioZona = servicioZona;
        this.servicioGeocerca = servicioGeocerca;
        this.servicioAuditoria = servicioAuditoria;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Transactional
    public RespuestaMarcaFichaje marcar(SolicitudMarcarFichaje solicitud, HttpServletRequest peticion) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no autenticado", 401);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        ConfiguracionFichajeEmpleado config = servicioConfiguracion.obtenerOcrearConfig(empleado);
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPolitica();

        if (!config.isFichajeMovilHabilitado()) {
            throw new ExcepcionNegocio("El fichaje móvil no está habilitado", 403);
        }
        if (servicioConfiguracion.requiereConsentimiento(config, politica)) {
            throw new ExcepcionNegocio("Debe aceptar la política de fichaje antes de marcar", 403);
        }

        validarSecuencia(empleado.getId(), solicitud.tipo());

        MarcaFichaje marca = new MarcaFichaje();
        marca.setEmpresa(empleado.getEmpresa());
        marca.setEmpleado(empleado);
        marca.setTipo(solicitud.tipo());
        marca.setFechaHoraServidor(LocalDateTime.now());
        marca.setFechaHoraDispositivo(solicitud.fechaHoraDispositivo());
        marca.setLatitud(solicitud.latitud());
        marca.setLongitud(solicitud.longitud());
        marca.setPrecisionMetros(solicitud.precisionMetros());
        marca.setIdDispositivo(solicitud.idDispositivo());
        marca.setModeloDispositivo(solicitud.modeloDispositivo());
        marca.setSistemaOperativo(solicitud.sistemaOperativo());
        marca.setVersionApp(solicitud.versionApp());
        if (peticion != null) {
            marca.setDireccionIp(UtilidadClienteHttp.obtenerDireccionIp(peticion));
            marca.setUserAgent(UtilidadClienteHttp.obtenerUserAgent(peticion));
        }

        aplicarValidacionGeografica(marca, empleado, config, politica, solicitud);

        MarcaFichaje guardada = repositorioMarca.save(marca);
        servicioAuditoria.registrar("MARCAR", "MarcaFichaje", guardada.getId(),
                solicitud.tipo().name(), guardada.getEstado().name());
        return mapear(guardada);
    }

    @Transactional
    public RespuestaEstadoFichajeDia obtenerEstadoDia() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        ConfiguracionFichajeEmpleado config = servicioConfiguracion.obtenerOcrearConfig(empleado);
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPolitica();
        boolean habilitado = config.isFichajeMovilHabilitado();
        boolean requiereConsentimiento = servicioConfiguracion.requiereConsentimiento(config, politica);

        Optional<MarcaFichaje> ultima = repositorioMarca
                .findFirstByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(empleado.getId());

        TipoMarcaFichaje siguiente = TipoMarcaFichaje.ENTRADA;
        String ultimaHora = null;
        TipoMarcaFichaje ultimaTipo = null;
        if (ultima.isPresent() && ultima.get().getEstado() != EstadoMarcaFichaje.RECHAZADA) {
            MarcaFichaje marca = ultima.get();
            ultimaTipo = marca.getTipo();
            ultimaHora = marca.getFechaHoraServidor().format(DateTimeFormatter.ofPattern("HH:mm"));
            siguiente = marca.getTipo() == TipoMarcaFichaje.ENTRADA
                    ? TipoMarcaFichaje.SALIDA : TipoMarcaFichaje.ENTRADA;
        }

        return new RespuestaEstadoFichajeDia(siguiente, habilitado, requiereConsentimiento, ultimaHora, ultimaTipo);
    }

    @Transactional(readOnly = true)
    public List<RespuestaMarcaFichaje> listarMisMarcas() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return repositorioMarca.findByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(empleado.getId())
                .stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaMarcaFichaje> listarMarcas(Long empleadoId, EstadoMarcaFichaje estado,
                                                    Long zonaId, LocalDate desde, LocalDate hasta) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime fin = hasta != null ? hasta.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        return repositorioMarca.buscarConFiltros(empresaId, empleadoId, estado, zonaId, inicio, fin)
                .stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaMarcaFichaje obtenerMarca(Long id) {
        return mapear(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<RespuestaMarcaFichaje> listarPendientesRevision() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioMarca
                .findByEmpresaIdAndEstadoAndDeletedFalseOrderByFechaHoraServidorDesc(
                        empresaId, EstadoMarcaFichaje.PENDIENTE_REVISION)
                .stream().map(this::mapear).toList();
    }

    @Transactional
    public RespuestaMarcaFichaje aprobar(Long id) {
        MarcaFichaje marca = obtenerEntidad(id);
        if (marca.getEstado() != EstadoMarcaFichaje.PENDIENTE_REVISION) {
            throw new ExcepcionNegocio("La marca no está pendiente de revisión", 400);
        }
        marca.setEstado(EstadoMarcaFichaje.VALIDADA);
        marca.setRevisadoAt(LocalDateTime.now());
        marca.setRevisadoPorUsuario(obtenerUsuarioActual());
        MarcaFichaje guardada = repositorioMarca.save(marca);
        servicioAuditoria.registrar("APROBAR", "MarcaFichaje", guardada.getId(), null, null);
        return mapear(guardada);
    }

    @Transactional
    public RespuestaMarcaFichaje rechazar(Long id, String notaRevision) {
        MarcaFichaje marca = obtenerEntidad(id);
        if (marca.getEstado() != EstadoMarcaFichaje.PENDIENTE_REVISION) {
            throw new ExcepcionNegocio("La marca no está pendiente de revisión", 400);
        }
        marca.setEstado(EstadoMarcaFichaje.RECHAZADA);
        marca.setNotaRevision(notaRevision);
        marca.setRevisadoAt(LocalDateTime.now());
        marca.setRevisadoPorUsuario(obtenerUsuarioActual());
        MarcaFichaje guardada = repositorioMarca.save(marca);
        servicioAuditoria.registrar("RECHAZAR", "MarcaFichaje", guardada.getId(), notaRevision, null);
        return mapear(guardada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaResumenFichajeEmpleado> generarResumen(LocalDate desde, LocalDate hasta) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        LocalDate inicio = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        List<MarcaFichaje> marcas = repositorioMarca.buscarConFiltros(
                empresaId, null, null, null,
                inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());

        Map<String, List<MarcaFichaje>> agrupadas = new LinkedHashMap<>();
        for (MarcaFichaje marca : marcas) {
            if (marca.getEstado() == EstadoMarcaFichaje.RECHAZADA) {
                continue;
            }
            String clave = marca.getEmpleado().getId() + "|" + marca.getFechaHoraServidor().toLocalDate();
            agrupadas.computeIfAbsent(clave, k -> new ArrayList<>()).add(marca);
        }

        List<RespuestaResumenFichajeEmpleado> resultado = new ArrayList<>();
        for (Map.Entry<String, List<MarcaFichaje>> entrada : agrupadas.entrySet()) {
            List<MarcaFichaje> delDia = entrada.getValue();
            delDia.sort(Comparator.comparing(MarcaFichaje::getFechaHoraServidor));
            Empleado empleado = delDia.get(0).getEmpleado();
            LocalDate fecha = delDia.get(0).getFechaHoraServidor().toLocalDate();
            int entradas = (int) delDia.stream().filter(m -> m.getTipo() == TipoMarcaFichaje.ENTRADA).count();
            int salidas = (int) delDia.stream().filter(m -> m.getTipo() == TipoMarcaFichaje.SALIDA).count();
            int pendientes = (int) delDia.stream()
                    .filter(m -> m.getEstado() == EstadoMarcaFichaje.PENDIENTE_REVISION).count();
            resultado.add(new RespuestaResumenFichajeEmpleado(
                    empleado.getId(),
                    empleado.getNombre() + " " + empleado.getApellido(),
                    fecha,
                    entradas,
                    salidas,
                    pendientes,
                    calcularHorasEstimadas(delDia)
            ));
        }
        resultado.sort(Comparator.comparing(RespuestaResumenFichajeEmpleado::fecha).reversed()
                .thenComparing(RespuestaResumenFichajeEmpleado::empleadoNombre));
        return resultado;
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(LocalDate desde, LocalDate hasta, Long empleadoId) {
        List<RespuestaMarcaFichaje> marcas = listarMarcas(empleadoId, null, null, desde, hasta);
        StringBuilder sb = new StringBuilder();
        sb.append("id,empleado,tipo,fecha_hora,estado,zona,latitud,longitud,dispositivo\n");
        for (RespuestaMarcaFichaje m : marcas) {
            sb.append(m.id()).append(',');
            sb.append(escaparCsv(m.empleadoNombre())).append(',');
            sb.append(m.tipo()).append(',');
            sb.append(m.fechaHoraServidor()).append(',');
            sb.append(m.estado()).append(',');
            sb.append(escaparCsv(m.zonaFichajeNombre() != null ? m.zonaFichajeNombre() : "")).append(',');
            sb.append(m.latitud() != null ? m.latitud() : "").append(',');
            sb.append(m.longitud() != null ? m.longitud() : "").append(',');
            sb.append(escaparCsv(m.modeloDispositivo() != null ? m.modeloDispositivo() : ""));
            sb.append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void aplicarValidacionGeografica(MarcaFichaje marca, Empleado empleado,
                                             ConfiguracionFichajeEmpleado config,
                                             PoliticaFichajeEmpresa politica,
                                             SolicitudMarcarFichaje solicitud) {
        if (solicitud.latitud() == null || solicitud.longitud() == null) {
            if (politica.isPermitirFichajeSinGps()) {
                marca.setEstado(EstadoMarcaFichaje.PENDIENTE_REVISION);
                marca.setMotivoEstado(MotivoEstadoMarcaFichaje.FUERA_DE_ZONA);
                return;
            }
            throw new ExcepcionNegocio("Se requiere ubicación GPS para fichar", 400);
        }

        if (solicitud.precisionMetros() != null
                && solicitud.precisionMetros() > politica.getPrecisionMaximaMetros()) {
            marca.setEstado(EstadoMarcaFichaje.PENDIENTE_REVISION);
            marca.setMotivoEstado(MotivoEstadoMarcaFichaje.PRECISION_BAJA);
            return;
        }

        List<ZonaFichaje> zonasHabilitadas = servicioZona.resolverZonasHabilitadas(
                empleado, config.isRestringirSoloZonasAsignadas());
        if (zonasHabilitadas.isEmpty()) {
            marca.setEstado(EstadoMarcaFichaje.PENDIENTE_REVISION);
            marca.setMotivoEstado(MotivoEstadoMarcaFichaje.SIN_ZONA_HABILITADA);
            return;
        }

        Optional<ZonaFichaje> zona = servicioZona.buscarZonaCoincidente(
                empleado, solicitud.latitud(), solicitud.longitud(),
                config.isRestringirSoloZonasAsignadas(), servicioGeocerca);
        if (zona.isPresent()) {
            marca.setZonaFichaje(zona.get());
            marca.setEstado(EstadoMarcaFichaje.VALIDADA);
        } else {
            marca.setEstado(EstadoMarcaFichaje.PENDIENTE_REVISION);
            marca.setMotivoEstado(MotivoEstadoMarcaFichaje.FUERA_DE_ZONA);
        }
    }

    private void validarSecuencia(Long empleadoId, TipoMarcaFichaje tipo) {
        Optional<MarcaFichaje> ultima = repositorioMarca
                .findFirstByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(empleadoId);
        if (ultima.isEmpty()) {
            if (tipo != TipoMarcaFichaje.ENTRADA) {
                throw new ExcepcionNegocio("La primera marca del día debe ser una entrada", 400);
            }
            return;
        }
        MarcaFichaje anterior = ultima.get();
        if (anterior.getEstado() == EstadoMarcaFichaje.RECHAZADA) {
            if (tipo != TipoMarcaFichaje.ENTRADA && anterior.getTipo() == TipoMarcaFichaje.SALIDA) {
                // permitir reintentar según última válida
            }
        }
        if (anterior.getEstado() != EstadoMarcaFichaje.RECHAZADA) {
            if (anterior.getTipo() == tipo) {
                String esperado = tipo == TipoMarcaFichaje.ENTRADA ? "salida" : "entrada";
                throw new ExcepcionNegocio("Debe registrar una " + esperado + " antes de otra "
                        + tipo.name().toLowerCase(), 400);
            }
        } else {
            TipoMarcaFichaje ultimaNoRechazada = buscarUltimaNoRechazada(empleadoId);
            if (ultimaNoRechazada != null && ultimaNoRechazada == tipo) {
                String esperado = tipo == TipoMarcaFichaje.ENTRADA ? "salida" : "entrada";
                throw new ExcepcionNegocio("Debe registrar una " + esperado + " antes de otra "
                        + tipo.name().toLowerCase(), 400);
            }
        }
    }

    private TipoMarcaFichaje buscarUltimaNoRechazada(Long empleadoId) {
        return repositorioMarca.findByEmpleadoIdAndDeletedFalseOrderByFechaHoraServidorDesc(empleadoId).stream()
                .filter(m -> m.getEstado() != EstadoMarcaFichaje.RECHAZADA)
                .findFirst()
                .map(MarcaFichaje::getTipo)
                .orElse(null);
    }

    private String calcularHorasEstimadas(List<MarcaFichaje> marcas) {
        long minutos = 0;
        LocalDateTime entrada = null;
        for (MarcaFichaje marca : marcas) {
            if (marca.getEstado() == EstadoMarcaFichaje.RECHAZADA) {
                continue;
            }
            if (marca.getTipo() == TipoMarcaFichaje.ENTRADA) {
                entrada = marca.getFechaHoraServidor();
            } else if (marca.getTipo() == TipoMarcaFichaje.SALIDA && entrada != null) {
                minutos += Duration.between(entrada, marca.getFechaHoraServidor()).toMinutes();
                entrada = null;
            }
        }
        if (minutos <= 0) {
            return "—";
        }
        long horas = minutos / 60;
        long mins = minutos % 60;
        return horas + "h " + mins + "m";
    }

    private MarcaFichaje obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioMarca.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Marca de fichaje no encontrada", 404));
    }

    private Usuario obtenerUsuarioActual() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        return repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new ExcepcionNegocio("Usuario no encontrado", 404));
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    private RespuestaMarcaFichaje mapear(MarcaFichaje marca) {
        Empleado empleado = marca.getEmpleado();
        ZonaFichaje zona = marca.getZonaFichaje();
        return new RespuestaMarcaFichaje(
                marca.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                marca.getTipo(),
                marca.getFechaHoraServidor(),
                marca.getFechaHoraDispositivo(),
                marca.getLatitud(),
                marca.getLongitud(),
                marca.getPrecisionMetros(),
                marca.getIdDispositivo(),
                marca.getModeloDispositivo(),
                marca.getSistemaOperativo(),
                marca.getVersionApp(),
                zona != null ? zona.getId() : null,
                zona != null ? zona.getNombre() : null,
                marca.getEstado(),
                marca.getMotivoEstado(),
                marca.getNotaRevision(),
                marca.getRevisadoAt()
        );
    }
}
