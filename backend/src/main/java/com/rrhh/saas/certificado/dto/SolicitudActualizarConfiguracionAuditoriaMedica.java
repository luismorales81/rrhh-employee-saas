package com.rrhh.saas.certificado.dto;



import jakarta.validation.constraints.Max;

import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotNull;



public record SolicitudActualizarConfiguracionAuditoriaMedica(

        @NotNull @Min(1) @Max(90) Integer diasReposoAlertaLeve,

        @NotNull @Min(1) @Max(20) Integer maxLicenciasCortasPeriodo,

        @NotNull @Min(30) @Max(365) Integer periodoEvaluacionDias,

        @NotNull Boolean requiereRevisionManualSiempre,

        String proyectoGcp,

        String regionVertex,

        String modeloGemini,

        String credencialesServicioJson,

        @Min(1) @Max(10) Integer maxIntentosExtraccion

) {

}


