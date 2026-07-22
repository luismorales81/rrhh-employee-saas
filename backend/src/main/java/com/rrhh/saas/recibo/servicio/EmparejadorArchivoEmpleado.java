package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.empleado.dominio.Empleado;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class EmparejadorArchivoEmpleado {

    private EmparejadorArchivoEmpleado() {
    }

    public static ResultadoEmparejamiento emparejar(String nombreArchivo, List<Empleado> empleados) {
        String base = extraerNombreBase(nombreArchivo);
        if (base.isBlank()) {
            return ResultadoEmparejamiento.error(nombreArchivo, "Nombre de archivo inválido");
        }

        String soloDigitos = base.replaceAll("[^0-9]", "");
        List<Empleado> coincidencias = new ArrayList<>();

        if (soloDigitos.length() == 11) {
            coincidencias.addAll(buscarPorCuil(soloDigitos, empleados));
        }
        if (coincidencias.isEmpty()) {
            coincidencias.addAll(buscarPorNumeroEmpleado(base, empleados));
        }
        if (coincidencias.isEmpty() && (soloDigitos.length() == 7 || soloDigitos.length() == 8)) {
            coincidencias.addAll(buscarPorDni(soloDigitos, empleados));
        }

        if (coincidencias.isEmpty()) {
            return ResultadoEmparejamiento.sinCoincidencia(nombreArchivo);
        }
        if (coincidencias.size() > 1) {
            return ResultadoEmparejamiento.error(nombreArchivo, "Emparejamiento ambiguo: varios empleados coinciden");
        }
        return ResultadoEmparejamiento.exitoso(nombreArchivo, coincidencias.get(0));
    }

    private static String extraerNombreBase(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "";
        }
        String nombre = nombreArchivo;
        int barra = Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\'));
        if (barra >= 0) {
            nombre = nombre.substring(barra + 1);
        }
        int punto = nombre.lastIndexOf('.');
        if (punto > 0) {
            nombre = nombre.substring(0, punto);
        }
        return nombre.trim();
    }

    private static List<Empleado> buscarPorCuil(String cuilNormalizado, List<Empleado> empleados) {
        return empleados.stream()
                .filter(e -> e.getCuil() != null && normalizarDigitos(e.getCuil()).equals(cuilNormalizado))
                .toList();
    }

    private static List<Empleado> buscarPorNumeroEmpleado(String numero, List<Empleado> empleados) {
        String normalizado = numero.toUpperCase(Locale.ROOT);
        return empleados.stream()
                .filter(e -> e.getNumeroEmpleado() != null
                        && e.getNumeroEmpleado().equalsIgnoreCase(normalizado))
                .toList();
    }

    private static List<Empleado> buscarPorDni(String dni, List<Empleado> empleados) {
        return empleados.stream()
                .filter(e -> e.getDni() != null && normalizarDigitos(e.getDni()).equals(dni))
                .toList();
    }

    private static String normalizarDigitos(String valor) {
        return valor.replaceAll("[^0-9]", "");
    }

    public record ResultadoEmparejamiento(
            String nombreArchivo,
            Optional<Empleado> empleado,
            String error
    ) {
        public boolean esExitoso() {
            return empleado.isPresent() && (error == null || error.isBlank());
        }

        static ResultadoEmparejamiento exitoso(String archivo, Empleado empleado) {
            return new ResultadoEmparejamiento(archivo, Optional.of(empleado), null);
        }

        static ResultadoEmparejamiento sinCoincidencia(String archivo) {
            return new ResultadoEmparejamiento(archivo, Optional.empty(), "Sin empleado coincidente");
        }

        static ResultadoEmparejamiento error(String archivo, String mensaje) {
            return new ResultadoEmparejamiento(archivo, Optional.empty(), mensaje);
        }
    }
}
