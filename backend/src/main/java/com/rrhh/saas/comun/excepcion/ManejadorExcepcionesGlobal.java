package com.rrhh.saas.comun.excepcion;

import com.rrhh.saas.comun.excepcion.ExcepcionValidacionVacaciones;
import com.rrhh.saas.comun.excepcion.RespuestaErrorValidacionVacaciones;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(ExcepcionValidacionVacaciones.class)
    public ResponseEntity<RespuestaErrorValidacionVacaciones> manejarValidacionVacaciones(
            ExcepcionValidacionVacaciones ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new RespuestaErrorValidacionVacaciones(
                        LocalDateTime.now(),
                        422,
                        ex.getMessage(),
                        ex.getErrores(),
                        ex.getAdvertencias()));
    }

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<RespuestaError> manejarExcepcionNegocio(ExcepcionNegocio ex) {
        return ResponseEntity.status(ex.getCodigoHttp())
                .body(new RespuestaError(LocalDateTime.now(), ex.getCodigoHttp(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RespuestaError> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new RespuestaError(LocalDateTime.now(), 401, "Credenciales inválidas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new RespuestaError(LocalDateTime.now(), 403, "Acceso denegado"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos inválidos");
        return ResponseEntity.badRequest()
                .body(new RespuestaError(LocalDateTime.now(), 400, mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarExcepcionGenerica(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RespuestaError(LocalDateTime.now(), 500, "Error interno del servidor"));
    }
}
