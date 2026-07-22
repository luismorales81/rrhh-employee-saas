package com.rrhh.saas.certificado.servicio;



import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;

import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;

import com.rrhh.saas.certificado.dominio.Certificado;

import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;

import com.rrhh.saas.certificado.dominio.OrigenCargaCertificado;

import com.rrhh.saas.certificado.dto.CertificadoCompletoDTO;

import com.rrhh.saas.certificado.dto.CertificadoResumenDTO;

import com.rrhh.saas.certificado.dto.RespuestaReporteAusentismo;

import com.rrhh.saas.certificado.dto.ResumenAusentismo;

import com.rrhh.saas.certificado.dto.SolicitudCargarCertificado;

import com.rrhh.saas.certificado.dto.SolicitudRechazarCertificado;

import com.rrhh.saas.certificado.dto.SolicitudValidarCertificado;

import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;

import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;

import com.rrhh.saas.certificado.repositorio.RepositorioDatosExtraidosCertificado;

import com.rrhh.saas.certificado.validacion.CalculadorAusentismoEmpleado;

import com.rrhh.saas.certificado.validacion.EvaluadorAlertaCertificado;

import com.rrhh.saas.certificado.validacion.ResultadoAlertaCertificado;

import com.rrhh.saas.certificado.validacion.ValidadorTransicionEstadoCertificado;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;

import com.rrhh.saas.comun.tenant.ContextoTenant;

import com.rrhh.saas.empleado.dominio.Empleado;

import com.rrhh.saas.empleado.servicio.ServicioEmpleado;

import com.rrhh.saas.nucleo.dominio.Empresa;

import com.rrhh.saas.nucleo.seguridad.UsuarioAutenticado;

import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.Set;



@Service

public class ServicioCertificado {



    private static final Long ACTOR_SERVICIO_IA = 0L;



    private static final Set<EstadoCertificado> ESTADOS_REPROCESABLES = Set.of(

            EstadoCertificado.EN_REVISION,

            EstadoCertificado.REQUIERE_ACLARACION,

            EstadoCertificado.EN_PROCESAMIENTO,

            EstadoCertificado.EXTRAIDO

    );



    private final RepositorioCertificado repositorio;

    private final RepositorioDatosExtraidosCertificado repositorioDatos;

    private final ServicioEmpleado servicioEmpleado;

    private final ServicioEmpresa servicioEmpresa;

    private final ServicioAlmacenamiento servicioAlmacenamiento;

    private final ServicioEventoCertificado servicioEvento;

    private final ServicioConfiguracionAuditoriaMedica servicioConfiguracion;

    private final ServicioProcesamientoCertificado servicioProcesamiento;



    public ServicioCertificado(RepositorioCertificado repositorio,

                               RepositorioDatosExtraidosCertificado repositorioDatos,

                               ServicioEmpleado servicioEmpleado,

                               ServicioEmpresa servicioEmpresa,

                               ServicioAlmacenamiento servicioAlmacenamiento,

                               ServicioEventoCertificado servicioEvento,

                               ServicioConfiguracionAuditoriaMedica servicioConfiguracion,

                               @Lazy ServicioProcesamientoCertificado servicioProcesamiento) {

        this.repositorio = repositorio;

        this.repositorioDatos = repositorioDatos;

        this.servicioEmpleado = servicioEmpleado;

        this.servicioEmpresa = servicioEmpresa;

        this.servicioAlmacenamiento = servicioAlmacenamiento;

        this.servicioEvento = servicioEvento;

        this.servicioConfiguracion = servicioConfiguracion;

        this.servicioProcesamiento = servicioProcesamiento;

    }



    @Transactional

    public Object cargar(SolicitudCargarCertificado solicitud, HttpServletRequest peticion) {

        Empresa empresa = obtenerEmpresaActual();

        Long usuarioId = ContextoTenant.obtenerUsuarioId();



        Long empleadoId = solicitud.empleadoId();

        if (empleadoId == null) {

            empleadoId = servicioEmpleado.obtenerPorUsuarioId(usuarioId).getId();

        }

        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresa.getId());



        if (solicitud.origenCarga() == OrigenCargaCertificado.EMPLEADO) {

            Empleado propio = servicioEmpleado.obtenerPorUsuarioId(usuarioId);

            if (!propio.getId().equals(empleado.getId()) && !tieneAutoridad("certificado.ver_completo")) {

                throw new ExcepcionNegocio("Solo puede cargar certificados propios", 403);

            }

        }



