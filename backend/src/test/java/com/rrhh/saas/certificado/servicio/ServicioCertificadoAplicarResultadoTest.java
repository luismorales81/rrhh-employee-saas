package com.rrhh.saas.certificado.servicio;

import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;
import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.dominio.TipoExamenCertificado;
import com.rrhh.saas.certificado.ia.ResultadoExtraccionGemini;
import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;
import com.rrhh.saas.certificado.repositorio.RepositorioDatosExtraidosCertificado;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioCertificadoAplicarResultadoTest {

    @Mock
    private RepositorioCertificado repositorio;
    @Mock
    private RepositorioDatosExtraidosCertificado repositorioDatos;
    @Mock
    private ServicioEmpleado servicioEmpleado;
    @Mock
    private ServicioAlmacenamiento servicioAlmacenamiento;
    @Mock
    private ServicioEventoCertificado servicioEvento;
    @Mock
    private ServicioConfiguracionAuditoriaMedica servicioConfiguracion;
    @Mock
    private ServicioProcesamientoCertificado servicioProcesamiento;

    @Test
    void aplicarResultadoVersionaExtraccionYTransicionaAEnRevision() {
        ServicioCertificado servicio = new ServicioCertificado(
                repositorio, repositorioDatos, servicioEmpleado, null,
                servicioAlmacenamiento, servicioEvento, servicioConfiguracion, servicioProcesamiento);

        Certificado certificado = certificadoEnProcesamiento();
        when(repositorio.findById(1L)).thenReturn(Optional.of(certificado));

        DatosExtraidosCertificado anterior = new DatosExtraidosCertificado();
        anterior.setNumeroVersion(2);
        anterior.setEsVigente(true);
        when(repositorioDatos.findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(1L))
                .thenReturn(Optional.of(anterior));
        when(repositorioDatos.findByCertificadoIdOrderByNumeroVersionDesc(1L))
                .thenReturn(List.of(anterior));

        when(repositorioDatos.save(any(DatosExtraidosCertificado.class))).thenAnswer(inv -> {
            DatosExtraidosCertificado datos = inv.getArgument(0);
            if (datos.getId() == null) {
                datos.setId(100L);
            }
            return datos;
        });
        when(repositorio.buscarHistorialPeriodo(any(), any(), any(), any())).thenReturn(List.of());
        when(servicioConfiguracion.obtenerEntidad(10L)).thenReturn(configPorDefecto());
        when(repositorio.save(any(Certificado.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultadoExtraccionGemini resultado = new ResultadoExtraccionGemini(
                "Fractura", 10, "Dr. López", "MN-55", LocalDate.of(2026, 7, 1), 0.95f, null);

        servicio.aplicarResultadoExtraccion(1L, resultado, null);

        assertFalse(anterior.isEsVigente());
        assertEquals(EstadoCertificado.EN_REVISION, certificado.getEstado());

        ArgumentCaptor<DatosExtraidosCertificado> captor = ArgumentCaptor.forClass(DatosExtraidosCertificado.class);
        verify(repositorioDatos, org.mockito.Mockito.times(2)).save(captor.capture());
        DatosExtraidosCertificado nuevo = captor.getAllValues().stream()
                .filter(d -> d.getNumeroVersion() == 3)
                .findFirst()
                .orElseThrow();
        assertTrue(nuevo.isEsVigente());
        assertEquals("Fractura", nuevo.getDiagnostico());
        assertEquals(10, nuevo.getReposoDias());
    }

    private static Certificado certificadoEnProcesamiento() {
        Empresa empresa = new Empresa();
        empresa.setId(10L);

        Empleado empleado = new Empleado();
        empleado.setId(20L);

        Certificado certificado = new Certificado();
        certificado.setId(1L);
        certificado.setEmpresa(empresa);
        certificado.setEmpleado(empleado);
        certificado.setEstado(EstadoCertificado.EN_PROCESAMIENTO);
        certificado.setTipoExamen(TipoExamenCertificado.AUSENTISMO);
        return certificado;
    }

    private static ConfiguracionAuditoriaMedica configPorDefecto() {
        ConfiguracionAuditoriaMedica config = new ConfiguracionAuditoriaMedica();
        config.setDiasReposoAlertaLeve(5);
        config.setMaxLicenciasCortasPeriodo(3);
        config.setPeriodoEvaluacionDias(90);
        return config;
    }
}
