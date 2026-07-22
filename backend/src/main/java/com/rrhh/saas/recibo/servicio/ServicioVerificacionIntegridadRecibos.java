package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.recibo.config.PropiedadesVerificacionIntegridadRecibos;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioVerificacionIntegridadRecibos {

    private static final Logger log = LoggerFactory.getLogger(ServicioVerificacionIntegridadRecibos.class);

    private final RepositorioRecibo repositorioRecibo;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioAuditoria servicioAuditoria;
    private final PropiedadesVerificacionIntegridadRecibos propiedades;

    public ServicioVerificacionIntegridadRecibos(RepositorioRecibo repositorioRecibo,
                                                 ServicioAlmacenamiento servicioAlmacenamiento,
                                                 ServicioAuditoria servicioAuditoria,
                                                 PropiedadesVerificacionIntegridadRecibos propiedades) {
        this.repositorioRecibo = repositorioRecibo;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioAuditoria = servicioAuditoria;
        this.propiedades = propiedades;
    }

    @Transactional
    public ResultadoVerificacionIntegridad verificarTodos() {
        if (!propiedades.isHabilitada()) {
            return new ResultadoVerificacionIntegridad(0, 0, List.of());
        }

        List<Recibo> recibos = repositorioRecibo.findByArchivoIsNotNullAndHashPdfIsNotNullAndDeletedFalse();
        int verificados = 0;
        int fallidos = 0;
        List<Long> recibosFallidos = new ArrayList<>();

        for (Recibo recibo : recibos) {
            try {
                ContextoTenant.establecerEmpresaId(recibo.getEmpresa().getId());
                String hashActual = servicioAlmacenamiento.calcularHashArchivo(recibo.getArchivo().getId());
                verificados++;
                if (!hashActual.equals(recibo.getHashPdf())) {
                    fallidos++;
                    recibosFallidos.add(recibo.getId());
                    log.error("Integridad fallida para recibo {}: hash almacenado {} vs actual {}",
                            recibo.getId(), recibo.getHashPdf(), hashActual);
                    servicioAuditoria.registrar(
                            "RECIBO_INTEGRIDAD_FALLIDA",
                            "Recibo",
                            recibo.getId(),
                            recibo.getHashPdf(),
                            hashActual
                    );
                }
            } catch (Exception e) {
                fallidos++;
                recibosFallidos.add(recibo.getId());
                log.error("Error al verificar integridad del recibo {}: {}", recibo.getId(), e.getMessage());
            } finally {
                ContextoTenant.limpiar();
            }
        }

        return new ResultadoVerificacionIntegridad(verificados, fallidos, recibosFallidos);
    }

    public record ResultadoVerificacionIntegridad(
            int verificados,
            int fallidos,
            List<Long> recibosFallidos
    ) {
    }
}
