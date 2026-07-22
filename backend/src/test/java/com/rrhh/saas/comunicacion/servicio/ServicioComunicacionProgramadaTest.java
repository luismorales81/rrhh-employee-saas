package com.rrhh.saas.comunicacion.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.comunicacion.dominio.Comunicacion;
import com.rrhh.saas.comunicacion.dominio.EstadoComunicacion;
import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;
import com.rrhh.saas.comunicacion.dto.SolicitudPublicarComunicacion;
import com.rrhh.saas.comunicacion.repositorio.RepositorioComunicacion;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioComunicacionProgramadaTest {

    @Autowired
    private ServicioComunicacion servicioComunicacion;

    @Autowired
    private RepositorioComunicacion repositorioComunicacion;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @BeforeEach
    void establecerContexto() {
        Empresa empresa = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> "Demo SA".equals(e.getNombre()))
                .findFirst()
                .orElseThrow();
        ContextoTenant.establecerEmpresaId(empresa.getId());
    }

    @AfterEach
    void limpiarContexto() {
        ContextoTenant.limpiar();
    }

    @Test
    void programarYPublicarPendientes() {
        var solicitud = new SolicitudPublicarComunicacion(
                "Comunicación programada test",
                "Mensaje de prueba",
                TipoDestinatarioComunicacion.EMPRESA,
                null,
                false,
                LocalDateTime.now().minusMinutes(1)
        );

        var respuesta = servicioComunicacion.programar(solicitud);
        assertEquals(EstadoComunicacion.PROGRAMADA, respuesta.estado());

        servicioComunicacion.publicarPendientes();

        Comunicacion publicada = repositorioComunicacion.findById(respuesta.id()).orElseThrow();
        assertEquals(EstadoComunicacion.PUBLICADA, publicada.getEstado());
        assertTrue(publicada.getFechaPublicacion() != null);

        List<Comunicacion> destinatariosCreados = repositorioComunicacion.findByEmpresaIdAndEstadoAndDeletedFalseOrderByFechaPublicacionProgramadaAsc(
                ContextoTenant.obtenerEmpresaId(), EstadoComunicacion.PROGRAMADA);
        assertTrue(destinatariosCreados.stream().noneMatch(c -> c.getId().equals(respuesta.id())));
    }
}