        ArchivoAlmacenado archivo = servicioAlmacenamiento.obtenerEntidad(solicitud.archivoId());

        String archivoUrl = servicioAlmacenamiento.construirUrlContenido(archivo.getId());



        Certificado certificado = new Certificado();

        certificado.setEmpleado(empleado);

        certificado.setEmpresa(empresa);

        certificado.setTipoExamen(solicitud.tipoExamen());

        certificado.setArchivoId(archivo.getId());

        certificado.setArchivoUrl(archivoUrl);

        certificado.setArchivoHashSha256(archivo.getHashSha256());

        certificado.setOrigenCarga(solicitud.origenCarga());

        certificado.setCargadoPorUsuarioId(usuarioId);

        certificado.setEstado(EstadoCertificado.CARGADO);

        certificado.setCreatedBy(usuarioId);



        Certificado guardado = repositorio.save(certificado);

        servicioEvento.registrar(guardado, null, EstadoCertificado.CARGADO, usuarioId,

                "Carga de certificado", peticion);



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(empresa.getId());

        if (config.tieneIaConfigurada()) {

            transicionar(guardado, EstadoCertificado.EN_PROCESAMIENTO, usuarioId,

                    "Inicio de procesamiento IA con Vertex", peticion);

            guardado.setUltimoIntentoExtraccion(LocalDateTime.now());

            repositorio.save(guardado);

            servicioProcesamiento.procesarExtraccionAsync(guardado.getId(), empresa.getId());

        } else {

            transicionar(guardado, EstadoCertificado.EN_REVISION, usuarioId,

                    "IA no configurada — revisión manual", peticion);

            repositorio.save(guardado);

        }



