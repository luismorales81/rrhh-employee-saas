package com.rrhh.saas.certificado.servicio;

import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.certificado.dominio.Certificado;
import com.rrhh.saas.certificado.dominio.ConfiguracionAuditoriaMedica;
import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import com.rrhh.saas.certificado.repositorio.RepositorioCertificado;
import com.rrhh.saas.certificado.repositorio.RepositorioDatosExtraidosCertificado;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioCertificadoReprocesarTest {

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

    private ServicioCertificado servicioCertificado;

    @BeforeEach
    void configurar() {
        servicioCertificado = new ServicioCertificado(
                repositorio,
                repositorioDatos,
                servicioEmpleado,
                null,
                servicioAlmacenamiento,
                servicioEvento,
                servicioConfiguracion,
                servicioProcesamiento
        );
        ContextoTenant.establecerEmpresaId(10L);
        ContextoTenant.establecerUsuarioId(5L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "auditor",
                        null,
                        List.of(new SimpleGrantedAuthority("certificado.validar"))));
    }

    @AfterEach
    void limpiar() {
        ContextoTenant.limpiar();
        SecurityContextHolder.clearContext();
    }

    @Test
    void reprocesarReseteaIntentosYDisparaExtraccionAsync() {
        Certificado certificado = certificadoBase();
        certificado.setEstado(EstadoCertificado.EN_REVISION);
        certificado.setIntentosExtraccion(2);
        certificado.setUltimoErrorExtraccion("error previo");

        when(repositorio.findByIdAndEmpresaIdAndDeletedFalseConDatos(1L, 10L))
                .thenReturn(Optional.of(certificado));
        when(repositorioDatos.findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        ConfiguracionAuditoriaMedica config = new ConfiguracionAuditoriaMedica();
        config.setProyectoGcp("proyecto");
        config.setRegionVertex("us-central1");
        config.setModeloGemini("gemini-2.0-flash");
        config.setCredencialesServicioCifrada("cifrado");
        when(servicioConfiguracion.obtenerEntidad(10L)).thenReturn(config);
        when(repositorio.save(any(Certificado.class))).thenAnswer(inv -> inv.getArgument(0));

        servicioCertificado.reprocesar(1L, null);

        assertEquals(0, certificado.getIntentosExtraccion());
        assertEquals(EstadoCertificado.EN_PROCESAMIENTO, certificado.getEstado());
        verify(servicioProcesamiento).procesarExtraccionAsync(1L, 10L);
        verify(servicioEvento).registrar(eq(certificado), eq(EstadoCertificado.EN_REVISION),
                eq(EstadoCertificado.EN_PROCESAMIENTO), eq(5L), any(), any());
    }

    private static Certificado certificadoBase() {
        Empresa empresa = new Empresa();
        empresa.setId(10L);

        Empleado empleado = new Empleado();
        empleado.setId(20L);

        Certificado certificado = new Certificado();
        certificado.setId(1L);
        certificado.setEmpresa(empresa);
        certificado.setEmpleado(empleado);
        certificado.setArchivoUrl("/api/archivos/99/contenido");
        return certificado;
    }
}
