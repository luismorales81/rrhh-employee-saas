package com.rrhh.saas.fichaje.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioGeocercaTest {

    private ServicioGeocerca servicioGeocerca;

    @BeforeEach
    void setUp() {
        servicioGeocerca = new ServicioGeocerca();
    }

    @Test
    void puntoDentroPoligonoCuadrado() {
        List<List<Double>> cuadrado = List.of(
                List.of(-34.60, -58.38),
                List.of(-34.60, -58.37),
                List.of(-34.61, -58.37),
                List.of(-34.61, -58.38)
        );
        assertTrue(servicioGeocerca.puntoDentroPoligono(-34.605, -58.375, cuadrado));
    }

    @Test
    void puntoFueraPoligono() {
        List<List<Double>> cuadrado = List.of(
                List.of(-34.60, -58.38),
                List.of(-34.60, -58.37),
                List.of(-34.61, -58.37),
                List.of(-34.61, -58.38)
        );
        assertFalse(servicioGeocerca.puntoDentroPoligono(-34.50, -58.50, cuadrado));
    }

    @Test
    void poligonoDegeneradoRetornaFalse() {
        assertFalse(servicioGeocerca.puntoDentroPoligono(-34.60, -58.38, List.of()));
        assertFalse(servicioGeocerca.puntoDentroPoligono(-34.60, -58.38,
                List.of(List.of(-34.60, -58.38), List.of(-34.61, -58.37))));
    }
}
