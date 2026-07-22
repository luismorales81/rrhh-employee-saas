package com.rrhh.saas.certificado.validacion;

import com.rrhh.saas.certificado.dominio.EstadoCertificado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTransicionEstadoCertificadoTest {

    @Test
    void permiteCargadoAEnProcesamiento() {
        assertTrue(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.CARGADO, EstadoCertificado.EN_PROCESAMIENTO));
    }

    @Test
    void rechazaValidadoARechazado() {
        assertFalse(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.VALIDADO, EstadoCertificado.RECHAZADO));
    }

    @Test
    void permiteEnRevisionAValidado() {
        assertTrue(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.EN_REVISION, EstadoCertificado.VALIDADO));
    }

    @Test
    void permiteReprocesarDesdeEnRevision() {
        assertTrue(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.EN_REVISION, EstadoCertificado.EN_PROCESAMIENTO));
    }

    @Test
    void permiteReprocesarDesdeExtraido() {
        assertTrue(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.EXTRAIDO, EstadoCertificado.EN_PROCESAMIENTO));
    }

    @Test
    void permiteReprocesarDesdeRequiereAclaracion() {
        assertTrue(ValidadorTransicionEstadoCertificado.validarTransicionEstado(
                EstadoCertificado.REQUIERE_ACLARACION, EstadoCertificado.EN_PROCESAMIENTO));
    }
}
