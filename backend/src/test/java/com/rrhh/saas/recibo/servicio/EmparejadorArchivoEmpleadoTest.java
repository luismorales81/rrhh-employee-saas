package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.dominio.EstadoEmpleado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmparejadorArchivoEmpleadoTest {

    @Test
    void emparejaPorCuilEnNombreArchivo() {
        Empleado juan = empleado(1L, "Juan", "Pérez", "EMP-001", "30123456", "20-30123456-7");
        var resultado = EmparejadorArchivoEmpleado.emparejar("20301234567.pdf", List.of(juan));
        assertTrue(resultado.esExitoso());
        assertEquals(1L, resultado.empleado().orElseThrow().getId());
    }

    @Test
    void emparejaPorNumeroEmpleado() {
        Empleado maria = empleado(2L, "María", "García", "EMP-002", "28987654", "27-28987654-3");
        var resultado = EmparejadorArchivoEmpleado.emparejar("EMP-002.pdf", List.of(maria));
        assertTrue(resultado.esExitoso());
        assertEquals(2L, resultado.empleado().orElseThrow().getId());
    }

    @Test
    void reportaErrorSinCoincidencia() {
        var resultado = EmparejadorArchivoEmpleado.emparejar("desconocido.pdf", List.of());
        assertTrue(resultado.error() != null && !resultado.error().isBlank());
    }

    private Empleado empleado(Long id, String nombre, String apellido, String numero, String dni, String cuil) {
        Empleado empleado = new Empleado();
        empleado.setId(id);
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setNumeroEmpleado(numero);
        empleado.setDni(dni);
        empleado.setCuil(cuil);
        empleado.setEstado(EstadoEmpleado.ACTIVO);
        return empleado;
    }
}
