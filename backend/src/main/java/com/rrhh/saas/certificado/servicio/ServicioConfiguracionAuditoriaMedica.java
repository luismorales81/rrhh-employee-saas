package com.rrhh.saas.certificado.servicio;



import com.rrhh.saas.certificado.dominio.CatalogoModelosGemini;

import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.dto.RespuestaConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.dto.SolicitudActualizarConfiguracionAuditoriaMedica;

import com.rrhh.saas.certificado.ia.ConfiguracionVertexTenant;

import com.rrhh.saas.certificado.repositorio.RepositorioConfiguracionAuditoriaMedica;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;

import com.rrhh.saas.comun.tenant.ContextoTenant;

import com.rrhh.saas.nucleo.dominio.Empresa;

import com.rrhh.saas.nucleo.seguridad.ServicioCifradoDatos;

import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



@Service

public class ServicioConfiguracionAuditoriaMedica {



    private final RepositorioConfiguracionAuditoriaMedica repositorio;

    private final ServicioEmpresa servicioEmpresa;

    private final ServicioCifradoDatos servicioCifradoDatos;



    public ServicioConfiguracionAuditoriaMedica(RepositorioConfiguracionAuditoriaMedica repositorio,

                                                ServicioEmpresa servicioEmpresa,

                                                ServicioCifradoDatos servicioCifradoDatos) {

        this.repositorio = repositorio;

        this.servicioEmpresa = servicioEmpresa;

        this.servicioCifradoDatos = servicioCifradoDatos;

    }



    @Transactional

    public RespuestaConfiguracionAuditoriaMedica obtener() {

        return mapear(obtenerOCrearEntidad(obtenerEmpresaActual().getId()));

    }



    @Transactional

    public RespuestaConfiguracionAuditoriaMedica actualizar(SolicitudActualizarConfiguracionAuditoriaMedica solicitud) {

        ConfiguracionAuditoriaMedica config = obtenerOCrearEntidad(obtenerEmpresaActual().getId());



        config.setDiasReposoAlertaLeve(solicitud.diasReposoAlertaLeve());

        config.setMaxLicenciasCortasPeriodo(solicitud.maxLicenciasCortasPeriodo());

        config.setPeriodoEvaluacionDias(solicitud.periodoEvaluacionDias());

        config.setRequiereRevisionManualSiempre(solicitud.requiereRevisionManualSiempre());



        if (solicitud.proyectoGcp() != null) {

            config.setProyectoGcp(solicitud.proyectoGcp().isBlank() ? null : solicitud.proyectoGcp().trim());

        }

        if (solicitud.regionVertex() != null) {

            config.setRegionVertex(solicitud.regionVertex().isBlank() ? null : solicitud.regionVertex().trim());

        }

        if (solicitud.modeloGemini() != null) {

            if (!solicitud.modeloGemini().isBlank()

                    && !CatalogoModelosGemini.esModeloPermitido(solicitud.modeloGemini())) {

                throw new ExcepcionNegocio("Modelo Gemini no permitido: " + solicitud.modeloGemini());

            }

            config.setModeloGemini(solicitud.modeloGemini().isBlank() ? null : solicitud.modeloGemini().trim());

        }

        if (solicitud.credencialesServicioJson() != null && !solicitud.credencialesServicioJson().isBlank()) {

            config.setCredencialesServicioCifrada(

                    servicioCifradoDatos.cifrar(solicitud.credencialesServicioJson().trim()));

        }

        if (solicitud.maxIntentosExtraccion() != null) {

            config.setMaxIntentosExtraccion(solicitud.maxIntentosExtraccion());

        }



        validarConfiguracionVertex(config);

        return mapear(repositorio.save(config));

    }



    @Transactional(readOnly = true)

    public ConfiguracionAuditoriaMedica obtenerEntidad(Long empresaId) {

        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)

                .orElseGet(() -> construirValoresPorDefecto(empresaId));

    }



    @Transactional(readOnly = true)

    public ConfiguracionVertexTenant resolverConfigVertex(Long empresaId) {

        ConfiguracionAuditoriaMedica config = obtenerEntidad(empresaId);

        if (!config.tieneIaConfigurada()) {

            return null;

        }

        String json = servicioCifradoDatos.descifrar(config.getCredencialesServicioCifrada());

        return new ConfiguracionVertexTenant(

                config.getProyectoGcp(),

                config.getRegionVertex(),

                config.getModeloGemini(),

                json

        );

    }



    private void validarConfiguracionVertex(ConfiguracionAuditoriaMedica config) {

        boolean tieneAlguno = (config.getProyectoGcp() != null && !config.getProyectoGcp().isBlank())

                || (config.getRegionVertex() != null && !config.getRegionVertex().isBlank())

                || (config.getModeloGemini() != null && !config.getModeloGemini().isBlank())

                || config.tieneCredencialesConfiguradas();

        if (tieneAlguno && !config.tieneIaConfigurada()) {

            throw new ExcepcionNegocio(

                    "La configuración Vertex AI está incompleta: proyecto, región, modelo y credenciales son obligatorios");

        }

    }



    private ConfiguracionAuditoriaMedica obtenerOCrearEntidad(Long empresaId) {

        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)

                .orElseGet(() -> crearPorDefecto(empresaId));

    }



    private ConfiguracionAuditoriaMedica crearPorDefecto(Long empresaId) {

        ConfiguracionAuditoriaMedica config = construirValoresPorDefecto(empresaId);

        return repositorio.save(config);

    }



    private ConfiguracionAuditoriaMedica construirValoresPorDefecto(Long empresaId) {

        ConfiguracionAuditoriaMedica config = new ConfiguracionAuditoriaMedica();

        config.setEmpresa(servicioEmpresa.obtenerEntidad(empresaId));

        return config;

    }



    private Empresa obtenerEmpresaActual() {

        Long empresaId = ContextoTenant.obtenerEmpresaId();

        if (empresaId == null) {

            throw new ExcepcionNegocio("Empresa no definida en contexto", 403);

        }

        return servicioEmpresa.obtenerEntidad(empresaId);

    }



    private RespuestaConfiguracionAuditoriaMedica mapear(ConfiguracionAuditoriaMedica config) {

        return RespuestaConfiguracionAuditoriaMedica.desdeEntidad(

                config.getDiasReposoAlertaLeve(),

                config.getMaxLicenciasCortasPeriodo(),

                config.getPeriodoEvaluacionDias(),

                config.isRequiereRevisionManualSiempre(),

                config.tieneIaConfigurada(),

                config.getProyectoGcp(),

                config.getRegionVertex(),

                config.getModeloGemini(),

                config.tieneCredencialesConfiguradas(),

                config.getMaxIntentosExtraccion()

        );

    }

}


