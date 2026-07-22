package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.RrhhSaasApplication;
import com.rrhh.saas.archivo.dto.RespuestaArchivoAlmacenado;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioEmpresa;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = RrhhSaasApplication.class)
@ActiveProfiles("test")
class ServicioReciboInmutabilidadTest {

    @Autowired
    private ServicioRecibo servicioRecibo;

    @Autowired
    private ServicioAlmacenamiento servicioAlmacenamiento;

    @Autowired
    private RepositorioRecibo repositorioRecibo;

    @Autowired
    private RepositorioEmpleado repositorioEmpleado;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    private Long empresaId;
    private Long usuarioRrhhId;

    @BeforeEach
    void establecerContexto() {
        Empresa empresa = repositorioEmpresa.findByDeletedFalse().stream()
                .filter(e -> "Demo SA".equals(e.getNombre()))
                .findFirst()
                .orElseThrow();
        empresaId = empresa.getId();
        usuarioRrhhId = repositorioUsuario.findByEmailAndDeletedFalse("rrhh@demo.local")
                .orElseThrow()
                .getId();
        ContextoTenant.establecerEmpresaId(empresaId);
        ContextoTenant.establecerUsuarioId(usuarioRrhhId);
    }

    @AfterEach
    void limpiarContexto() {
        ContextoTenant.limpiar();
    }

    @Test
    void rechazaCargaSinArchivoId() {
        var empleado = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).get(0);
        assertThrows(ExcepcionNegocio.class, () -> servicioRecibo.publicarRecibo(
                empleado.getId(),
                "2099-01",
                "Recibo prueba",
                null,
                null,
                null
        ));
    }

    @Test
    void rechazaEliminarReciboFirmado() {
        var empleado = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .filter(e -> "EMP-002".equals(e.getNumeroEmpleado()))
                .findFirst()
                .orElseThrow();

        RespuestaArchivoAlmacenado archivo = servicioAlmacenamiento.guardarDesdeBase64(
                "recibo-inmutabilidad.pdf", "application/pdf", "%PDF-test%".getBytes());

        String periodo = "inmut-" + YearMonth.now().plusYears(5);
        var recibo = servicioRecibo.publicarRecibo(
                empleado.getId(), periodo, "Recibo inmutabilidad", archivo.id(), null, null);

        Recibo entidad = repositorioRecibo.findById(recibo.id()).orElseThrow();
        entidad.setEstado(EstadoRecibo.FIRMADO);
        repositorioRecibo.save(entidad);

        assertThrows(ExcepcionNegocio.class, () -> servicioRecibo.eliminarLogico(recibo.id()));
    }

    @Test
    void rechazaEliminarArchivoReferenciadoPorRecibo() {
        var empleado = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).get(0);
        RespuestaArchivoAlmacenado archivo = servicioAlmacenamiento.guardarDesdeBase64(
                "recibo-archivo-ref.pdf", "application/pdf", "%PDF-ref%".getBytes());

        servicioRecibo.publicarRecibo(
                empleado.getId(),
                "inm-" + (System.currentTimeMillis() % 100000),
                "Recibo archivo ref",
                archivo.id(),
                BigDecimal.TEN,
                BigDecimal.TEN
        );

        assertThrows(ExcepcionNegocio.class, () -> servicioAlmacenamiento.eliminarLogico(archivo.id()));
    }

    @Test
    void validaCamposInmutablesTrasPublicacion() {
        var empleado = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).get(0);
        RespuestaArchivoAlmacenado archivo = servicioAlmacenamiento.guardarDesdeBase64(
                "recibo-campos.pdf", "application/pdf", "%PDF-campos%".getBytes());

        var respuesta = servicioRecibo.publicarRecibo(
                empleado.getId(),
                "inm-c-" + (System.currentTimeMillis() % 100000),
                "Recibo campos",
                archivo.id(),
                null,
                null
        );

        Recibo original = repositorioRecibo.findById(respuesta.id()).orElseThrow();
        Recibo modificado = repositorioRecibo.findById(respuesta.id()).orElseThrow();
        modificado.setPeriodo("9999-12");

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> servicioRecibo.validarCamposInmutables(original, modificado));
        assertEquals(400, ex.getCodigoHttp());
    }
}
