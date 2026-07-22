package com.rrhh.saas.certificado.ia;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@ConditionalOnProperty(name = "app.certificados.ia.simulada", havingValue = "true")
public class ServicioExtraccionGeminiSimulado implements GeminiExtraccionService {

    @Override
    public ResultadoExtraccionGemini extraer(byte[] archivo, String tipoMime, ConfiguracionVertexTenant config) {
        if (archivo == null || archivo.length == 0) {
            return ResultadoExtraccionGemini.errorDocumento("documento_no_reconocido");
        }
        return new ResultadoExtraccionGemini(
                "Gripe simulada",
                3,
                "Dr. Simulado",
                "MN-12345",
                LocalDate.now().minusDays(1),
                0.92f,
                null
        );
    }
}
