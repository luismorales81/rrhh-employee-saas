package com.rrhh.saas.nucleo.seguridad;

import java.util.List;

/**
 * Permisos operativos de tenant concedidos al SuperAdmin solo en modo soporte.
 */
public final class CatalogoPermisosSoporteTenant {

    private CatalogoPermisosSoporteTenant() {
    }

    public static List<String> todos() {
        return List.of(
                "empleado.crear", "empleado.actualizar", "empleado.ver",
                "organizacion.gestionar",
                "workflow.configurar", "workflow.ver", "tipo_solicitud.gestionar",
                "solicitud.crear", "solicitud.ver", "solicitud.aprobar",
                "solicitud.rechazar", "solicitud.solicitar_cambios", "solicitud.escalar", "solicitud.cancelar",
                "vacacion.politica.gestionar", "vacacion.politica.ver", "vacacion.ver",
                "vacacion.saldo.gestionar", "vacacion.saldo.ver", "vacacion.solicitar",
                "ausencia.tipo.gestionar", "ausencia.tipo.ver", "ausencia.registrar",
                "ausencia.ver", "ausencia.actualizar",
                "documento.categoria.gestionar", "documento.categoria.ver",
                "documento.gestionar", "documento.ver", "documento.asignar", "documento.asignacion.ver",
                "documento.mis.ver", "documento.confirmar_lectura", "documento.firmar",
                "comunicacion.publicar", "comunicacion.ver", "comunicacion.destinatarios.ver",
                "comunicacion.mis.ver", "comunicacion.confirmar_lectura",
                "recibo.cargar", "recibo.ver", "recibo.mis.ver", "recibo.firmar",
                "notificacion.ver",
                "archivo.subir", "archivo.descargar",
                "onboarding.gestionar", "onboarding.ver",
                "activo.gestionar", "activo.ver",
                "offboarding.gestionar", "offboarding.ver",
                "novedad.gestionar", "novedad.ver",
                "rol.crear"
        );
    }
}
