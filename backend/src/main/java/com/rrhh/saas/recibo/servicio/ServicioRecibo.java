package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comun.utilidad.UtilidadClienteHttp;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioAutenticacion;
import com.rrhh.saas.nucleo.servicio.ServicioCorreo;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.notificacion.servicio.ServicioNotificacion;
import com.rrhh.saas.recibo.config.PropiedadesRecibos;
import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.EventoFirmaRecibo;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.dominio.TipoConfirmacionRecibo;
import com.rrhh.saas.recibo.dominio.TipoEventoFirmaRecibo;
import com.rrhh.saas.recibo.dto.RespuestaAlertasRecibos;
import com.rrhh.saas.recibo.dto.RespuestaEventoFirmaRecibo;
import com.rrhh.saas.recibo.dto.RespuestaRecibo;
import com.rrhh.saas.recibo.dto.SolicitudCargarRecibo;
import com.rrhh.saas.recibo.dto.SolicitudConfirmarRecepcionRecibo;
import com.rrhh.saas.recibo.dto.SolicitudRepublicarRecibo;
import com.rrhh.saas.recibo.repositorio.RepositorioEventoFirmaRecibo;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ServicioRecibo {

    private static final Set<EstadoRecibo> ESTADOS_PENDIENTES_FIRMA = EnumSet.of(EstadoRecibo.PENDIENTE, EstadoRecibo.VISTO);

    private final RepositorioRecibo repositorio;
    private final RepositorioEventoFirmaRecibo repositorioEventoFirma;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioCorreo servicioCorreo;
    private final ServicioAutenticacion servicioAutenticacion;
    private final PropiedadesRecibos propiedadesRecibos;

    public ServicioRecibo(RepositorioRecibo repositorio,
                          RepositorioEventoFirmaRecibo repositorioEventoFirma,
                          ServicioEmpleado servicioEmpleado,
                          ServicioEmpresa servicioEmpresa,
                          ServicioAuditoria servicioAuditoria,
                          ServicioNotificacion servicioNotificacion,
                          ServicioAlmacenamiento servicioAlmacenamiento,
                          ServicioCorreo servicioCorreo,
                          ServicioAutenticacion servicioAutenticacion,
                          PropiedadesRecibos propiedadesRecibos) {
        this.repositorio = repositorio;
        this.repositorioEventoFirma = repositorioEventoFirma;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioCorreo = servicioCorreo;
        this.servicioAutenticacion = servicioAutenticacion;
        this.propiedadesRecibos = propiedadesRecibos;
    }

    @Transactional
    public RespuestaRecibo cargar(SolicitudCargarRecibo solicitud) {
        return publicarRecibo(
                solicitud.empleadoId(),
                solicitud.periodo(),
                solicitud.titulo(),
                solicitud.archivoId(),
                solicitud.montoNeto(),
                solicitud.montoBruto()
        );
    }

    @Transactional
    public RespuestaRecibo publicarRecibo(Long empleadoId, String periodo, String titulo, Long archivoId,
                                          BigDecimal montoNeto, BigDecimal montoBruto) {
        if (archivoId == null) {
            throw new ExcepcionNegocio("El archivoId es obligatorio para publicar un recibo");
        }

        Empresa empresa = obtenerEmpresaActual();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresa.getId());

        if (repositorio.existsByEmpleadoIdAndPeriodoAndVigenteTrueAndDeletedFalse(empleadoId, periodo)) {
            throw new ExcepcionNegocio("Ya existe un recibo vigente para este empleado en el período " + periodo);
        }

        ArchivoAlmacenado archivo = servicioAlmacenamiento.obtenerEntidad(archivoId);
        Long publicadoPorId = ContextoTenant.obtenerUsuarioId();

        Recibo recibo = new Recibo();
        recibo.setEmpresa(empresa);
        recibo.setEmpleado(empleado);
        recibo.setPeriodo(periodo);
        recibo.setTitulo(titulo);
        recibo.setMontoNeto(montoNeto);
        recibo.setMontoBruto(montoBruto);
        recibo.setPublicadoPorId(publicadoPorId);
        recibo.setEstado(EstadoRecibo.PENDIENTE);
        recibo.setFechaPublicacion(LocalDateTime.now());
        recibo.setArchivo(archivo);
        recibo.setHashPdf(archivo.getHashSha256());
        recibo.setUrlArchivo(servicioAlmacenamiento.construirUrlContenido(archivo.getId()));
        recibo.setRequiereRepublicacion(false);
        recibo.setVersion(1);
        recibo.setVigente(true);

        Recibo guardado = repositorio.save(recibo);
        servicioAuditoria.registrar("CARGAR", "Recibo", guardado.getId(), null, guardado.getTitulo());

        notificarDisponibilidadRecibo(guardado, empleado, empresa.getId());
        return mapear(guardado, true);
    }

    @Transactional
    public RespuestaRecibo republicar(Long id, SolicitudRepublicarRecibo solicitud) {
        Recibo anterior = obtenerReciboEmpresa(id);
        if (!anterior.isVigente()) {
            throw new ExcepcionNegocio("Solo se puede republicar el recibo vigente de un período", 400);
        }

        boolean tieneFirma = tieneEventoFirmaDefinitivo(anterior.getId());
        if (tieneFirma && anterior.getEstado() != EstadoRecibo.FIRMADO
                && anterior.getEstado() != EstadoRecibo.DISCONFORMIDAD) {
            throw new ExcepcionNegocio("Estado inconsistente del recibo anterior", 400);
        }
        if (!tieneFirma && anterior.getEstado() != EstadoRecibo.PENDIENTE
                && anterior.getEstado() != EstadoRecibo.VISTO) {
            throw new ExcepcionNegocio("Solo se puede republicar recibos pendientes o ya firmados", 400);
        }

        ArchivoAlmacenado archivo = servicioAlmacenamiento.obtenerEntidad(solicitud.archivoId());
        Long publicadoPorId = ContextoTenant.obtenerUsuarioId();

        anterior.setVigente(false);
        anterior.setMotivoRepublicacion(solicitud.motivo());
        repositorio.save(anterior);

        Recibo nuevo = new Recibo();
        nuevo.setEmpresa(anterior.getEmpresa());
        nuevo.setEmpleado(anterior.getEmpleado());
        nuevo.setPeriodo(anterior.getPeriodo());
        nuevo.setTitulo(anterior.getTitulo());
        nuevo.setMontoNeto(anterior.getMontoNeto());
        nuevo.setMontoBruto(anterior.getMontoBruto());
        nuevo.setPublicadoPorId(publicadoPorId);
        nuevo.setEstado(EstadoRecibo.PENDIENTE);
        nuevo.setFechaPublicacion(LocalDateTime.now());
        nuevo.setArchivo(archivo);
        nuevo.setHashPdf(archivo.getHashSha256());
        nuevo.setUrlArchivo(servicioAlmacenamiento.construirUrlContenido(archivo.getId()));
        nuevo.setRequiereRepublicacion(false);
        nuevo.setVersion(anterior.getVersion() + 1);
        nuevo.setReciboAnterior(anterior);
        nuevo.setVigente(true);

        Recibo guardado = repositorio.save(nuevo);
        servicioAuditoria.registrar("REPUBLICAR", "Recibo", guardado.getId(),
                "v" + anterior.getVersion(), "v" + guardado.getVersion() + ": " + solicitud.motivo());

        notificarDisponibilidadRecibo(guardado, anterior.getEmpleado(), anterior.getEmpresa().getId());
        return mapear(guardado, true);
    }

    @Transactional
    public RespuestaRecibo reenviarNotificacion(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        if (!recibo.isVigente()) {
            throw new ExcepcionNegocio("Solo se puede renotificar el recibo vigente", 400);
        }
        notificarDisponibilidadRecibo(recibo, recibo.getEmpleado(), recibo.getEmpresa().getId());
        return mapear(recibo, true);
    }

    @Transactional
    public void eliminarLogico(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        validarPuedeEliminar(recibo);
        recibo.setDeleted(true);
        recibo.setDeletedAt(LocalDateTime.now());
        repositorio.save(recibo);
        servicioAuditoria.registrar("ELIMINAR", "Recibo", id, recibo.getTitulo(), null);
    }

    @Transactional
    public RespuestaRecibo obtenerPorId(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        validarAccesoRecibo(recibo);
        return mapear(recibo, puedeVerHashPdf());
    }

    @Transactional
    public RespuestaRecibo registrarVisualizacion(Long id, HttpServletRequest peticion) {
        Recibo recibo = obtenerReciboPropio(id);
        validarReciboVigenteParaFirma(recibo);
        validarTieneHashParaFirma(recibo);

        if (recibo.getVistoPorEmpleadoEn() == null) {
            recibo.setVistoPorEmpleadoEn(LocalDateTime.now());
            if (recibo.getEstado() == EstadoRecibo.PENDIENTE) {
                recibo.setEstado(EstadoRecibo.VISTO);
            }
            repositorio.save(recibo);
        }

        registrarEvento(recibo, TipoEventoFirmaRecibo.VISTO, null, peticion);
        return mapear(recibo, false);
    }

    @Transactional
    public RespuestaRecibo confirmarRecepcion(Long id, SolicitudConfirmarRecepcionRecibo solicitud,
                                              HttpServletRequest peticion) {
        Recibo recibo = obtenerReciboPropio(id);
        validarReciboVigenteParaFirma(recibo);
        validarTieneHashParaFirma(recibo);

        if (propiedadesRecibos.isRequiereContrasenaFirma()) {
            servicioAutenticacion.verificarContrasenaActual(solicitud.contrasena());
        }

        if (recibo.getEstado() == EstadoRecibo.FIRMADO || recibo.getEstado() == EstadoRecibo.DISCONFORMIDAD) {
            throw new ExcepcionNegocio("Este recibo ya fue firmado", 400);
        }

        if (solicitud.tipo() == TipoConfirmacionRecibo.DISCONFORMIDAD) {
            if (solicitud.comentario() == null || solicitud.comentario().trim().length() < 10) {
                throw new ExcepcionNegocio("Debe indicar el motivo de disconformidad (mínimo 10 caracteres)");
            }
        }

        verificarIntegridadPdf(recibo);

        TipoEventoFirmaRecibo tipoEvento = solicitud.tipo() == TipoConfirmacionRecibo.CONFIRMADO
                ? TipoEventoFirmaRecibo.CONFIRMADO
                : TipoEventoFirmaRecibo.DISCONFORMIDAD;

        registrarEvento(recibo, tipoEvento, solicitud.comentario(), peticion);

        recibo.setEstado(solicitud.tipo() == TipoConfirmacionRecibo.CONFIRMADO
                ? EstadoRecibo.FIRMADO
                : EstadoRecibo.DISCONFORMIDAD);
        recibo.setFechaFirma(LocalDateTime.now());
        if (recibo.getVistoPorEmpleadoEn() == null) {
            recibo.setVistoPorEmpleadoEn(LocalDateTime.now());
        }

        Recibo guardado = repositorio.save(recibo);
        servicioAuditoria.registrar("FIRMAR", "Recibo", guardado.getId(), null, solicitud.tipo().name());

        if (solicitud.tipo() == TipoConfirmacionRecibo.DISCONFORMIDAD) {
            notificarDisconformidadRrhh(guardado, solicitud.comentario());
        }

        return mapear(guardado, false);
    }

    @Transactional
    public RespuestaRecibo firmar(Long id, HttpServletRequest peticion) {
        return confirmarRecepcion(id, new SolicitudConfirmarRecepcionRecibo(
                TipoConfirmacionRecibo.CONFIRMADO, null, null), peticion);
    }

    @Transactional(readOnly = true)
    public Resource obtenerPdf(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        validarAccesoRecibo(recibo);

        if (recibo.getArchivo() != null) {
            return servicioAlmacenamiento.obtenerContenido(recibo.getArchivo().getId());
        }
        throw new ExcepcionNegocio("El recibo no tiene archivo PDF almacenado en el sistema", 404);
    }

    @Transactional(readOnly = true)
    public String obtenerTipoMimePdf(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        if (recibo.getArchivo() != null) {
            return recibo.getArchivo().getTipoMime();
        }
        return "application/pdf";
    }

    @Transactional(readOnly = true)
    public List<RespuestaEventoFirmaRecibo> listarEventosFirma(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        validarAccesoRecibo(recibo);
        return repositorioEventoFirma.findByReciboIdOrderByTimestampEventoAsc(id).stream()
                .map(this::mapearEvento)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaRecibo> listarEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndVigenteTrueAndDeletedFalseOrderByFechaPublicacionDesc(empresaId).stream()
                .map(r -> mapear(r, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaRecibo> listarPropios() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return repositorio.findByEmpleadoIdAndVigenteTrueAndDeletedFalseOrderByFechaPublicacionDesc(empleado.getId())
                .stream()
                .map(r -> mapear(r, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaRecibo> listarPorEmpleado(Long empleadoId) {
        Empresa empresa = obtenerEmpresaActual();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresa.getId());
        return repositorio.findByEmpleadoIdAndDeletedFalseOrderByFechaPublicacionDesc(empleadoId).stream()
                .map(r -> mapear(r, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaRecibo> listarVersiones(Long id) {
        Recibo recibo = obtenerReciboEmpresa(id);
        validarAccesoRecibo(recibo);
        Long raizId = recibo.getReciboAnterior() != null
                ? encontrarRaizVersion(recibo).getId()
                : recibo.getId();
        Recibo raiz = repositorio.findById(raizId).orElse(recibo);
        return construirCadenaVersiones(raiz);
    }

    @Transactional(readOnly = true)
    public RespuestaAlertasRecibos obtenerResumenAlertas() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        int dias = propiedadesRecibos.getDiasAlertaPendiente();
        LocalDateTime umbral = LocalDateTime.now().minusDays(dias);

        List<Recibo> antiguos = repositorio
                .findByEmpresaIdAndVigenteTrueAndEstadoInAndDeletedFalseAndFechaPublicacionBefore(
                        empresaId, ESTADOS_PENDIENTES_FIRMA, umbral);
        List<Recibo> disconformidades = repositorio
                .findByEmpresaIdAndVigenteTrueAndEstadoAndDeletedFalseOrderByFechaFirmaDesc(
                        empresaId, EstadoRecibo.DISCONFORMIDAD);

        long totalPendientes = repositorio.countByEmpresaIdAndVigenteTrueAndEstadoInAndDeletedFalse(
                empresaId, ESTADOS_PENDIENTES_FIRMA);

        return new RespuestaAlertasRecibos(
                (int) totalPendientes,
                disconformidades.size(),
                antiguos.size(),
                dias,
                antiguos.stream().map(r -> mapearAlerta(r)).toList(),
                disconformidades.stream().limit(20).map(r -> mapearAlerta(r)).toList()
        );
    }

    @Transactional(readOnly = true)
    public Recibo obtenerReciboEmpresaEntidad(Long id) {
        return obtenerReciboEmpresa(id);
    }

    void validarCamposInmutables(Recibo anterior, Recibo modificado) {
        if (anterior.getFechaPublicacion() == null) {
            return;
        }
        if (!Objects.equals(anterior.getHashPdf(), modificado.getHashPdf())
                || !Objects.equals(idArchivo(anterior), idArchivo(modificado))
                || !Objects.equals(anterior.getEmpleado().getId(), modificado.getEmpleado().getId())
                || !Objects.equals(anterior.getPeriodo(), modificado.getPeriodo())) {
            throw new ExcepcionNegocio(
                    "No se pueden modificar hash, archivo, empleado ni período de un recibo publicado", 400);
        }
    }

    private void notificarDisponibilidadRecibo(Recibo recibo, Empleado empleado, Long empresaId) {
        if (empleado.getUsuario() != null) {
            servicioNotificacion.crear(
                    empresaId,
                    empleado.getUsuario().getId(),
                    "Nuevo recibo disponible",
                    "Tu recibo de " + recibo.getPeriodo() + " ya está disponible para firmar.",
                    "RECIBO",
                    recibo.getId()
            );
        }

        String email = resolverEmailEmpleado(empleado);
        String cuerpo = "Hola " + empleado.getNombre() + ",\n\n"
                + "Tu recibo de sueldo del período " + recibo.getPeriodo()
                + " fue puesto a disposición en el portal del empleado.\n"
                + "Ingresá a: " + propiedadesRecibos.getUrlMisRecibos() + "\n\n"
                + "Este mensaje constituye notificación de puesta a disposición (Ley 27.802).";

        boolean enviado = servicioCorreo.enviarSiHabilitado(
                email,
                "Recibo disponible — período " + recibo.getPeriodo(),
                cuerpo
        );

        recibo.setNotificadoEn(LocalDateTime.now());
        recibo.setCanalNotificacion(enviado ? "EMAIL+IN_APP" : "IN_APP");
        repositorio.save(recibo);
    }

    private void notificarDisconformidadRrhh(Recibo recibo, String comentario) {
        Long publicadoPorId = recibo.getPublicadoPorId();
        if (publicadoPorId == null) {
            return;
        }
        Empleado empleado = recibo.getEmpleado();
        servicioNotificacion.crear(
                recibo.getEmpresa().getId(),
                publicadoPorId,
                "Disconformidad en recibo",
                empleado.getNombre() + " " + empleado.getApellido()
                        + " firmó en disconformidad el recibo " + recibo.getPeriodo()
                        + ": " + comentario,
                "RECIBO_DISCONFORMIDAD",
                recibo.getId()
        );
    }

    private String resolverEmailEmpleado(Empleado empleado) {
        if (empleado.getEmailLaboral() != null && !empleado.getEmailLaboral().isBlank()) {
            return empleado.getEmailLaboral();
        }
        if (empleado.getEmailPersonal() != null && !empleado.getEmailPersonal().isBlank()) {
            return empleado.getEmailPersonal();
        }
        if (empleado.getUsuario() != null) {
            return empleado.getUsuario().getEmail();
        }
        return null;
    }

    private List<RespuestaRecibo> construirCadenaVersiones(Recibo raiz) {
        List<RespuestaRecibo> versiones = new java.util.ArrayList<>();
        versiones.add(mapear(raiz, true));
        Recibo actual = raiz;
        while (true) {
            List<Recibo> hijos = repositorio.findByReciboAnteriorIdOrderByVersionAsc(actual.getId());
            if (hijos.isEmpty()) {
                break;
            }
            actual = hijos.get(hijos.size() - 1);
            versiones.add(mapear(actual, true));
        }
        return versiones;
    }

    private Recibo encontrarRaizVersion(Recibo recibo) {
        Recibo actual = recibo;
        while (actual.getReciboAnterior() != null) {
            actual = actual.getReciboAnterior();
        }
        return actual;
    }

    private boolean tieneEventoFirmaDefinitivo(Long reciboId) {
        return repositorioEventoFirma.findByReciboIdOrderByTimestampEventoAsc(reciboId).stream()
                .anyMatch(e -> e.getTipo() == TipoEventoFirmaRecibo.CONFIRMADO
                        || e.getTipo() == TipoEventoFirmaRecibo.DISCONFORMIDAD);
    }

    private void validarReciboVigenteParaFirma(Recibo recibo) {
        if (!recibo.isVigente()) {
            throw new ExcepcionNegocio("Este recibo fue reemplazado por una nueva versión", 400);
        }
    }

    private RespuestaAlertasRecibos.ReciboAlertaResumen mapearAlerta(Recibo recibo) {
        Empleado empleado = recibo.getEmpleado();
        return new RespuestaAlertasRecibos.ReciboAlertaResumen(
                recibo.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                recibo.getPeriodo(),
                recibo.getEstado(),
                recibo.getFechaPublicacion(),
                obtenerComentarioDisconformidad(recibo.getId()),
                recibo.getNotificadoEn()
        );
    }

    private String obtenerComentarioDisconformidad(Long reciboId) {
        return repositorioEventoFirma.findByReciboIdOrderByTimestampEventoAsc(reciboId).stream()
                .filter(e -> e.getTipo() == TipoEventoFirmaRecibo.DISCONFORMIDAD)
                .map(EventoFirmaRecibo::getComentario)
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    private void validarPuedeEliminar(Recibo recibo) {
        if (recibo.getEstado() == EstadoRecibo.FIRMADO || recibo.getEstado() == EstadoRecibo.DISCONFORMIDAD) {
            throw new ExcepcionNegocio("No se puede eliminar un recibo firmado o en disconformidad", 400);
        }
        if (tieneEventoFirmaDefinitivo(recibo.getId())) {
            throw new ExcepcionNegocio("No se puede eliminar un recibo con eventos de firma registrados", 400);
        }
    }

    private Long idArchivo(Recibo recibo) {
        return recibo.getArchivo() != null ? recibo.getArchivo().getId() : null;
    }

    private void verificarIntegridadPdf(Recibo recibo) {
        if (recibo.getArchivo() == null) {
            throw new ExcepcionNegocio("El recibo no tiene archivo verificable. Contactá a RRHH.", 400);
        }
        String hashActual = servicioAlmacenamiento.calcularHashArchivo(recibo.getArchivo().getId());
        if (!hashActual.equals(recibo.getHashPdf())) {
            throw new ExcepcionNegocio("El archivo fue modificado después de su publicación", 409);
        }
    }

    private void validarTieneHashParaFirma(Recibo recibo) {
        if (recibo.isRequiereRepublicacion() || recibo.getHashPdf() == null || recibo.getHashPdf().isBlank()) {
            throw new ExcepcionNegocio(
                    "Este recibo no tiene hash de integridad. RRHH debe volver a publicarlo con archivo PDF.",
                    400
            );
        }
    }

    private void registrarEvento(Recibo recibo, TipoEventoFirmaRecibo tipo, String comentario,
                                 HttpServletRequest peticion) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        String userAgent = UtilidadClienteHttp.obtenerUserAgent(peticion);

        EventoFirmaRecibo evento = new EventoFirmaRecibo();
        evento.setReciboId(recibo.getId());
        evento.setEmpleadoId(recibo.getEmpleado().getId());
        evento.setEmpresaId(recibo.getEmpresa().getId());
        evento.setUsuarioId(usuarioId);
        evento.setTipo(tipo);
        evento.setTimestampEvento(LocalDateTime.now());
        evento.setDireccionIp(UtilidadClienteHttp.obtenerDireccionIp(peticion));
        evento.setUserAgent(userAgent);
        evento.setSistemaOperativo(UtilidadClienteHttp.detectarSistemaOperativo(userAgent));
        evento.setHashPdfEnFirma(recibo.getHashPdf());
        evento.setComentario(comentario);
        repositorioEventoFirma.save(evento);
    }

    private Recibo obtenerReciboEmpresa(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Recibo no encontrado", 404));
    }

    private Recibo obtenerReciboPropio(Long id) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        Recibo recibo = obtenerReciboEmpresa(id);
        if (!recibo.getEmpleado().getId().equals(empleado.getId())) {
            throw new ExcepcionNegocio("No puede acceder a recibos de otro empleado", 403);
        }
        return recibo;
    }

    private void validarAccesoRecibo(Recibo recibo) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            return;
        }
        try {
            Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
            if (!recibo.getEmpleado().getId().equals(empleado.getId())) {
                throw new ExcepcionNegocio("No puede acceder a recibos de otro empleado", 403);
            }
        } catch (ExcepcionNegocio ex) {
            if (ex.getCodigoHttp() == 403) {
                throw ex;
            }
        }
    }

    private boolean puedeVerHashPdf() {
        return true;
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaRecibo mapear(Recibo recibo, boolean incluirHash) {
        Empleado empleado = recibo.getEmpleado();
        String urlDescarga = recibo.getArchivo() != null
                ? "/api/recibos/" + recibo.getId() + "/pdf"
                : null;
        Long reciboAnteriorId = recibo.getReciboAnterior() != null ? recibo.getReciboAnterior().getId() : null;
        String comentario = recibo.getEstado() == EstadoRecibo.DISCONFORMIDAD
                ? obtenerComentarioDisconformidad(recibo.getId())
                : null;

        return new RespuestaRecibo(
                recibo.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                recibo.getPeriodo(),
                recibo.getTitulo(),
                urlDescarga,
                recibo.getEstado(),
                recibo.getFechaPublicacion(),
                recibo.getVistoPorEmpleadoEn(),
                recibo.getFechaFirma(),
                recibo.getMontoNeto(),
                recibo.getMontoBruto(),
                incluirHash ? recibo.getHashPdf() : null,
                recibo.isRequiereRepublicacion(),
                recibo.getVersion(),
                reciboAnteriorId,
                recibo.isVigente(),
                recibo.getNotificadoEn(),
                recibo.getCanalNotificacion(),
                comentario
        );
    }

    private RespuestaEventoFirmaRecibo mapearEvento(EventoFirmaRecibo evento) {
        return new RespuestaEventoFirmaRecibo(
                evento.getId(),
                evento.getReciboId(),
                evento.getTipo(),
                evento.getTimestampEvento(),
                evento.getDireccionIp(),
                evento.getUserAgent(),
                evento.getSistemaOperativo(),
                evento.getHashPdfEnFirma(),
                evento.getComentario(),
                evento.getEmpleadoId(),
                evento.getUsuarioId()
        );
    }
}
