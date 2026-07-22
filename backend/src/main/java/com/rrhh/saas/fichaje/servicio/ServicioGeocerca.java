package com.rrhh.saas.fichaje.servicio;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioGeocerca {

    /**
     * Determina si un punto (lat, lng) está dentro de un polígono usando ray casting.
     * Cada vértice es [latitud, longitud].
     */
    public boolean puntoDentroPoligono(double latitud, double longitud, List<List<Double>> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        int cantidad = vertices.size();
        boolean dentro = false;
        for (int i = 0, j = cantidad - 1; i < cantidad; j = i++) {
            double latI = vertices.get(i).get(0);
            double lngI = vertices.get(i).get(1);
            double latJ = vertices.get(j).get(0);
            double lngJ = vertices.get(j).get(1);

            boolean cruzaLongitud = (lngI > longitud) != (lngJ > longitud);
            if (cruzaLongitud) {
                double latCruce = (latJ - latI) * (longitud - lngI) / (lngJ - lngI) + latI;
                if (latitud < latCruce) {
                    dentro = !dentro;
                }
            }
        }
        return dentro;
    }
}
