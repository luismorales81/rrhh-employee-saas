package com.rrhh.saas.nucleo.seguridad;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado simétrico para secretos por tenant (p. ej. clave API de Gemini).
 */
@Service
public class ServicioCifradoDatos {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANO_IV = 12;
    private static final int TAMANO_TAG = 128;

    private final SecretKey claveSecreta;

    public ServicioCifradoDatos(@Value("${app.jwt.secreto}") String secretoBase) {
        this.claveSecreta = derivarClave(secretoBase);
    }

    public String cifrar(String textoPlano) {
        if (textoPlano == null || textoPlano.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[TAMANO_IV];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta, new GCMParameterSpec(TAMANO_TAG, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo cifrar el dato sensible");
        }
    }

    public String descifrar(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isBlank()) {
            return null;
        }
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifrado);
            byte[] iv = new byte[TAMANO_IV];
            byte[] cifrado = new byte[datos.length - TAMANO_IV];
            System.arraycopy(datos, 0, iv, 0, TAMANO_IV);
            System.arraycopy(datos, TAMANO_IV, cifrado, 0, cifrado.length);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta, new GCMParameterSpec(TAMANO_TAG, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo descifrar el dato sensible");
        }
    }

    private SecretKey derivarClave(String secretoBase) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secretoBase.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new ExcepcionNegocio("No se pudo inicializar el servicio de cifrado");
        }
    }
}
