package com.rrhh.saas.nucleo.seguridad;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting in-memory para login y refresco (ventana fija deslizante).
 */
@Service
public class ServicioLimitacionIntentosAuth {

    private static final int MAX_INTENTOS_LOGIN = 10;
    private static final int MAX_INTENTOS_REFRESCO = 30;
    private static final long VENTANA_MS = 15 * 60 * 1000L;

    private final Map<String, Deque<Long>> intentosLogin = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> intentosRefresco = new ConcurrentHashMap<>();

    public void verificarLogin(String ip, String email) {
        verificar(intentosLogin, clave(ip, email), MAX_INTENTOS_LOGIN);
    }

    public void registrarFalloLogin(String ip, String email) {
        registrar(intentosLogin, clave(ip, email));
    }

    public void registrarExitoLogin(String ip, String email) {
        intentosLogin.remove(clave(ip, email));
    }

    public void verificarRefresco(String ip) {
        verificar(intentosRefresco, "refresco:" + normalizarIp(ip), MAX_INTENTOS_REFRESCO);
    }

    public void registrarIntentoRefresco(String ip) {
        registrar(intentosRefresco, "refresco:" + normalizarIp(ip));
    }

    private void verificar(Map<String, Deque<Long>> mapa, String clave, int maximo) {
        long ahora = Instant.now().toEpochMilli();
        Deque<Long> cola = mapa.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            purgar(cola, ahora);
            if (cola.size() >= maximo) {
                throw new ExcepcionNegocio(
                        "Demasiados intentos. Esperá unos minutos e intentá de nuevo.", 429);
            }
        }
    }

    private void registrar(Map<String, Deque<Long>> mapa, String clave) {
        long ahora = Instant.now().toEpochMilli();
        Deque<Long> cola = mapa.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            purgar(cola, ahora);
            cola.addLast(ahora);
        }
    }

    private void purgar(Deque<Long> cola, long ahora) {
        while (!cola.isEmpty() && ahora - cola.peekFirst() > VENTANA_MS) {
            cola.removeFirst();
        }
    }

    private String clave(String ip, String email) {
        String correo = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return normalizarIp(ip) + "|" + correo;
    }

    private String normalizarIp(String ip) {
        return ip == null || ip.isBlank() ? "desconocida" : ip.trim();
    }
}
