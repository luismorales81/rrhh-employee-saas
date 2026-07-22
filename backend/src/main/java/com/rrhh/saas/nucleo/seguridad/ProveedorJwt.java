package com.rrhh.saas.nucleo.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class ProveedorJwt {

    private static final int LONGITUD_MINIMA_SECRETO = 32;

    private final PropiedadesJwt propiedadesJwt;
    private SecretKey clave;

    public ProveedorJwt(PropiedadesJwt propiedadesJwt) {
        this.propiedadesJwt = propiedadesJwt;
    }

    @PostConstruct
    void validarSecreto() {
        String secreto = propiedadesJwt.getSecreto();
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalStateException(
                    "app.jwt.secreto debe tener al menos " + LONGITUD_MINIMA_SECRETO
                            + " bytes reales (sin relleno). Configure JWT_SECRETO.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public String generarTokenAcceso(Long usuarioId, String email, Long empresaId,
                                     boolean esSuperAdmin, List<String> permisos) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + propiedadesJwt.getExpiracionMs());

        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .claim("empresaId", empresaId)
                .claim("esSuperAdmin", esSuperAdmin)
                .claim("permisos", permisos)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public String generarTokenRefresco(Long usuarioId) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + propiedadesJwt.getRefrescoExpiracionMs());

        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("tipo", "refresco")
                .id(UUID.randomUUID().toString())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public Claims parsearToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esTokenValido(String token) {
        try {
            parsearToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
