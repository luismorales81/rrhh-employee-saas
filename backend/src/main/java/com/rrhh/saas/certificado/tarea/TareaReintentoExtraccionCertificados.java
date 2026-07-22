package com.rrhh.saas.certificado.tarea;



import com.rrhh.saas.certificado.config.PropiedadesCertificados;

import com.rrhh.saas.certificado.dominio.Certificado;

import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;

import com.rrhh.saas.certificado.servicio.ServicioCertificado;

import com.rrhh.saas.certificado.servicio.ServicioConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.servicio.ServicioProcesamientoCertificado;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;



import java.time.LocalDateTime;

import java.util.List;



@Component

public class TareaReintentoExtraccionCertificados {



    private static final Logger log = LoggerFactory.getLogger(TareaReintentoExtraccionCertificados.class);



    private final RepositorioCertificado repositorio;

    private final PropiedadesCertificados propiedades;

    private final ServicioConfiguracionAuditoriaMedica servicioConfiguracion;

    private final ServicioProcesamientoCertificado servicioProcesamiento;

    private final ServicioCertificado servicioCertificado;



    public TareaReintentoExtraccionCertificados(RepositorioCertificado repositorio,

                                                PropiedadesCertificados propiedades,

                                                ServicioConfiguracionAuditoriaMedica servicioConfiguracion,

                                                ServicioProcesamientoCertificado servicioProcesamiento,

                                                ServicioCertificado servicioCertificado) {

        this.repositorio = repositorio;

        this.propiedades = propiedades;

        this.servicioConfiguracion = servicioConfiguracion;

        this.servicioProcesamiento = servicioProcesamiento;

        this.servicioCertificado = servicioCertificado;

    }



    @Scheduled(cron = "${app.certificados.reintento.cron:0 */5 * * * *}")

    public void reintentarExtraccionesColgadas() {

        try {

            LocalDateTime limite = LocalDateTime.now()

                    .minusMinutes(propiedades.getReintento().getMinutosColgado());

            List<Certificado> colgados = repositorio.buscarColgadosEnProcesamiento(limite);

            if (colgados.isEmpty()) {

                return;

            }



            int reintentados = 0;

            int finalizados = 0;

            for (Certificado certificado : colgados) {

                ConfiguracionAuditoriaMedica config = servicioConfiguracion.obtenerEntidad(

                        certificado.getEmpresa().getId());

                if (certificado.getIntentosExtraccion() >= config.getMaxIntentosExtraccion()) {

                    servicioCertificado.finalizarExtraccionAgotada(certificado.getId());

                    finalizados++;

                    continue;

                }

                servicioProcesamiento.procesarExtraccionAsync(

                        certificado.getId(), certificado.getEmpresa().getId());

                reintentados++;

            }

            log.info("Reintento extracción certificados: {} reintentados, {} finalizados por agotar intentos",

                    reintentados, finalizados);

        } catch (Exception e) {

            log.warn("Error en tarea de reintento de extracción de certificados: {}", e.getMessage());

        }

    }

}


