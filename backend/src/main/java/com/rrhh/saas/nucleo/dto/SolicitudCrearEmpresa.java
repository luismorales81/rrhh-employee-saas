package com.rrhh.saas.nucleo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearEmpresa(
        @NotBlank String nombre,
        String razonSocial,
        String cuit,
        @Email String email,
        String telefono
) {
}