        return mapearSegunRol(guardado);

    }



    @Transactional

    public Object reprocesar(Long id, HttpServletRequest peticion) {

        Certificado certificado = obtenerEntidad(id);

        Long usuarioId = ContextoTenant.obtenerUsuarioId();



        if (!ESTADOS_REPROCESABLES.contains(certificado.getEstado())) {

            throw new ExcepcionNegocio("No se puede reprocesar un certificado en estado "

                    + certificado.getEstado());

        }



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                certificado.getEmpresa().getId());

        if (!config.tieneIaConfigurada()) {

            throw new ExcepcionNegocio("La extracción IA no está configurada para esta empresa");

        }



        certificado.setIntentosExtraccion(0);

        certificado.setUltimoErrorExtraccion(null);

        certificado.setUltimoIntentoExtraccion(LocalDateTime.now());



        if (certificado.getEstado() != EstadoCertificado.EN_PROCESAMIENTO) {

            transicionar(certificado, EstadoCertificado.EN_PROCESAMIENTO, usuarioId,

                    "Reprocesamiento manual solicitado", peticion);

        } else {

            servicioEvento.registrar(certificado, certificado.getEstado(), certificado.getEstado(),

                    usuarioId, "Reprocesamiento manual solicitado (ya en procesamiento)", peticion);

        }



        repositorio.save(certificado);

        servicioProcesamiento.procesarExtraccionAsync(certificado.getId(), certificado.getEmpresa().getId());

        enriquecerConDatosVigentes(certificado);

        return mapearCompleto(certificado, ResultadoAlertaCertificado.sinAlertas());

    }



    @Transactional

    public void marcarIntentoExtraccion(Long certificadoId) {

        Certificado certificado = repositorio.findById(certificadoId)

                .orElseThrow(() -> new ExcepcionNegocio("Certificado no encontrado", 404));

        certificado.setUltimoIntentoExtraccion(LocalDateTime.now());

        repositorio.save(certificado);

    }



    @Transactional

    public void aplicarResultadoExtraccion(Long certificadoId, ResultadoExtraccionGemini resultado,

                                           HttpServletRequest peticion) {

        Certificado certificado = repositorio.findById(certificadoId)

                .orElseThrow(() -> new ExcepcionNegocio("Certificado no encontrado", 404));



        if (certificado.isDeleted()) {

            throw new ExcepcionNegocio("Certificado no encontrado", 404);

        }

        if (certificado.getEstado() != EstadoCertificado.EN_PROCESAMIENTO) {

            return;

        }



        if (resultado != null && resultado.tieneErrorDocumento()) {

            transicionar(certificado, EstadoCertificado.REQUIERE_ACLARACION, ACTOR_SERVICIO_IA,

                    "Documento no reconocido por IA", peticion);

            certificado.setUltimoErrorExtraccion(truncarError(resultado.error()));

            repositorio.save(certificado);

            return;

        }



        DatosExtraidosCertificado datos = crearNuevaVersionExtraccion(certificado, resultado);

        certificado.setDatosExtraidos(datos);



        transicionar(certificado, EstadoCertificado.EXTRAIDO, ACTOR_SERVICIO_IA,

                "Extracción IA completada", peticion);



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                certificado.getEmpresa().getId());

        List<Certificado> historial = obtenerHistorialPeriodo(certificado, config);

        ResultadoAlertaCertificado alerta = EvaluadorAlertaCertificado.evaluar(datos, config, historial);



        if (alerta.requiereAclaracion()) {

            transicionar(certificado, EstadoCertificado.REQUIERE_ACLARACION, ACTOR_SERVICIO_IA,

                    String.join("; ", alerta.motivos()), peticion);

        } else {

            transicionar(certificado, EstadoCertificado.EN_REVISION, ACTOR_SERVICIO_IA,

                    alerta.disparaAlerta()

                            ? "Enviado a revisión con alertas: " + String.join("; ", alerta.motivos())

                            : "Enviado a revisión estándar",

                    peticion);

        }



        certificado.setUltimoErrorExtraccion(null);

        repositorio.save(certificado);

    }



    @Transactional

    public void registrarFalloExtraccion(Long certificadoId, String error, HttpServletRequest peticion) {

        Certificado certificado = repositorio.findById(certificadoId)

                .orElseThrow(() -> new ExcepcionNegocio("Certificado no encontrado", 404));



        if (certificado.getEstado() != EstadoCertificado.EN_PROCESAMIENTO) {

            return;

        }



        certificado.setIntentosExtraccion(certificado.getIntentosExtraccion() + 1);

        certificado.setUltimoErrorExtraccion(truncarError(error));

        certificado.setUltimoIntentoExtraccion(LocalDateTime.now());



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                certificado.getEmpresa().getId());

        if (certificado.getIntentosExtraccion() >= config.getMaxIntentosExtraccion()) {

            transicionar(certificado, EstadoCertificado.REQUIERE_ACLARACION, ACTOR_SERVICIO_IA,

                    "Agotados intentos de extracción: " + truncarError(error), peticion);

        }



        repositorio.save(certificado);

    }



    @Transactional

    public void finalizarExtraccionAgotada(Long certificadoId) {

        Certificado certificado = repositorio.findById(certificadoId).orElse(null);

        if (certificado == null || certificado.isDeleted()

                || certificado.getEstado() != EstadoCertificado.EN_PROCESAMIENTO) {

            return;

        }



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                certificado.getEmpresa().getId());

        if (certificado.getIntentosExtraccion() < config.getMaxIntentosExtraccion()) {

            return;

        }



        String mensaje = certificado.getUltimoErrorExtraccion() != null

                ? certificado.getUltimoErrorExtraccion()

                : "Agotados intentos de extracción";

        transicionar(certificado, EstadoCertificado.REQUIERE_ACLARACION, ACTOR_SERVICIO_IA, mensaje, null);

        repositorio.save(certificado);

    }



    @Transactional

    public CertificadoCompletoDTO validar(Long id, SolicitudValidarCertificado solicitud,

                                          HttpServletRequest peticion) {

        Certificado certificado = obtenerEntidad(id);

        Long usuarioId = ContextoTenant.obtenerUsuarioId();



        DatosExtraidosCertificado datos = repositorioDatos

                .findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(id)

                .orElseThrow(() -> new ExcepcionNegocio("No hay datos extraídos para validar", 400));



        if (Boolean.TRUE.equals(solicitud.corregidoManualmente())) {

            if (solicitud.diagnostico() != null) {

                datos.setDiagnostico(solicitud.diagnostico());

            }

            if (solicitud.reposoDias() != null) {

                datos.setReposoDias(solicitud.reposoDias());

            }

            if (solicitud.medicoNombre() != null) {

                datos.setMedicoNombre(solicitud.medicoNombre());

            }

            if (solicitud.medicoMatricula() != null) {

                datos.setMedicoMatricula(solicitud.medicoMatricula());

            }

            if (solicitud.fechaEmision() != null) {

                datos.setFechaEmision(solicitud.fechaEmision());

            }

            datos.setCorregidoManualmente(true);

            repositorioDatos.save(datos);

            certificado.setDatosExtraidos(datos);

        }



        if (certificado.getEstado() == EstadoCertificado.REQUIERE_ACLARACION) {

            transicionar(certificado, EstadoCertificado.EN_REVISION, usuarioId,

                    "Aclaración resuelta por auditor", peticion);

        }



        transicionar(certificado, EstadoCertificado.VALIDADO, usuarioId,

                solicitud.observaciones() != null ? solicitud.observaciones() : "Validado por auditor médico",

                peticion);

        repositorio.save(certificado);

        return mapearCompleto(certificado, ResultadoAlertaCertificado.sinAlertas());

    }



    @Transactional

    public CertificadoCompletoDTO rechazar(Long id, SolicitudRechazarCertificado solicitud,

                                           HttpServletRequest peticion) {

        Certificado certificado = obtenerEntidad(id);

        Long usuarioId = ContextoTenant.obtenerUsuarioId();

        transicionar(certificado, EstadoCertificado.RECHAZADO, usuarioId,

                solicitud.observaciones(), peticion);

        repositorio.save(certificado);

        return mapearCompleto(certificado, ResultadoAlertaCertificado.sinAlertas());

    }



    @Transactional(readOnly = true)

    public Object obtenerPorId(Long id) {

        Certificado certificado = obtenerEntidad(id);

        validarAccesoLectura(certificado);

        return mapearSegunRol(certificado);

    }



    @Transactional(readOnly = true)

    public List<?> listarPorEmpleado(Long empleadoId) {

        Empresa empresa = obtenerEmpresaActual();

        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresa.getId());



        if (!puedeVerHistorialEmpleado(empleadoId)) {

            throw new ExcepcionNegocio("No autorizado a ver el historial de certificados", 403);

        }



        List<Certificado> certificados = repositorio

                .findByEmpleadoIdAndEmpresaIdAndDeletedFalseOrderByCreatedAtDescConDatos(

                        empleadoId, empresa.getId());

        enriquecerConDatosVigentes(certificados);

        return certificados.stream()

                .map(this::mapearSegunRol)

                .toList();

    }



    @Transactional(readOnly = true)

    public List<?> listarBandejaRevision(boolean incluirRequiereAclaracion) {

        if (!tieneAutoridad("certificado.ver_completo")

                && !tieneAutoridad("certificado.ver_resumen")

                && !tieneAutoridad("certificado.validar")) {

            throw new ExcepcionNegocio("No autorizado a ver la bandeja de certificados", 403);

        }



        Empresa empresa = obtenerEmpresaActual();

        List<EstadoCertificado> estados = new ArrayList<>();

        estados.add(EstadoCertificado.EN_REVISION);

        if (incluirRequiereAclaracion) {

            estados.add(EstadoCertificado.REQUIERE_ACLARACION);

        }



        List<Certificado> certificados = repositorio.buscarBandejaPorEstados(empresa.getId(), estados);

        enriquecerConDatosVigentes(certificados);



        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(empresa.getId());

        certificados.sort((a, b) -> {

            boolean priorA = evaluarAlerta(a, config).revisionPrioritaria();

            boolean priorB = evaluarAlerta(b, config).revisionPrioritaria();

            if (priorA != priorB) {

                return priorB ? 1 : -1;

            }

            return b.getCreatedAt().compareTo(a.getCreatedAt());

        });



        return certificados.stream()

                .map(this::mapearSegunRol)

                .toList();

    }



    @Transactional(readOnly = true)

    public RespuestaReporteAusentismo reporteAusentismo(LocalDate desde, LocalDate hasta) {

        if (desde == null || hasta == null || hasta.isBefore(desde)) {

            throw new ExcepcionNegocio("Rango de fechas inválido");

        }

        Empresa empresa = obtenerEmpresaActual();

        List<Certificado> validados = repositorio.buscarPorEmpresaYEstadoConDatos(

                empresa.getId(), EstadoCertificado.VALIDADO);

        enriquecerConDatosVigentes(validados);



        Map<Long, List<Certificado>> porEmpleado = new LinkedHashMap<>();

        for (Certificado c : validados) {

            porEmpleado.computeIfAbsent(c.getEmpleado().getId(), k -> new ArrayList<>()).add(c);

        }



        List<ResumenAusentismo> resumenes = new ArrayList<>();

        int diasTotales = 0;

        for (Map.Entry<Long, List<Certificado>> entrada : porEmpleado.entrySet()) {

            ResumenAusentismo resumen = CalculadorAusentismoEmpleado.calcular(

                    entrada.getKey(), desde, hasta, entrada.getValue());

            if (resumen.totalCertificadosValidados() > 0) {

                resumenes.add(resumen);

                diasTotales += resumen.diasReposoTotales();

            }

        }



        return new RespuestaReporteAusentismo(

                empresa.getId(), desde, hasta, resumenes.size(), diasTotales, resumenes);

    }



    private DatosExtraidosCertificado crearNuevaVersionExtraccion(Certificado certificado,

                                                                  ResultadoExtraccionGemini resultado) {

        repositorioDatos.findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(certificado.getId())

                .ifPresent(anterior -> {

                    anterior.setEsVigente(false);

                    repositorioDatos.save(anterior);

                });



        int siguienteVersion = repositorioDatos.findByCertificadoIdOrderByNumeroVersionDesc(certificado.getId())

                .stream()

                .findFirst()

                .map(d -> d.getNumeroVersion() + 1)

                .orElse(1);



        DatosExtraidosCertificado datos = new DatosExtraidosCertificado();

        datos.setCertificado(certificado);

        datos.setNumeroVersion(siguienteVersion);

        datos.setEsVigente(true);

        datos.setDiagnostico(resultado != null ? resultado.diagnostico() : null);

        datos.setReposoDias(resultado != null ? resultado.reposoDias() : null);

        datos.setMedicoNombre(resultado != null ? resultado.medicoNombre() : null);

        datos.setMedicoMatricula(resultado != null ? resultado.medicoMatricula() : null);

        datos.setFechaEmision(resultado != null ? resultado.fechaEmision() : null);

        datos.setConfianzaExtraccion(resultado != null ? resultado.confianzaExtraccion() : null);

        datos.setCorregidoManualmente(false);

        return repositorioDatos.save(datos);

    }



    private void enriquecerConDatosVigentes(Certificado certificado) {

        repositorioDatos.findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(certificado.getId())

                .ifPresent(certificado::setDatosExtraidos);

    }



    private void enriquecerConDatosVigentes(List<Certificado> certificados) {

        for (Certificado certificado : certificados) {

            enriquecerConDatosVigentes(certificado);

        }

    }



    private void transicionar(Certificado certificado, EstadoCertificado nuevo,

                              Long actorUsuarioId, String observaciones, HttpServletRequest peticion) {

        EstadoCertificado actual = certificado.getEstado();

        if (!ValidadorTransicionEstadoCertificado.validarTransicionEstado(actual, nuevo)) {

            throw new ExcepcionNegocio("Transición no permitida: " + actual + " → " + nuevo);

        }

        certificado.setEstado(nuevo);

        servicioEvento.registrar(certificado, actual, nuevo, actorUsuarioId, observaciones, peticion);

    }



    private List<Certificado> obtenerHistorialPeriodo(Certificado actual,

                                                      ConfiguracionAuditoriaMedica config) {

        List<Certificado> historial = repositorio.buscarHistorialPeriodo(

                        actual.getEmpleado().getId(),

                        actual.getEmpresa().getId(),

                        LocalDateTime.now().minusDays(config.getPeriodoEvaluacionDias()),

                        List.of(EstadoCertificado.VALIDADO, EstadoCertificado.EN_REVISION,

                                EstadoCertificado.EXTRAIDO, EstadoCertificado.REQUIERE_ACLARACION))

                .stream()

                .filter(c -> !Objects.equals(c.getId(), actual.getId()))

                .toList();

        enriquecerConDatosVigentes(historial);

        return historial;

    }



    private Certificado obtenerEntidad(Long id) {

        Long empresaId = ContextoTenant.obtenerEmpresaId();

        Certificado certificado = repositorio.findByIdAndEmpresaIdAndDeletedFalseConDatos(id, empresaId)

                .orElseThrow(() -> new ExcepcionNegocio("Certificado no encontrado", 404));

        enriquecerConDatosVigentes(certificado);

        return certificado;

    }



    private void validarAccesoLectura(Certificado certificado) {

        if (tieneAutoridad("certificado.ver_completo") || tieneAutoridad("certificado.ver_resumen")) {

            return;

        }

        if (tieneAutoridad("certificado.ver_propio")) {

            Empleado propio = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());

            if (propio.getId().equals(certificado.getEmpleado().getId())) {

                return;

            }

        }

        throw new ExcepcionNegocio("No autorizado a ver este certificado", 403);

    }



    private boolean puedeVerHistorialEmpleado(Long empleadoId) {

        if (tieneAutoridad("certificado.ver_completo") || tieneAutoridad("certificado.ver_resumen")) {

            return true;

        }

        if (tieneAutoridad("certificado.ver_propio")) {

            Empleado propio = servicioEmpleado.obtenerPorUsuarioId(ContextoTenant.obtenerUsuarioId());

            return propio.getId().equals(empleadoId);

        }

        return false;

    }



    private Object mapearSegunRol(Certificado certificado) {

        ResultadoAlertaCertificado alerta = evaluarAlerta(certificado);



        boolean propio = esCertificadoPropio(certificado);

        if (tieneAutoridad("certificado.ver_completo")

                || tieneAutoridad("certificado.validar")

                || (tieneAutoridad("certificado.ver_propio") && propio)) {

            return mapearCompleto(certificado, alerta);

        }

        if (tieneAutoridad("certificado.ver_resumen")) {

            return mapearResumen(certificado, alerta);

        }

        if (propio && tieneAutoridad("certificado.ver_propio")) {

            return mapearCompleto(certificado, alerta);

        }

        throw new ExcepcionNegocio("No autorizado a ver este certificado", 403);

    }



    private ResultadoAlertaCertificado evaluarAlerta(Certificado certificado) {

        if (certificado.getDatosExtraidos() == null) {

            return ResultadoAlertaCertificado.sinAlertas();

        }

        ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                certificado.getEmpresa().getId());

        return EvaluadorAlertaCertificado.evaluar(

                certificado.getDatosExtraidos(), config,

                obtenerHistorialPeriodo(certificado, config));

    }



    private ResultadoAlertaCertificado evaluarAlerta(Certificado certificado,

                                                     ConfiguracionAuditoriaMedica config) {

        if (certificado.getDatosExtraidos() == null) {

            return ResultadoAlertaCertificado.sinAlertas();

        }

        return EvaluadorAlertaCertificado.evaluar(

                certificado.getDatosExtraidos(), config,

                obtenerHistorialPeriodo(certificado, config));

    }



    private boolean esCertificadoPropio(Certificado certificado) {

        Long usuarioId = ContextoTenant.obtenerUsuarioId();

        if (usuarioId == null) {

            return false;

        }

        try {

            Empleado propio = servicioEmpleado.obtenerPorUsuarioId(usuarioId);

            return propio.getId().equals(certificado.getEmpleado().getId());

        } catch (ExcepcionNegocio e) {

            return false;

        }

    }



    private CertificadoCompletoDTO mapearCompleto(Certificado c, ResultadoAlertaCertificado alerta) {

        DatosExtraidosCertificado d = c.getDatosExtraidos();

        return new CertificadoCompletoDTO(

                c.getId(),

                c.getEmpleado().getId(),

                c.getTipoExamen(),

                c.getEstado(),

                c.getOrigenCarga(),

                c.getArchivoUrl(),

                c.getArchivoHashSha256(),

                d != null ? d.getDiagnostico() : null,

                d != null ? d.getReposoDias() : null,

                d != null ? d.getMedicoNombre() : null,

                d != null ? d.getMedicoMatricula() : null,

                d != null ? d.getFechaEmision() : null,

                d != null ? d.getConfianzaExtraccion() : null,

                d != null && d.isCorregidoManualmente(),

                c.getCreatedAt(),

                alerta.revisionPrioritaria(),

                alerta.motivos()

        );

    }



    private CertificadoResumenDTO mapearResumen(Certificado c, ResultadoAlertaCertificado alerta) {

        DatosExtraidosCertificado d = c.getDatosExtraidos();

        return new CertificadoResumenDTO(

                c.getId(),

                c.getEmpleado().getId(),

                c.getTipoExamen(),

                c.getEstado(),

                d != null ? d.getReposoDias() : null,

                d != null ? d.getFechaEmision() : null,

                c.getOrigenCarga(),

                c.getArchivoUrl(),

                c.getCreatedAt(),

                alerta.revisionPrioritaria(),

                alerta.motivos()

        );

    }



    private Empresa obtenerEmpresaActual() {

        Long empresaId = ContextoTenant.obtenerEmpresaId();

        if (empresaId == null) {

            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);

        }

        return servicioEmpresa.obtenerEntidad(empresaId);

    }



    private boolean tieneAutoridad(String codigo) {

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null) {

            return false;

        }

        if (autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario && usuario.isEsSuperAdmin()) {

            return true;

        }

        return autenticacion.getAuthorities().stream()

                .map(GrantedAuthority::getAuthority)

                .anyMatch(codigo::equals);

    }



    private static String truncarError(String error) {

        if (error == null) {

            return null;

        }

        return error.length() > 500 ? error.substring(0, 500) : error;

    }

}


