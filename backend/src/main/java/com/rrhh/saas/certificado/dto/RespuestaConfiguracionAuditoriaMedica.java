package com.rrhh.saas.certificado.dto;



import com.rrhh.saas.certificado.dominio.CatalogoModelosGemini;



import java.util.List;



public record RespuestaConfiguracionAuditoriaMedica(

        int diasReposoAlertaLeve,

        int maxLicenciasCortasPeriodo,

        int periodoEvaluacionDias,

        boolean requiereRevisionManualSiempre,

        boolean iaConfigurada,

        String proyectoGcp,

        String regionVertex,

        String modeloGemini,

        boolean credencialesConfiguradas,

        int maxIntentosExtraccion,

        List<String> modelosDisponibles

) {

    public static RespuestaConfiguracionAuditoriaMedica desdeEntidad(

            int diasReposoAlertaLeve,

            int maxLicenciasCortasPeriodo,

            int periodoEvaluacionDias,

            boolean requiereRevisionManualSiempre,

            boolean iaConfigurada,

            String proyectoGcp,

            String regionVertex,

            String modeloGemini,

            boolean credencialesConfiguradas,

            int maxIntentosExtraccion) {

        return new RespuestaConfiguracionAuditoriaMedica(

                diasReposoAlertaLeve,

                maxLicenciasCortasPeriodo,

                periodoEvaluacionDias,

                requiereRevisionManualSiempre,

                iaConfigurada,

                proyectoGcp,

                regionVertex,

                modeloGemini,

                credencialesConfiguradas,

                maxIntentosExtraccion,

                CatalogoModelosGemini.MODELOS_DISPONIBLES

        );

    }

}


