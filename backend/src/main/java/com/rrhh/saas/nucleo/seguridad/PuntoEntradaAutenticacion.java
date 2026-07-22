package com.rrhh.saas.nucleo.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.comun.excepcion.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class PuntoEntradaAutenticacion implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public PuntoEntradaAutenticacion(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new RespuestaError(LocalDateTime.now(), 401, "No autenticado o token inválido"));
    }
}
