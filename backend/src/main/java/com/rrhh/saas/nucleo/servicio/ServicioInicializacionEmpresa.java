package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.ausentismo.dominio.TipoAusencia;
import com.rrhh.saas.ausentismo.repositorio.RepositorioTipoAusencia;
import com.rrhh.saas.documento.dominio.CategoriaDocumento;
import com.rrhh.saas.documento.repositorio.RepositorioCategoriaDocumento;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.dominio.Permiso;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.repositorio.RepositorioPermiso;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServicioInicializacionEmpresa {

    private final RepositorioRol repositorioRol;
    private final RepositorioPermiso repositorioPermiso;
    private final RepositorioTipoAusencia repositorioTipoAusencia;
    private final RepositorioCategoriaDocumento repositorioCategoriaDocumento;

    public ServicioInicializacionEmpresa(RepositorioRol repositorioRol,
                                         RepositorioPermiso repositorioPermiso,
                                         RepositorioTipoAusencia repositorioTipoAusencia,
                                         RepositorioCategoriaDocumento repositorioCategoriaDocumento) {
        this.repositorioRol = repositorioRol;
        this.repositorioPermiso = repositorioPermiso;
        this.repositorioTipoAusencia = repositorioTipoAusencia;
        this.repositorioCategoriaDocumento = repositorioCategoriaDocumento;
    }

    @Transactional
    public void crearRolesPorDefecto(Empresa empresa) {
        crearTiposAusenciaPorDefecto(empresa);
        crearCategoriasDocumentoPorDefecto(empresa);
        crearRol(empresa, "Administrador", Set.of(
                "usuario.crear", "usuario.ver", "rol.crear", "rol.ver", "permiso.ver",
                "empleado.crear", "empleado.actualizar", "empleado.ver",
                "organizacion.gestionar", "workflow.configurar", "workflow.ver",
                "tipo_solicitud.gestionar", "solicitud.ver", "solicitud.aprobar",
                "solicitud.rechazar", "solicitud.cancelar", "empresa.ver",
                "vacacion.politica.gestionar", "vacacion.politica.ver",
                "vacacion.ver", "vacacion.saldo.gestionar", "vacacion.saldo.ver",
                "vacacion.configurar", "vacacion.patron.gestionar", "vacacion.aprobar",
                "ausencia.tipo.gestionar", "ausencia.tipo.ver", "ausencia.registrar",
                "ausencia.ver", "ausencia.actualizar",
                "documento.categoria.gestionar", "documento.categoria.ver",
                "documento.gestionar", "documento.ver", "documento.asignar", "documento.asignacion.ver",
                "comunicacion.publicar", "comunicacion.ver", "comunicacion.destinatarios.ver",
                "recibo.cargar", "recibo.ver", "recibo.exportar_expediente",
                "archivo.subir", "archivo.descargar",
                "onboarding.gestionar", "onboarding.ver",
                "activo.gestionar", "activo.ver",
                "offboarding.gestionar", "offboarding.ver",
                "novedad.gestionar", "novedad.ver",
                "fichaje.zona.ver", "fichaje.zona.gestionar", "fichaje.configurar",
                "fichaje.ver", "fichaje.revisar",
                "certificado.cargar", "certificado.ver_resumen", "certificado.configurar",
                "reporte.ausentismo.ver"
        ));
        crearRol(empresa, "RRHH", Set.of(
                "empleado.crear", "empleado.actualizar", "empleado.ver",
                "organizacion.gestionar", "workflow.configurar", "workflow.ver",
                "tipo_solicitud.gestionar", "solicitud.ver",
                "solicitud.aprobar", "solicitud.rechazar", "solicitud.solicitar_cambios",
                "solicitud.cancelar",
                "vacacion.politica.ver", "vacacion.ver", "vacacion.saldo.gestionar", "vacacion.saldo.ver",
                "vacacion.configurar", "vacacion.patron.gestionar", "vacacion.aprobar",
                "ausencia.tipo.ver", "ausencia.registrar", "ausencia.ver", "ausencia.actualizar",
                "documento.categoria.ver", "documento.gestionar", "documento.ver",
                "documento.asignar", "documento.asignacion.ver",
                "comunicacion.publicar", "comunicacion.ver", "comunicacion.destinatarios.ver",
                "recibo.cargar", "recibo.ver", "recibo.exportar_expediente",
                "archivo.subir", "archivo.descargar",
                "onboarding.gestionar", "onboarding.ver",
                "activo.gestionar", "activo.ver",
                "offboarding.gestionar", "offboarding.ver",
                "novedad.gestionar", "novedad.ver",
                "fichaje.zona.ver", "fichaje.zona.gestionar", "fichaje.configurar",
                "fichaje.ver", "fichaje.revisar",
                "certificado.cargar", "certificado.ver_resumen", "certificado.configurar",
                "reporte.ausentismo.ver"
        ));
        crearRol(empresa, "Supervisor", Set.of(
                "empleado.ver", "solicitud.ver", "solicitud.aprobar",
                "solicitud.rechazar", "solicitud.escalar",
                "vacacion.ver", "vacacion.saldo.ver",
                "ausencia.ver",
                "certificado.ver_resumen", "reporte.ausentismo.ver",
                "documento.asignacion.ver", "documento.ver",
                "comunicacion.ver", "comunicacion.destinatarios.ver",
                "fichaje.ver"
        ));
        crearRol(empresa, "Empleado", Set.of(
                "empleado.ver", "solicitud.crear", "solicitud.ver", "solicitud.cancelar",
                "vacacion.solicitar", "vacacion.ver", "vacacion.saldo.ver",
                "ausencia.solicitar", "ausencia.mis.ver",
                "documento.mis.ver", "documento.confirmar_lectura", "documento.firmar",
                "comunicacion.mis.ver", "comunicacion.confirmar_lectura",
                "recibo.mis.ver", "recibo.firmar",
                "notificacion.ver",
                "archivo.subir", "archivo.descargar",
                "onboarding.ver",
                "activo.ver",
                "fichaje.registrar", "fichaje.mis.ver",
                "certificado.cargar", "certificado.ver_propio"
        ));
        crearRol(empresa, "MedicoLaboral", Set.of(
                "certificado.cargar", "certificado.ver_completo", "certificado.ver_propio",
                "archivo.subir", "archivo.descargar", "empleado.ver"
        ));
        crearRol(empresa, "AuditorMedico", Set.of(
                "certificado.ver_completo", "certificado.validar", "certificado.rechazar",
                "certificado.configurar", "reporte.ausentismo.ver",
                "archivo.descargar", "empleado.ver"
        ));
    }

    private void crearRol(Empresa empresa, String nombre, Set<String> codigosPermiso) {
        Rol rol = new Rol();
        rol.setEmpresa(empresa);
        rol.setNombre(nombre);
        rol.setDescripcion("Rol por defecto: " + nombre);

        Set<Permiso> permisos = new HashSet<>();
        for (String codigo : codigosPermiso) {
            repositorioPermiso.findByCodigoAndDeletedFalse(codigo).ifPresent(permisos::add);
        }
        rol.setPermisos(permisos);
        repositorioRol.save(rol);
    }

    private void crearTiposAusenciaPorDefecto(Empresa empresa) {
        crearTipoAusenciaSiNoExiste(empresa, "Enfermedad", "Ausencia por enfermedad", true);
        crearTipoAusenciaSiNoExiste(empresa, "Llegada tarde", "Llegada fuera de horario", false);
        crearTipoAusenciaSiNoExiste(empresa, "Falta injustificada", "Inasistencia sin justificación", false);
        crearTipoAusenciaSiNoExiste(empresa, "Falta justificada", "Inasistencia con justificación", true);
        crearTipoAusenciaSiNoExiste(empresa, "Permiso personal", "Permiso personal", false);
    }

    private void crearTipoAusenciaSiNoExiste(Empresa empresa, String nombre, String descripcion,
                                             boolean requiereCertificado) {
        if (repositorioTipoAusencia.findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), nombre).isPresent()) {
            return;
        }
        TipoAusencia tipo = new TipoAusencia();
        tipo.setEmpresa(empresa);
        tipo.setNombre(nombre);
        tipo.setDescripcion(descripcion);
        tipo.setRequiereCertificado(requiereCertificado);
        repositorioTipoAusencia.save(tipo);
    }

    private void crearCategoriasDocumentoPorDefecto(Empresa empresa) {
        crearCategoriaDocumentoSiNoExiste(empresa, "Contrato", "Contratos laborales");
        crearCategoriaDocumentoSiNoExiste(empresa, "Reglamento", "Reglamentos internos");
        crearCategoriaDocumentoSiNoExiste(empresa, "Recibo", "Recibos de sueldo");
        crearCategoriaDocumentoSiNoExiste(empresa, "Certificado", "Certificados y constancias");
        crearCategoriaDocumentoSiNoExiste(empresa, "Política", "Políticas de la empresa");
    }

    private void crearCategoriaDocumentoSiNoExiste(Empresa empresa, String nombre, String descripcion) {
        if (repositorioCategoriaDocumento.findByEmpresaIdAndNombreAndDeletedFalse(empresa.getId(), nombre).isPresent()) {
            return;
        }
        CategoriaDocumento categoria = new CategoriaDocumento();
        categoria.setEmpresa(empresa);
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        repositorioCategoriaDocumento.save(categoria);
    }
}
