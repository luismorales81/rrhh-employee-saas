package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.activo.dto.SolicitudAsignarActivo;
import com.rrhh.saas.activo.dto.SolicitudCrearActivo;
import com.rrhh.saas.activo.servicio.ServicioActivo;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dto.SolicitudPublicarComunicacion;
import com.rrhh.saas.comunicacion.servicio.ServicioComunicacion;
import com.rrhh.saas.fichaje.dominio.AlcanceZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.SolicitudCrearZonaFichaje;
import com.rrhh.saas.fichaje.repositorio.RepositorioZonaFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioConfiguracionFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioPoliticaFichaje;
import com.rrhh.saas.fichaje.servicio.ServicioZonaFichaje;
import com.rrhh.saas.documento.dto.SolicitudAsignarDocumento;
import com.rrhh.saas.documento.dto.SolicitudCrearDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioCategoriaDocumento;
import com.rrhh.saas.documento.servicio.ServicioAsignacionDocumento;
import com.rrhh.saas.documento.servicio.ServicioDocumento;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dto.RespuestaEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudActualizarEmpleado;
import com.rrhh.saas.empleado.dto.SolicitudCrearEmpleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioLegajo;
import com.rrhh.saas.nucleo.config.PropiedadesDemo;
import com.rrhh.saas.onboarding.dto.SolicitudCrearPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.repositorio.RepositorioPlantillaTareaOnboarding;
import com.rrhh.saas.onboarding.servicio.ServicioOnboarding;
import com.rrhh.saas.offboarding.dto.SolicitudCrearPlantillaTareaOffboarding;
import com.rrhh.saas.offboarding.servicio.ServicioOffboarding;
import com.rrhh.saas.organizacion.dto.SolicitudCrearArea;
import com.rrhh.saas.organizacion.dto.SolicitudCrearGerencia;
import com.rrhh.saas.organizacion.dto.SolicitudCrearPuesto;
import com.rrhh.saas.organizacion.servicio.ServicioOrganizacion;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dto.RespuestaEmpresa;
import com.rrhh.saas.nucleo.dto.RespuestaUsuario;
import com.rrhh.saas.nucleo.dto.SolicitudCrearEmpresa;
import com.rrhh.saas.nucleo.dto.SolicitudCrearUsuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.dto.SolicitudCargarRecibo;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import com.rrhh.saas.recibo.servicio.ServicioRecibo;
import com.rrhh.saas.solicitud.dominio.TipoPaso;
import com.rrhh.saas.solicitud.dto.RespuestaFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudCrearFlujoTrabajo;
import com.rrhh.saas.solicitud.dto.SolicitudCrearTipoSolicitud;
import com.rrhh.saas.solicitud.repositorio.RepositorioTipoSolicitud;
import com.rrhh.saas.solicitud.servicio.ServicioFlujoTrabajo;
import com.rrhh.saas.solicitud.servicio.ServicioTipoSolicitud;
import com.rrhh.saas.vacacion.dominio.DiaSemana;
import com.rrhh.saas.vacacion.dto.RespuestaPoliticaVacaciones;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPatronDescanso;
import com.rrhh.saas.vacacion.dto.SolicitudCrearPoliticaVacaciones;
import com.rrhh.saas.vacacion.repositorio.RepositorioPoliticaVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioConfiguracionVacaciones;
import com.rrhh.saas.vacacion.servicio.ServicioPatronDescanso;
import com.rrhh.saas.vacacion.servicio.ServicioPoliticaVacaciones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
public class ServicioCargaDatosDemo {

    private static final Logger log = LoggerFactory.getLogger(ServicioCargaDatosDemo.class);
    private static final String NOMBRE_EMPRESA_DEMO = "Demo SA";
    private static final String EMAIL_RRHH_DEMO = "rrhh@demo.local";
    private static final String EMAIL_EMPLEADO_DEMO = "empleado@demo.local";
    private static final String NUMERO_JUAN = "EMP-001";

    private static final String CLAVE_DEMO = "Demo123!";

