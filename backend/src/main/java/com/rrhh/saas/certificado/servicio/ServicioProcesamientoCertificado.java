package com.rrhh.saas.certificado.servicio;



import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;

import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;

import com.rrhh.saas.certificado.dominio.Certificado;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;

import com.rrhh.saas.certificado.ia.ConfiguracionVertexTenant;

import com.rrhh.saas.certificado.ia.GeminiExtraccionService;

import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;

import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;

import com.rrhh.saas.comun.tenant.ContextoTenant;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Service;



@Service

public class ServicioProcesamientoCertificado {



    private static final Logger log = LoggerFactory.getLogger(ServicioProcesamientoCertificado.class);



    private final RepositorioCertificado repositorio;

    private final ServicioAlmacenamiento servicioAlmacenamiento;

    private final ServicioConfiguracionAuditoriaMedica servicioConfiguracion;

    private final GeminiExtraccionService geminiExtraccionService;

    private final ServicioCertificado servicioCertificado;



    public ServicioProcesamientoCertificado(RepositorioCertificado repositorio,

                                            ServicioAlmacenamiento servicioAlmacenamiento,

                                            ServicioConfiguracionAuditoriaMedica servicioConfiguracion,

                                            GeminiExtraccionService geminiExtraccionService,

                                            ServicioCertificado servicioCertificado) {

        this.repositorio = repositorio;

        this.servicioAlmacenamiento = servicioAlmacenamiento;

        this.servicioConfiguracion = servicioConfiguracion;

        this.geminiExtraccionService = geminiExtraccionService;

        this.servicioCertificado = servicioCertificado;

    }



    @Async("ejecutorExtraccionCertificados")

    public void procesarExtraccionAsync(Long certificadoId, Long empresaId) {

        ContextoTenant.establecerEmpresaId(empresaId);

        try {

            Certificado certificado = repositorio.findById(certificadoId).orElse(null);

            if (certificado == null || certificado.isDeleted()) {

                return;

            }

            if (certificado.getEstado() != EstadoCertificado.EN_PROCESAMIENTO) {

                return;

            }



            servicioCertificado.marcarIntentoExtraccion(certificadoId);



            ConfiguracionVertexTenant config = servicioConfiguracion.resolverConfigVertex(empresaId);

            if (config == null) {

                servicioCertificado.registrarFalloExtraccion(certificadoId,

                        "Configuración Vertex AI no disponible", null);

                return;

            }



            if (certificado.getArchivoId() == null) {

                servicioCertificado.registrarFalloExtraccion(certificadoId,

                        "Certificado sin archivo asociado", null);

                return;

            }



            ArchivoAlmacenado archivo = servicioAlmacenamiento.obtenerEntidad(certificado.getArchivoId());

            byte[] contenido = servicioAlmacenamiento.leerBytes(certificado.getArchivoId());



            try {

                ResultadoExtraccionGemini resultado = geminiExtraccionService.extraer(

                        contenido, archivo.getTipoMime(), config);

                servicioCertificado.aplicarResultadoExtraccion(certificadoId, resultado, null);

            } catch (ExcepcionNegocio e) {

                servicioCertificado.registrarFalloExtraccion(certificadoId, e.getMessage(), null);

            } catch (Exception e) {

                log.error("Error inesperado en extracción del certificado {}: {}", certificadoId, e.getMessage());

                servicioCertificado.registrarFalloExtraccion(certificadoId, e.getMessage(), null);

            }

        } finally {

            ContextoTenant.limpiar();

        }

    }

}


