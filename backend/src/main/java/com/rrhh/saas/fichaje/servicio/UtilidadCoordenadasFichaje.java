package com.rrhh.saas.fichaje.servicio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UtilidadCoordenadasFichaje {

    private static final TypeReference<List<List<Double>>> TIPO_COORDENADAS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public UtilidadCoordenadasFichaje(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serializar(List<List<Double>> coordenadas) {
        validarPoligono(coordenadas);
        try {
            return objectMapper.writeValueAsString(coordenadas);
        } catch (JsonProcessingException e) {
            throw new ExcepcionNegocio("Coordenadas de zona inválidas", 400);
        }
    }

    public List<List<Double>> deserializar(String json) {
        try {
            List<List<Double>> coordenadas = objectMapper.readValue(json, TIPO_COORDENADAS);
            validarPoligono(coordenadas);
            return coordenadas;
        } catch (JsonProcessingException e) {
            throw new ExcepcionNegocio("Coordenadas de zona corruptas", 500);
        }
    }

    public void validarPoligono(List<List<Double>> coordenadas) {
        if (coordenadas == null || coordenadas.size() < 3) {
            throw new ExcepcionNegocio("El polígono debe tener al menos 3 vértices", 400);
        }
        for (List<Double> punto : coordenadas) {
            if (punto == null || punto.size() < 2) {
                throw new ExcepcionNegocio("Cada vértice debe tener latitud y longitud", 400);
            }
            double lat = punto.get(0);
            double lng = punto.get(1);
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                throw new ExcepcionNegocio("Coordenadas fuera de rango válido", 400);
            }
        }
    }
}
