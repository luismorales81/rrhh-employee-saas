package com.rrhh.saas.certificado.servicio;

import com.rrhh.saas.archivo.dominio.ArchivoAlmacenado;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.ia.ConfiguracionVertexTenant;
import com.rrhh.saas.certificado.ia.GeminiExtraccionService;
import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;
import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioProcesamientoCertificadoTest {

    @Mock
    private RepositorioCertificado repositorio;
    @Mock
    private ServicioAlmacenamiento servicioAlmacenamiento;
    @Mock
    private ServicioConfiguracionAuditoriaMedica servicioConfiguracion;
    @Mock
    private GeminiExtraccionService geminiExtraccionService;
    @Mock
    private ServicioCertificado servicioCertificado;

    @InjectMocks
    private ServicioProcesamientoCertificado servicioProcesamiento;

    @Test
    void procesaExtraccionExitosaYDelegaResultado() {
        Certificado certificado = certificadoEnProcesamiento(1L, 10L);
        when(repositorio.findById(1L)).thenReturn(Optional.of(certificado));

        ConfiguracionVertexTenant config = new ConfiguracionVertexTenant(
                "proyecto", "us-central1", "gemini-2.0-flash", "{}");
        when(servicioConfiguracion.resolverConfigVertex(10L)).thenReturn(config);

        ArchivoAlmacenado archivo = new ArchivoAlmacenado();
        archivo.setTipoMime("application/pdf");
        when(servicioAlmacenamiento.obtenerEntidad(99L)).thenReturn(archivo);
        when(servicioAlmacenamiento.leerBytes(99L)).thenReturn(new byte[]{1, 2, 3});

        ResultadoExtraccionGemini resultado = new ResultadoExtraccionGemini(
                "Gripe", 2, "Dr. Test", "MN-1", LocalDate.now(), 0.9f, null);
        when(geminiExtraccionService.extraer(any(), eq("application/pdf"), eq(config)))
                .thenReturn(resultado);

        servicioProcesamiento.procesarExtraccionAsync(1L, 10L);

        verify(servicioCertificado).marcarIntentoExtraccion(1L);
        verify(servicioCertificado).aplicarResultadoExtraccion(1L, resultado, null);
        verify(servicioCertificado, never()).registrarFalloExtraccion(any(), any(), any());
    }

    @Test
    void ignoraCertificadoQueNoEstaEnProcesamiento() {
        Certificado certificado = certificadoEnProcesamiento(2L, 10L);
        certificado.setEstado(EstadoCertificado.EN_REVISION);
        when(repositorio.findById(2L)).thenReturn(Optional.of(certificado));

        servicioProcesamiento.procesarExtraccionAsync(2L, 10L);

        verify(servicioCertificado, never()).marcarIntentoExtraccion(any());
        verify(geminiExtraccionService, never()).extraer(any(), any(), any());
    }

    @Test
    void registraFalloCuandoGeminiLanzaExcepcion() {
        Certificado certificado = certificadoEnProcesamiento(3L, 10L);
        when(repositorio.findById(3L)).thenReturn(Optional.of(certificado));

        ConfiguracionVertexTenant config = new ConfiguracionVertexTenant(
                "proyecto", "us-central1", "gemini-2.0-flash", "{}");
        when(servicioConfiguracion.resolverConfigVertex(10L)).thenReturn(config);

        ArchivoAlmacenado archivo = new ArchivoAlmacenado();
        archivo.setTipoMime("image/jpeg");
        when(servicioAlmacenamiento.obtenerEntidad(99L)).thenReturn(archivo);
        when(servicioAlmacenamiento.leerBytes(99L)).thenReturn(new byte[]{1});
        when(geminiExtraccionService.extraer(any(), any(), any()))
                .thenThrow(new RuntimeException("timeout Vertex"));

        servicioProcesamiento.procesarExtraccionAsync(3L, 10L);

        verify(servicioCertificado).registrarFalloExtraccion(eq(3L), any(), eq(null));
        verify(servicioCertificado, never()).aplicarResultadoExtraccion(any(), any(), any());
    }

    private static Certificado certificadoEnProcesamiento(Long id, Long empresaId) {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);

        Certificado certificado = new Certificado();
        certificado.setId(id);
        certificado.setEmpresa(empresa);
        certificado.setEstado(EstadoCertificado.EN_PROCESAMIENTO);
        certificado.setArchivoId(99L);
        return certificado;
    }
}
