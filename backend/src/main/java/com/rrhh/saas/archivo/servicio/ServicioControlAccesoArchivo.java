package com.rrhh.saas.archivo.servicio;

import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.documento.repositorio.RepositorioAsignacionDocumento;
import com.rrhh.saas.nucleo.seguridad.UsuarioAutenticado;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Controla la descarga HTTP de archivos: RRHH/roles privilegiados ven el tenant;
 * empleados solo acceden a archivos propios (recibo, certificado, documento o subida propia).
 */
@Service
public class ServicioControlAccesoArchivo {

    private static final Set<String> PERMISOS_DESCARGA_AMPLIA = Set.of(
            "recibo.ver",
            "recibo.cargar",
            "documento.ver",
            "documento.crear",
            "documento.asignar",
            "certificado.ver_completo",
            "certificado.validar",
            "certificado.rechazar",
            "empleado.crear",
            "empleado.editar"
    );

    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final RepositorioRecibo repositorioRecibo;
    private final RepositorioCertificado repositorioCertificado;
    private final RepositorioAsignacionDocumento repositorioAsignacionDocumento;

    public ServicioControlAccesoArchivo(ServicioAlmacenamiento servicioAlmacenamiento,
                                        RepositorioRecibo repositorioRecibo,
                                        RepositorioCertificado repositorioCertificado,
                                        RepositorioAsignacionDocumento repositorioAsignacionDocumento) {
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.repositorioRecibo = repositorioRecibo;
        this.repositorioCertificado = repositorioCertificado;
        this.repositorioAsignacionDocumento = repositorioAsignacionDocumento;
    }

    @Transactional(readOnly = true)
    public ArchivoAlmacenado obtenerParaDescargaHttp(Long archivoId) {
        ArchivoAlmacenado archivo = servicioAlmacenamiento.obtenerEntidad(archivoId);
        if (tieneDescargaAmplia()) {
            return archivo;
        }
        if (tienePropiedadSobreArchivo(archivo)) {
            return archivo;
        }
        throw new ExcepcionNegocio("Archivo no encontrado", 404);
    }

    private boolean tieneDescargaAmplia() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            return false;
        }
        if (autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario && usuario.isEsSuperAdmin()) {
            return true;
        }
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PERMISOS_DESCARGA_AMPLIA::contains);
    }

    private boolean tienePropiedadSobreArchivo(ArchivoAlmacenado archivo) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            return false;
        }
        if (usuarioId.equals(archivo.getCreatedBy())) {
            return true;
        }
        if (repositorioRecibo.existsByArchivo_IdAndEmpleado_Usuario_IdAndDeletedFalse(archivo.getId(), usuarioId)) {
            return true;
        }
        if (repositorioCertificado.existsByArchivoIdAndEmpleado_Usuario_IdAndDeletedFalse(archivo.getId(), usuarioId)) {
            return true;
        }
        String fragmento = "/archivos/" + archivo.getId() + "/";
        return repositorioAsignacionDocumento
                .existsByEmpleado_Usuario_IdAndDocumento_UrlArchivoContainingAndDeletedFalse(usuarioId, fragmento)
                || repositorioAsignacionDocumento
                .existsFirmaConUrlParaUsuario(usuarioId, fragmento);
    }
}
