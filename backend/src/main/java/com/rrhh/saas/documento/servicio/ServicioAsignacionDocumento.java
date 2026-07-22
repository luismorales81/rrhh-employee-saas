package com.rrhh.saas.documento.servicio;

import com.rrhh.saas.archivo.config.PropiedadesAlmacenamiento;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.archivo.servicio.ValidadorArchivo;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.documento.dominio.AsignacionDocumento;
import com.rrhh.saas.documento.dominio.Documento;
import com.rrhh.saas.documento.dominio.EstadoAsignacionDocumento;
import com.rrhh.saas.documento.dominio.FirmaDocumento;
import com.rrhh.saas.documento.dto.RespuestaAsignacionDocumento;
import com.rrhh.saas.documento.dto.RespuestaEvidenciaFirma;
import com.rrhh.saas.documento.dto.SolicitudAsignarDocumento;
import com.rrhh.saas.documento.dto.SolicitudFirmarDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioAsignacionDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioFirmaDocumento;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class ServicioAsignacionDocumento {

    private final RepositorioAsignacionDocumento repositorio;
    private final RepositorioFirmaDocumento repositorioFirma;
    private final ServicioDocumento servicioDocumento;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ApplicationEventPublisher publicadorEventos;
    private final PropiedadesAlmacenamiento propiedadesAlmacenamiento;

    public ServicioAsignacionDocumento(RepositorioAsignacionDocumento repositorio,
                                       RepositorioFirmaDocumento repositorioFirma,
                                       ServicioDocumento servicioDocumento,
                                       ServicioEmpleado servicioEmpleado,
                                       ServicioEmpresa servicioEmpresa,
                                       ServicioAuditoria servicioAuditoria,
                                       ServicioAlmacenamiento servicioAlmacenamiento,
                                       ApplicationEventPublisher publicadorEventos,
                                       PropiedadesAlmacenamiento propiedadesAlmacenamiento) {
        this.repositorio = repositorio;
        this.repositorioFirma = repositorioFirma;
        this.servicioDocumento = servicioDocumento;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.publicadorEventos = publicadorEventos;
        this.propiedadesAlmacenamiento = propiedadesAlmacenamiento;
    }

    @Transactional
    public List<RespuestaAsignacionDocumento> asignar(Long documentoId, SolicitudAsignarDocumento solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        Documento documento = servicioDocumento.obtenerEntidad(documentoId, empresa.getId());
        if (!documento.isActivo()) {
            throw new ExcepcionNegocio("El documento no está activo");
        }

        List<RespuestaAsignacionDocumento> resultados = new ArrayList<>();
        for (Long empleadoId : solicitud.empleadoIds()) {
            Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresa.getId());
            if (repositorio.existsByEmpleadoIdAndDocumentoIdAndDeletedFalse(empleadoId, documentoId)) {
                throw new ExcepcionNegocio(
                        "El documento ya está asignado al empleado " + empleado.getNombre() + " " + empleado.getApellido());
            }

            AsignacionDocumento asignacion = new AsignacionDocumento();
            asignacion.setEmpresa(empresa);
            asignacion.setEmpleado(empleado);
            asignacion.setDocumento(documento);
            asignacion.setEstado(EstadoAsignacionDocumento.PENDIENTE);
            asignacion.setFechaAsignacion(LocalDateTime.now());
            asignacion.setFechaVencimiento(solicitud.fechaVencimiento());

            AsignacionDocumento guardada = repositorio.save(asignacion);
            servicioAuditoria.registrar("ASIGNAR", "AsignacionDocumento", guardada.getId(), null,
                    documento.getTitulo() + " -> " + empleado.getApellido());
            publicadorEventos.publishEvent(new EventoDocumentoAsignado(
                    guardada.getId(), documentoId, empleadoId, empresa.getId()));
            resultados.add(mapear(guardada, false));
        }
        return resultados;
    }

    @Transactional
    public RespuestaAsignacionDocumento confirmarLectura(Long asignacionId) {
        Empresa empresa = obtenerEmpresaActual();
        AsignacionDocumento asignacion = obtenerEntidad(asignacionId, empresa.getId());
        validarPropietario(asignacion);

        if (asignacion.getEstado() == EstadoAsignacionDocumento.FIRMADO) {
            return mapear(asignacion, true);
        }
        if (asignacion.getEstado() == EstadoAsignacionDocumento.RECHAZADO) {
            throw new ExcepcionNegocio("No se puede marcar como leído un documento rechazado");
        }

        asignacion.setEstado(EstadoAsignacionDocumento.LEIDO);
        asignacion.setFechaLectura(LocalDateTime.now());
        AsignacionDocumento actualizada = repositorio.save(asignacion);
        servicioAuditoria.registrar("LEER", "AsignacionDocumento", actualizada.getId(), null, null);
        publicadorEventos.publishEvent(new EventoDocumentoLeido(
                actualizada.getId(), asignacion.getEmpleado().getId(), empresa.getId()));
        return mapear(actualizada, repositorioFirma.findByAsignacionIdAndDeletedFalse(asignacionId).isPresent());
    }

    @Transactional
    public RespuestaAsignacionDocumento firmar(Long asignacionId, SolicitudFirmarDocumento solicitud,
                                               HttpServletRequest peticion) {
        Empresa empresa = obtenerEmpresaActual();
        AsignacionDocumento asignacion = obtenerEntidad(asignacionId, empresa.getId());
        validarPropietario(asignacion);
        Documento documento = asignacion.getDocumento();

        if (!documento.isRequiereFirma()) {
            throw new ExcepcionNegocio("Este documento no requiere firma");
        }
        if (asignacion.getEstado() == EstadoAsignacionDocumento.FIRMADO) {
            throw new ExcepcionNegocio("El documento ya fue firmado");
        }
        if (asignacion.getEstado() == EstadoAsignacionDocumento.RECHAZADO) {
            throw new ExcepcionNegocio("No se puede firmar un documento rechazado");
        }
        if (repositorioFirma.findByAsignacionIdAndDeletedFalse(asignacionId).isPresent()) {
            throw new ExcepcionNegocio("El documento ya fue firmado");
        }

        LocalDateTime ahora = LocalDateTime.now();
        String hashFirma = generarHashFirma(asignacionId, asignacion.getEmpleado().getId(), ahora);

        String urlArchivoFirmado = solicitud.urlArchivoFirmado();
        if (solicitud.imagenFirmaBase64() != null && !solicitud.imagenFirmaBase64().isBlank()) {
            byte[] imagen = decodificarImagenBase64(solicitud.imagenFirmaBase64());
            var archivo = servicioAlmacenamiento.guardarDesdeBase64(
                    "firma-asignacion-" + asignacionId + ".png", "image/png", imagen);
            urlArchivoFirmado = archivo.urlContenido();
            hashFirma = hashFirma + ":" + archivo.hashSha256();
        }

        FirmaDocumento firma = new FirmaDocumento();
        firma.setAsignacion(asignacion);
        firma.setEmpleado(asignacion.getEmpleado());
        firma.setFechaFirma(ahora);
        firma.setHashFirma(hashFirma);
        firma.setUrlArchivoFirmado(urlArchivoFirmado);
        firma.setDireccionIp(obtenerIpCliente(peticion));
        firma.setDispositivo(solicitud.dispositivo() != null ? solicitud.dispositivo() : peticion.getHeader("User-Agent"));
        repositorioFirma.save(firma);

        asignacion.setEstado(EstadoAsignacionDocumento.FIRMADO);
        if (asignacion.getFechaLectura() == null) {
            asignacion.setFechaLectura(ahora);
        }
        AsignacionDocumento actualizada = repositorio.save(asignacion);
        servicioAuditoria.registrar("FIRMAR", "AsignacionDocumento", actualizada.getId(), null, hashFirma);
        publicadorEventos.publishEvent(new EventoDocumentoFirmado(
                actualizada.getId(), documento.getId(), asignacion.getEmpleado().getId(), empresa.getId()));
        return mapear(actualizada, true);
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionDocumento> listarPorEmpresa() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorio.findByEmpresaIdAndDeletedFalseOrderByFechaAsignacionDesc(empresaId).stream()
                .map(a -> mapear(a, repositorioFirma.findByAsignacionIdAndDeletedFalse(a.getId()).isPresent()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionDocumento> listarPorEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        return repositorio.findByEmpleadoIdAndDeletedFalseOrderByFechaAsignacionDesc(empleadoId).stream()
                .map(a -> mapear(a, repositorioFirma.findByAsignacionIdAndDeletedFalse(a.getId()).isPresent()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionDocumento> listarMisAsignaciones() {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no definido en contexto", 403);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        return listarPorEmpleado(empleado.getId());
    }

    @Transactional(readOnly = true)
    public RespuestaAsignacionDocumento obtenerPorId(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        AsignacionDocumento asignacion = obtenerEntidad(id, empresaId);
        return mapear(asignacion, repositorioFirma.findByAsignacionIdAndDeletedFalse(id).isPresent());
    }

    private AsignacionDocumento obtenerEntidad(Long id, Long empresaId) {
        return repositorio.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Asignación de documento no encontrada", 404));
    }

    private void validarPropietario(AsignacionDocumento asignacion) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no definido en contexto", 403);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        if (!asignacion.getEmpleado().getId().equals(empleado.getId())) {
            throw new ExcepcionNegocio("Solo el empleado asignado puede realizar esta acción", 403);
        }
    }

    @Transactional(readOnly = true)
    public RespuestaEvidenciaFirma obtenerEvidenciaFirma(Long asignacionId) {
        Empresa empresa = obtenerEmpresaActual();
        AsignacionDocumento asignacion = obtenerEntidad(asignacionId, empresa.getId());
        if (!tienePermiso("documento.asignacion.ver")) {
            validarPropietario(asignacion);
        }
        FirmaDocumento firma = repositorioFirma.findByAsignacionIdAndDeletedFalse(asignacionId)
                .orElseThrow(() -> new ExcepcionNegocio("No hay evidencia de firma para esta asignación", 404));
        return new RespuestaEvidenciaFirma(
                asignacionId,
                firma.getFechaFirma(),
                firma.getHashFirma(),
                firma.getUrlArchivoFirmado(),
                firma.getDireccionIp(),
                firma.getDispositivo()
        );
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

    private byte[] decodificarImagenBase64(String imagenBase64) {
        ValidadorArchivo.validarLongitudBase64(imagenBase64, propiedadesAlmacenamiento.getTamanoMaximoBytes());
        String datos = imagenBase64;
        if (datos.contains(",")) {
            datos = datos.substring(datos.indexOf(',') + 1);
        }
        return Base64.getDecoder().decode(datos);
    }

    private String generarHashFirma(Long asignacionId, Long empleadoId, LocalDateTime fecha) {
        String contenido = asignacionId + "|" + empleadoId + "|" + fecha.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ExcepcionNegocio("Error al generar hash de firma");
        }
    }

    private String obtenerIpCliente(HttpServletRequest peticion) {
        if (peticion == null) {
            return null;
        }
        String encabezado = peticion.getHeader("X-Forwarded-For");
        if (encabezado != null && !encabezado.isBlank()) {
            return encabezado.split(",")[0].trim();
        }
        return peticion.getRemoteAddr();
    }

    private Empresa obtenerEmpresaActual() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        if (empresaId == null) {
            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);
        }
        return servicioEmpresa.obtenerEntidad(empresaId);
    }

    private RespuestaAsignacionDocumento mapear(AsignacionDocumento asignacion, boolean firmado) {
        Empleado empleado = asignacion.getEmpleado();
        Documento documento = asignacion.getDocumento();
        return new RespuestaAsignacionDocumento(
                asignacion.getId(),
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                documento.getId(),
                documento.getTitulo(),
                documento.getUrlArchivo(),
                documento.isRequiereFirma(),
                asignacion.getEstado(),
                asignacion.getFechaAsignacion(),
                asignacion.getFechaLectura(),
                asignacion.getFechaVencimiento(),
                firmado
        );
    }

    public record EventoDocumentoAsignado(Long asignacionId, Long documentoId, Long empleadoId, Long empresaId) {
    }

    public record EventoDocumentoLeido(Long asignacionId, Long empleadoId, Long empresaId) {
    }

    public record EventoDocumentoFirmado(Long asignacionId, Long documentoId, Long empleadoId, Long empresaId) {
    }
}
