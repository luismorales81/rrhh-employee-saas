package com.rrhh.saas.nucleo.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.comun.excepcion.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ManejadorAccesoDenegadoHttp implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ManejadorAccesoDenegadoHttp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new RespuestaError(LocalDateTime.now(), 403, "Acceso denegado"));
    }
}
