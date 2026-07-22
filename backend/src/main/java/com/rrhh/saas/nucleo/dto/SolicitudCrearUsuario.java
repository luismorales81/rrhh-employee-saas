package com.rrhh.saas.nucleo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record SolicitudCrearUsuario(
        @NotNull Long empresaId,
        @NotBlank @Email String email,
        @NotBlank String password,
        Set<Long> rolesIds
) {
}