    private final PropiedadesDemo propiedadesDemo;
    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioPoliticaVacaciones repositorioPoliticaVacaciones;
    private final RepositorioTipoSolicitud repositorioTipoSolicitud;
    private final RepositorioCategoriaDocumento repositorioCategoriaDocumento;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioLegajo servicioLegajo;
    private final ServicioPoliticaVacaciones servicioPoliticaVacaciones;
    private final ServicioFlujoTrabajo servicioFlujoTrabajo;
    private final ServicioTipoSolicitud servicioTipoSolicitud;
    private final ServicioDocumento servicioDocumento;
    private final ServicioAsignacionDocumento servicioAsignacionDocumento;
    private final ServicioComunicacion servicioComunicacion;
    private final ServicioRecibo servicioRecibo;
    private final RepositorioRecibo repositorioRecibo;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioOnboarding servicioOnboarding;
    private final ServicioOffboarding servicioOffboarding;
    private final RepositorioPlantillaTareaOnboarding repositorioPlantillaOnboarding;
    private final RepositorioZonaFichaje repositorioZonaFichaje;
    private final ServicioPoliticaFichaje servicioPoliticaFichaje;
    private final ServicioZonaFichaje servicioZonaFichaje;
    private final ServicioConfiguracionFichaje servicioConfiguracionFichaje;
    private final ServicioPatronDescanso servicioPatronDescanso;
    private final ServicioConfiguracionVacaciones servicioConfiguracionVacaciones;
    private final ServicioOrganizacion servicioOrganizacion;
    private final ServicioActivo servicioActivo;

    public ServicioCargaDatosDemo(PropiedadesDemo propiedadesDemo,
                                  RepositorioEmpresa repositorioEmpresa,
                                  RepositorioUsuario repositorioUsuario,
                                  RepositorioRol repositorioRol,
                                  RepositorioEmpleado repositorioEmpleado,
                                  RepositorioPoliticaVacaciones repositorioPoliticaVacaciones,
                                  RepositorioTipoSolicitud repositorioTipoSolicitud,
                                  RepositorioCategoriaDocumento repositorioCategoriaDocumento,
                                  ServicioEmpresa servicioEmpresa,
                                  ServicioUsuario servicioUsuario,
                                  ServicioEmpleado servicioEmpleado,
                                  ServicioLegajo servicioLegajo,
                                  ServicioPoliticaVacaciones servicioPoliticaVacaciones,
                                  ServicioFlujoTrabajo servicioFlujoTrabajo,
                                  ServicioTipoSolicitud servicioTipoSolicitud,
                                  ServicioDocumento servicioDocumento,
                                  ServicioAsignacionDocumento servicioAsignacionDocumento,
                                  ServicioComunicacion servicioComunicacion,
                                  ServicioRecibo servicioRecibo,
                                  RepositorioRecibo repositorioRecibo,
                                  ServicioAlmacenamiento servicioAlmacenamiento,
                                  ServicioOnboarding servicioOnboarding,
                                  ServicioOffboarding servicioOffboarding,
                                  RepositorioPlantillaTareaOnboarding repositorioPlantillaOnboarding,
                                  RepositorioZonaFichaje repositorioZonaFichaje,
                                  ServicioPoliticaFichaje servicioPoliticaFichaje,
                                  ServicioZonaFichaje servicioZonaFichaje,
                                  ServicioConfiguracionFichaje servicioConfiguracionFichaje,
                                  ServicioPatronDescanso servicioPatronDescanso,
                                  ServicioConfiguracionVacaciones servicioConfiguracionVacaciones,
                                  ServicioOrganizacion servicioOrganizacion,
                                  ServicioActivo servicioActivo) {
        this.propiedadesDemo = propiedadesDemo;
        this.repositorioEmpresa = repositorioEmpresa;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioPoliticaVacaciones = repositorioPoliticaVacaciones;
        this.repositorioTipoSolicitud = repositorioTipoSolicitud;
        this.repositorioCategoriaDocumento = repositorioCategoriaDocumento;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioLegajo = servicioLegajo;
        this.servicioPoliticaVacaciones = servicioPoliticaVacaciones;
        this.servicioFlujoTrabajo = servicioFlujoTrabajo;
        this.servicioTipoSolicitud = servicioTipoSolicitud;
        this.servicioDocumento = servicioDocumento;
        this.servicioAsignacionDocumento = servicioAsignacionDocumento;
        this.servicioComunicacion = servicioComunicacion;
        this.servicioRecibo = servicioRecibo;
        this.repositorioRecibo = repositorioRecibo;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioOnboarding = servicioOnboarding;
        this.servicioOffboarding = servicioOffboarding;
        this.repositorioPlantillaOnboarding = repositorioPlantillaOnboarding;
        this.repositorioZonaFichaje = repositorioZonaFichaje;
        this.servicioPoliticaFichaje = servicioPoliticaFichaje;
        this.servicioZonaFichaje = servicioZonaFichaje;
        this.servicioConfiguracionFichaje = servicioConfiguracionFichaje;
        this.servicioPatronDescanso = servicioPatronDescanso;
        this.servicioConfiguracionVacaciones = servicioConfiguracionVacaciones;
        this.servicioOrganizacion = servicioOrganizacion;
        this.servicioActivo = servicioActivo;
    }

    @Transactional
    public void cargarSiCorresponde() {
        if (!propiedadesDemo.isCargarDatos()) {
            return;
        }
        if (repositorioUsuario.findByEmailAndDeletedFalse(EMAIL_EMPLEADO_DEMO).isPresent()) {
            log.debug("Usuario demo {} ya existe, se omite la carga", EMAIL_EMPLEADO_DEMO);
            return;
        }

        log.info("Cargando datos de demostración para {}", NOMBRE_EMPRESA_DEMO);

        RespuestaEmpresa empresa = obtenerOCrearEmpresaDemo();

        ejecutarConTenant(empresa.id(), null, () -> {
            Rol rolRrhh = obtenerRol(empresa.id(), "RRHH");
            Rol rolEmpleado = obtenerRol(empresa.id(), "Empleado");

            RespuestaUsuario usuarioRrhh = obtenerOCrearUsuario(
                    empresa.id(), EMAIL_RRHH_DEMO, rolRrhh.getId());
            RespuestaUsuario usuarioEmpleado = obtenerOCrearUsuario(
                    empresa.id(), EMAIL_EMPLEADO_DEMO, rolEmpleado.getId());

            RespuestaEmpleado juan = obtenerOCrearJuan(empresa.id(), usuarioEmpleado.id());

            sembrarFichajeDemo(empresa.id(), juan.id());
            habilitarFichajeMovilDemo(empresa.id());

            if (repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresa.id()).stream()
                    .noneMatch(e -> "EMP-002".equals(e.getNumeroEmpleado()))) {
                servicioEmpleado.crear(new SolicitudCrearEmpleado(
                        "María",
                        "García",
                        "EMP-002",
                        "28987654",
                        "27-28987654-3",
                        LocalDate.of(1988, 11, 3),
                        LocalDate.now().minusMonths(6),
                        "011-5555-0002",
                        "maria.garcia@mail.demo",
                        null,
                        null, null, null, null, null,
                        null
                ));
            }

            if (repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresa.id()).stream()
                    .noneMatch(e -> "EMP-003".equals(e.getNumeroEmpleado()))) {
                servicioEmpleado.crear(new SolicitudCrearEmpleado(
                        "Pedro",
                        "López",
                        "EMP-003",
                        "30111222",
                        "20-30111222-4",
                        LocalDate.of(1992, 4, 12),
                        LocalDate.now().minusYears(2),
                        "011-5555-0003",
                        "pedro.lopez@mail.demo",
                        null,
                        null, null, null, null, null,
                        null
                ));
            }

            if (repositorioPoliticaVacaciones.findByEmpresaIdAndActivaTrueAndDeletedFalse(empresa.id()).isEmpty()) {
                RespuestaPoliticaVacaciones politica = servicioPoliticaVacaciones.crear(
                        new SolicitudCrearPoliticaVacaciones(
                                "Política LCT Argentina",
                                "Rangos según Art. 150 LCT (días corridos por antigüedad al 31/12)",
                                List.of(
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(0, 5, 14),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(5, 10, 21),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(10, 20, 28),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(20, null, 35)
                                )
                        ));
                servicioPoliticaVacaciones.activar(politica.id());
            }

            servicioConfiguracionVacaciones.obtener();
            sembrarPatronesDescansoDemo(empresa.id());

            if (repositorioTipoSolicitud.findByEmpresaIdAndNombreAndDeletedFalse(empresa.id(), "Vacaciones").isEmpty()) {
                RespuestaFlujoTrabajo flujo = servicioFlujoTrabajo.crear(new SolicitudCrearFlujoTrabajo(
                        "Aprobación Vacaciones",
                        "Un paso de aprobación por RRHH",
                        List.of(new SolicitudCrearFlujoTrabajo.SolicitudPasoFlujo(
                                1, "Aprobación RRHH", TipoPaso.RRHH, null, null
                        ))
                ));

                servicioTipoSolicitud.crear(new SolicitudCrearTipoSolicitud(
                        "Vacaciones",
                        "Solicitud de días de vacaciones",
                        flujo.id(),
                        true,
                        false
                ));
                servicioTipoSolicitud.crear(new SolicitudCrearTipoSolicitud(
                        "Licencia",
                        "Solicitud de licencia general",
                        flujo.id(),
                        true,
                        false
                ));
            }

            Long categoriaContratoId = repositorioCategoriaDocumento
                    .findByEmpresaIdAndNombreAndDeletedFalse(empresa.id(), "Contrato")
                    .orElseThrow()
                    .getId();

            var documento = servicioDocumento.crear(new SolicitudCrearDocumento(
                    categoriaContratoId,
                    "Contrato de trabajo - Juan Pérez",
                    "Contrato laboral de ejemplo para la demostración",
                    "1.0",
                    propiedadesDemo.getUrlPdfContrato(),
                    null,
                    true
            ));

            servicioAsignacionDocumento.asignar(documento.id(), new SolicitudAsignarDocumento(
                    List.of(juan.id()),
                    LocalDate.now().plusMonths(1)
            ));

            servicioComunicacion.publicar(new SolicitudPublicarComunicacion(
                    "Bienvenida a la plataforma RRHH",
                    "Te damos la bienvenida a Demo SA. Revisá tus documentos y recibos desde el portal del empleado.",
                    TipoDestinatarioComunicacion.EMPRESA,
                    null,
                    false,
                    null
            ));

            YearMonth mesAnterior = YearMonth.now().minusMonths(1);
            ejecutarConTenant(empresa.id(), usuarioRrhh.id(), () -> {
                var archivoRecibo = servicioAlmacenamiento.guardarDesdeBase64(
                        "recibo-demo-" + mesAnterior + ".pdf",
                        "application/pdf",
                        leerPdfReciboDemo()
                );
                servicioRecibo.cargar(new SolicitudCargarRecibo(
                        juan.id(),
                        mesAnterior.toString(),
                        "Recibo de sueldo " + mesAnterior.getMonthValue() + "/" + mesAnterior.getYear(),
                        archivoRecibo.id(),
                        new BigDecimal("450000.00"),
                        new BigDecimal("600000.00")
                ));
            });

            crearPlantillasOnboardingDemo(empresa.id());
            crearPlantillasOffboardingDemo(empresa.id());

            ejecutarConTenant(empresa.id(), usuarioRrhh.id(), () ->
                    servicioLegajo.iniciarOnboarding(juan.id())
            );
        });

        log.info("Datos demo cargados: usuarios {} y {}", EMAIL_RRHH_DEMO, EMAIL_EMPLEADO_DEMO);
    }

    /**
     * Completa datos LCT de vacaciones en entornos demo ya inicializados (post-migración V26+).
     */
    @Transactional
    public void completarVacacionesLctDemoSiCorresponde() {
        if (!propiedadesDemo.isCargarDatos()) {
            return;
        }

        var empresaOpt = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> NOMBRE_EMPRESA_DEMO.equals(e.getNombre()))
                .findFirst();
        if (empresaOpt.isEmpty()) {
            return;
        }

        Long empresaId = empresaOpt.get().getId();
        ejecutarConTenant(empresaId, null, () -> {
            if (repositorioPoliticaVacaciones.findByEmpresaIdAndActivaTrueAndDeletedFalse(empresaId).isEmpty()) {
                RespuestaPoliticaVacaciones politica = servicioPoliticaVacaciones.crear(
                        new SolicitudCrearPoliticaVacaciones(
                                "Política LCT Argentina",
                                "Rangos según Art. 150 LCT (días corridos por antigüedad al 31/12)",
                                List.of(
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(0, 5, 14),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(5, 10, 21),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(10, 20, 28),
                                        new SolicitudCrearPoliticaVacaciones.SolicitudRangoPolitica(20, null, 35)
                                )
                        ));
                servicioPoliticaVacaciones.activar(politica.id());
                log.info("Política LCT demo creada para empresa {}", empresaId);
            }

            servicioConfiguracionVacaciones.obtener();

            if (repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                    .noneMatch(e -> "EMP-002".equals(e.getNumeroEmpleado()))) {
                servicioEmpleado.crear(new SolicitudCrearEmpleado(
                        "María", "García", "EMP-002", "28987654", "27-28987654-3",
                        LocalDate.of(1988, 11, 3), LocalDate.now().minusMonths(6),
                        "011-5555-0002", "maria.garcia@mail.demo",
                        null, null, null, null, null, null, null
                ));
            }

            if (repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                    .noneMatch(e -> "EMP-003".equals(e.getNumeroEmpleado()))) {
                servicioEmpleado.crear(new SolicitudCrearEmpleado(
                        "Pedro", "López", "EMP-003", "30111222", "27-30111222-4",
                        LocalDate.of(1992, 4, 15), LocalDate.now().minusYears(1),
                        "011-5555-0003", "pedro.lopez@mail.demo",
                        null, null, null, null, null, null, null
                ));
            }

            sembrarPatronesDescansoDemo(empresaId);
            sembrarFichajeDemo(empresaId, null);
            habilitarFichajeMovilDemo(empresaId);
        });
    }

    /**
     * Habilita fichaje móvil para empleados demo en entornos ya inicializados.
     */
    @Transactional
    public void completarFichajeDemoSiCorresponde() {
        if (!propiedadesDemo.isCargarDatos()) {
            return;
        }

        var empresaOpt = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> NOMBRE_EMPRESA_DEMO.equals(e.getNombre()))
                .findFirst();
        if (empresaOpt.isEmpty()) {
            return;
        }

        Long empresaId = empresaOpt.get().getId();
        ejecutarConTenant(empresaId, null, () -> {
            sembrarFichajeDemo(empresaId, null);
            habilitarFichajeMovilDemo(empresaId);
        });
    }

    /**
     * Completa estructura organizacional, plantillas de onboarding/offboarding y activos
     * en entornos demo ya inicializados sin carga completa.
     */
    @Transactional
    public void completarOrganizacionActivosYChecklistsDemoSiCorresponde() {
        if (!propiedadesDemo.isCargarDatos()) {
            return;
        }

        var empresaOpt = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> NOMBRE_EMPRESA_DEMO.equals(e.getNombre()))
                .findFirst();
        if (empresaOpt.isEmpty()) {
            return;
        }

        Long empresaId = empresaOpt.get().getId();
        sembrarOrganizacionDemo(empresaId);
        crearPlantillasOnboardingDemo(empresaId);
        crearPlantillasOffboardingDemo(empresaId);
        sembrarActivosDemo(empresaId);
    }

    /**
     * Repara recibos demo sin hash (legacy) que impiden la firma en entornos ya inicializados.
     */
    @Transactional
    public void repararReciboDemoSiCorresponde() {
        if (!propiedadesDemo.isCargarDatos()) {
            return;
        }

        var empresaOpt = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> NOMBRE_EMPRESA_DEMO.equals(e.getNombre()))
                .findFirst();
        if (empresaOpt.isEmpty()) {
            return;
        }

        Long empresaId = empresaOpt.get().getId();
        var juanOpt = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .filter(e -> NUMERO_JUAN.equals(e.getNumeroEmpleado()))
                .findFirst();
        if (juanOpt.isEmpty()) {
            return;
        }

        Long juanId = juanOpt.get().getId();
        List<Recibo> recibosVigentes = repositorioRecibo
                .findByEmpleadoIdAndVigenteTrueAndDeletedFalseOrderByFechaPublicacionDesc(juanId);

        boolean tieneReciboValido = recibosVigentes.stream()
                .anyMatch(this::reciboTieneIntegridadVerificable);
        if (tieneReciboValido) {
            return;
        }

        for (Recibo legacy : recibosVigentes) {
            if (!reciboTieneIntegridadVerificable(legacy)
                    && (legacy.getEstado() == EstadoRecibo.PENDIENTE || legacy.getEstado() == EstadoRecibo.VISTO)) {
                legacy.setVigente(false);
                legacy.setDeleted(true);
                legacy.setDeletedAt(LocalDateTime.now());
                repositorioRecibo.save(legacy);
                log.info("Recibo demo legacy {} (período {}) archivado por falta de hash", legacy.getId(), legacy.getPeriodo());
            }
        }

        var usuarioRrhhOpt = repositorioUsuario.findByEmailAndDeletedFalse(EMAIL_RRHH_DEMO);
        if (usuarioRrhhOpt.isEmpty()) {
            return;
        }

        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        ejecutarConTenant(empresaId, usuarioRrhhOpt.get().getId(), () -> {
            if (repositorioRecibo.existsByEmpleadoIdAndPeriodoAndVigenteTrueAndDeletedFalse(juanId, mesAnterior.toString())) {
                return;
            }
            var archivoRecibo = servicioAlmacenamiento.guardarDesdeBase64(
                    "recibo-demo-" + mesAnterior + ".pdf",
                    "application/pdf",
                    leerPdfReciboDemo()
            );
            servicioRecibo.cargar(new SolicitudCargarRecibo(
                    juanId,
                    mesAnterior.toString(),
                    "Recibo de sueldo " + mesAnterior.getMonthValue() + "/" + mesAnterior.getYear(),
                    archivoRecibo.id(),
                    new BigDecimal("450000.00"),
                    new BigDecimal("600000.00")
            ));
            log.info("Recibo demo reparado para Juan, período {}", mesAnterior);
        });
    }

    private boolean reciboTieneIntegridadVerificable(Recibo recibo) {
        return !recibo.isRequiereRepublicacion()
                && recibo.getHashPdf() != null
                && !recibo.getHashPdf().isBlank();
    }

    private RespuestaEmpresa obtenerOCrearEmpresaDemo() {
        return repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> NOMBRE_EMPRESA_DEMO.equals(e.getNombre()))
                .findFirst()
                .map(e -> servicioEmpresa.obtenerPorId(e.getId()))
                .orElseGet(() -> servicioEmpresa.crear(new SolicitudCrearEmpresa(
                        NOMBRE_EMPRESA_DEMO,
                        "Demo Sociedad Anónima",
                        "30-71234567-8",
                        "contacto@demo.local",
                        "011-4000-0000"
                )));
    }

    private RespuestaUsuario obtenerOCrearUsuario(Long empresaId, String email, Long rolId) {
        return repositorioUsuario.findByEmailAndDeletedFalse(email)
                .map(u -> new RespuestaUsuario(
                        u.getId(),
                        empresaId,
                        u.getEmail(),
                        u.isActivo(),
                        u.getRoles().stream().map(r -> r.getNombre()).toList()))
                .orElseGet(() -> servicioUsuario.crear(new SolicitudCrearUsuario(
                        empresaId,
                        email,
                        CLAVE_DEMO,
                        Set.of(rolId)
                )));
    }

    private RespuestaEmpleado obtenerOCrearJuan(Long empresaId, Long usuarioEmpleadoId) {
        return repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .filter(e -> NUMERO_JUAN.equals(e.getNumeroEmpleado()))
                .findFirst()
                .map(e -> servicioEmpleado.obtenerPorId(e.getId()))
                .orElseGet(() -> servicioEmpleado.crear(new SolicitudCrearEmpleado(
                        "Juan",
                        "Pérez",
                        NUMERO_JUAN,
                        "30123456",
                        "20-30123456-7",
                        LocalDate.of(1990, 5, 15),
                        LocalDate.now().minusYears(1),
                        "011-5555-0001",
                        "juan.perez@mail.demo",
                        EMAIL_EMPLEADO_DEMO,
                        null, null, null, null, null,
                        usuarioEmpleadoId
                )));
    }

    private Rol obtenerRol(Long empresaId, String nombre) {
        return repositorioRol.findByEmpresaIdAndNombreAndDeletedFalse(empresaId, nombre)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + nombre));
    }

    private void crearPlantillasOnboardingDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            if (!repositorioPlantillaOnboarding.findByEmpresaIdAndDeletedFalseOrderByOrdenAsc(empresaId).isEmpty()) {
                return;
            }
            servicioOnboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOnboarding(
                    "Completar perfil", 1, true, "PERFIL"));
            servicioOnboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOnboarding(
                    "Firmar contrato", 2, true, "FIRMAR_CONTRATO"));
            servicioOnboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOnboarding(
                    "Leer comunicación de bienvenida", 3, true, "LEER_COMUNICACION"));
            servicioOnboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOnboarding(
                    "Recibir notebook", 4, true, "RECIBIR_NOTEBOOK"));
        });
    }

    private void crearPlantillasOffboardingDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            if (!servicioOffboarding.listarPlantillas().isEmpty()) {
                return;
            }
            servicioOffboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOffboarding(
                    "Devolver activos asignados", 1, true, ServicioOffboarding.TIPO_DEVOLVER_ACTIVO));
            servicioOffboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOffboarding(
                    "Entrevista de salida con RRHH", 2, true, "MANUAL"));
            servicioOffboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOffboarding(
                    "Baja de accesos y credenciales", 3, true, "MANUAL"));
            servicioOffboarding.crearPlantilla(new SolicitudCrearPlantillaTareaOffboarding(
                    "Liquidación final y documentación", 4, false, "MANUAL"));
        });
    }

    private void sembrarOrganizacionDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            if (!servicioOrganizacion.listarGerencias().isEmpty()) {
                return;
            }

            var gerenciaAdministracion = servicioOrganizacion.crearGerencia(
                    new SolicitudCrearGerencia("Administración", "Gestión corporativa y soporte interno"));
            var gerenciaOperaciones = servicioOrganizacion.crearGerencia(
                    new SolicitudCrearGerencia("Operaciones", "Procesos operativos y logística"));
            var gerenciaTecnologia = servicioOrganizacion.crearGerencia(
                    new SolicitudCrearGerencia("Tecnología", "Sistemas, infraestructura y desarrollo"));

            var areaRrhh = servicioOrganizacion.crearArea(new SolicitudCrearArea(
                    "Recursos Humanos", "Gestión de personas y cultura", gerenciaAdministracion.id()));
            servicioOrganizacion.crearArea(new SolicitudCrearArea(
                    "Finanzas", "Contabilidad, tesorería y control", gerenciaAdministracion.id()));
            servicioOrganizacion.crearArea(new SolicitudCrearArea(
                    "Logística", "Distribución y almacén", gerenciaOperaciones.id()));
            var areaDesarrollo = servicioOrganizacion.crearArea(new SolicitudCrearArea(
                    "Desarrollo", "Ingeniería de software", gerenciaTecnologia.id()));

            var puestoAnalistaRrhh = servicioOrganizacion.crearPuesto(new SolicitudCrearPuesto(
                    "Analista de RRHH", "Semi-senior", "Soporte al área de personas"));
            servicioOrganizacion.crearPuesto(new SolicitudCrearPuesto(
                    "Gerente de Área", "Senior", "Liderazgo de equipo"));
            var puestoDesarrollador = servicioOrganizacion.crearPuesto(new SolicitudCrearPuesto(
                    "Desarrollador Backend", "Semi-senior", "Desarrollo Java/Spring"));
            var puestoAuxiliar = servicioOrganizacion.crearPuesto(new SolicitudCrearPuesto(
                    "Auxiliar Administrativo", "Junior", "Tareas administrativas generales"));

            vincularEmpleadoOrganizacionSiCorresponde(empresaId, "EMP-001",
                    gerenciaOperaciones.id(), null, puestoAuxiliar.id());
            vincularEmpleadoOrganizacionSiCorresponde(empresaId, "EMP-002",
                    gerenciaAdministracion.id(), areaRrhh.id(), puestoAnalistaRrhh.id());
            vincularEmpleadoOrganizacionSiCorresponde(empresaId, "EMP-003",
                    gerenciaTecnologia.id(), areaDesarrollo.id(), puestoDesarrollador.id());

            log.info("Estructura organizacional demo creada para empresa {}", empresaId);
        });
    }

    private void vincularEmpleadoOrganizacionSiCorresponde(Long empresaId, String numeroEmpleado,
                                                           Long gerenciaId, Long areaId, Long puestoId) {
        repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .filter(e -> numeroEmpleado.equals(e.getNumeroEmpleado()))
                .findFirst()
                .ifPresent(empleado -> {
                    if (empleado.getGerencia() != null) {
                        return;
                    }
                    servicioEmpleado.actualizar(empleado.getId(), solicitudActualizarDesdeEmpleado(
                            empleado, gerenciaId, areaId, puestoId));
                });
    }

    private SolicitudActualizarEmpleado solicitudActualizarDesdeEmpleado(Empleado empleado,
                                                                         Long gerenciaId,
                                                                         Long areaId,
                                                                         Long puestoId) {
        Long supervisorId = empleado.getSupervisor() != null ? empleado.getSupervisor().getId() : null;
        Long sectorId = empleado.getSector() != null ? empleado.getSector().getId() : null;
        Long usuarioId = empleado.getUsuario() != null ? empleado.getUsuario().getId() : null;
        return new SolicitudActualizarEmpleado(
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getNumeroEmpleado(),
                empleado.getDni(),
                empleado.getCuil(),
                empleado.getFechaNacimiento(),
                empleado.getFechaIngreso(),
                empleado.getTelefono(),
                empleado.getEmailPersonal(),
                empleado.getEmailLaboral(),
                supervisorId,
                gerenciaId,
                areaId,
                sectorId,
                puestoId,
                usuarioId
        );
    }

    private void sembrarActivosDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            if (!servicioActivo.listar().isEmpty()) {
                return;
            }

            var notebook = servicioActivo.crear(new SolicitudCrearActivo(
                    "Notebook Dell Latitude 5420", "NOTEBOOK", "DEMO-NB-001"));
            servicioActivo.crear(new SolicitudCrearActivo(
                    "Celular Samsung Galaxy A54", "CELULAR", "DEMO-CEL-002"));
            servicioActivo.crear(new SolicitudCrearActivo(
                    "Monitor LG 27 pulgadas", "MONITOR", "DEMO-MON-003"));
            servicioActivo.crear(new SolicitudCrearActivo(
                    "Silla ergonómica", "MOBILIARIO", "DEMO-SIL-004"));

            repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                    .filter(e -> NUMERO_JUAN.equals(e.getNumeroEmpleado()))
                    .findFirst()
                    .ifPresent(juan -> servicioActivo.asignar(notebook.id(), new SolicitudAsignarActivo(
                            juan.getId(),
                            LocalDate.now().minusMonths(11),
                            "Entrega kit de onboarding")));

            log.info("Activos demo creados para empresa {}", empresaId);
        });
    }

    private void sembrarPatronesDescansoDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            var empleados = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId);
            for (var empleado : empleados) {
                if (!servicioPatronDescanso.listarPorEmpleado(empleado.getId()).isEmpty()) {
                    continue;
                }
                List<DiaSemana> dias = switch (empleado.getNumeroEmpleado()) {
                    case "EMP-002" -> List.of(DiaSemana.MIERCOLES, DiaSemana.JUEVES);
                    case "EMP-003" -> List.of(DiaSemana.MARTES, DiaSemana.VIERNES);
                    default -> List.of(DiaSemana.SABADO, DiaSemana.DOMINGO);
                };
                servicioPatronDescanso.crear(empleado.getId(), new SolicitudCrearPatronDescanso(
                        dias,
                        LocalDate.of(2020, 1, 1),
                        null
                ));
            }
            log.info("Patrones de descanso demo creados para empresa {}", empresaId);
        });
    }

    private void sembrarFichajeDemo(Long empresaId, Long empleadoJuanId) {
        ejecutarConTenant(empresaId, null, () -> {
            if (!repositorioZonaFichaje.findByEmpresaIdAndDeletedFalseOrderByNombreAsc(empresaId).isEmpty()) {
                return;
            }
            servicioPoliticaFichaje.crearPoliticaParaEmpresa(servicioEmpresa.obtenerEntidad(empresaId));
            servicioZonaFichaje.crear(new SolicitudCrearZonaFichaje(
                    "Casa Central Demo",
                    "Zona de demostración (microcentro CABA)",
                    List.of(
                            List.of(-34.6025, -58.3830),
                            List.of(-34.6025, -58.3800),
                            List.of(-34.6050, -58.3800),
                            List.of(-34.6050, -58.3830)
                    ),
                    AlcanceZonaFichaje.GLOBAL,
                    true
            ));
            log.info("Datos demo de fichaje creados para empresa {}", empresaId);
        });
    }

    private void habilitarFichajeMovilDemo(Long empresaId) {
        ejecutarConTenant(empresaId, null, () -> {
            for (var empleado : repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId)) {
                servicioConfiguracionFichaje.actualizarConfiguracion(
                        empleado.getId(),
                        new SolicitudConfiguracionFichajeEmpleado(true, false)
                );
            }
            log.info("Fichaje móvil habilitado para empleados demo de empresa {}", empresaId);
        });
    }

    private void ejecutarConTenant(Long empresaId, Long usuarioId, Runnable accion) {
        Long empresaAnterior = ContextoTenant.obtenerEmpresaId();
        Long usuarioAnterior = ContextoTenant.obtenerUsuarioId();
        ContextoTenant.establecerEmpresaId(empresaId);
        if (usuarioId != null) {
            ContextoTenant.establecerUsuarioId(usuarioId);
        }
        try {
            accion.run();
        } finally {
            ContextoTenant.establecerEmpresaId(empresaAnterior);
            ContextoTenant.establecerUsuarioId(usuarioAnterior);
        }
    }

    private byte[] leerPdfReciboDemo() {
        try (InputStream entrada = getClass().getResourceAsStream("/demo/recibo-ejemplo.pdf")) {
            if (entrada == null) {
                return "%PDF-1.4\n% Demo recibo\n".getBytes();
            }
            return entrada.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el PDF de demo", e);
        }
    }
}
