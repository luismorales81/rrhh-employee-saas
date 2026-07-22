package com.rrhh.saas.fichaje.servicio;



import com.rrhh.saas.comun.tenant.ContextoTenant;

import com.rrhh.saas.fichaje.dominio.PoliticaFichajeEmpresa;

import com.rrhh.saas.fichaje.dto.RespuestaPoliticaFichaje;

import com.rrhh.saas.fichaje.dto.SolicitudActualizarPoliticaFichaje;

import com.rrhh.saas.fichaje.repositorio.RepositorioPoliticaFichajeEmpresa;

import com.rrhh.saas.nucleo.dominio.Empresa;

import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;

import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;



@Service

public class ServicioPoliticaFichaje {



    public static final String TEXTO_TRANSPARENCIA_DEFAULT = """

            Qué registramos al fichar:

            • Fecha y hora del fichaje

            • Ubicación GPS en el momento del fichaje

            • Modelo del dispositivo y versión de la app



            Qué NO hacemos:

            • No te rastreamos fuera del fichaje

            • No accedemos a tu galería ni contactos

            • No usamos ubicación en segundo plano

            """;



    private final RepositorioPoliticaFichajeEmpresa repositorio;

    private final ServicioEmpresa servicioEmpresa;

    private final ServicioAuditoria servicioAuditoria;



    public ServicioPoliticaFichaje(RepositorioPoliticaFichajeEmpresa repositorio,

                                   ServicioEmpresa servicioEmpresa,

                                   ServicioAuditoria servicioAuditoria) {

        this.repositorio = repositorio;

        this.servicioEmpresa = servicioEmpresa;

        this.servicioAuditoria = servicioAuditoria;

    }



    @Transactional

    public RespuestaPoliticaFichaje obtenerPolitica() {

        return mapear(obtenerOCrearPolitica());

    }



    @Transactional

    public RespuestaPoliticaFichaje actualizarPolitica(SolicitudActualizarPoliticaFichaje solicitud) {

        PoliticaFichajeEmpresa politica = obtenerOCrearPolitica();

        String textoAnterior = politica.getTextoTransparencia();

        politica.setTextoTransparencia(solicitud.textoTransparencia());

        politica.setPermitirFichajeSinGps(solicitud.permitirFichajeSinGps());

        politica.setPrecisionMaximaMetros(solicitud.precisionMaximaMetros());

        if (!textoAnterior.equals(solicitud.textoTransparencia())) {

            politica.setVersion(politica.getVersion() + 1);

        }

        PoliticaFichajeEmpresa guardada = repositorio.save(politica);

        servicioAuditoria.registrar("ACTUALIZAR", "PoliticaFichajeEmpresa", guardada.getId(), null, null);

        return mapear(guardada);

    }



    /**

     * Usa la transacción actual. Preferir este método dentro de flujos de escritura

     * (carga demo, actualización de configuración) para evitar inserts duplicados.

     */

    @Transactional

    public PoliticaFichajeEmpresa obtenerOCrearPolitica() {

        return buscarOCrearParaEmpresa(ContextoTenant.obtenerEmpresaId(), null);

    }



    /**

     * Abre una transacción independiente para poder crear la política desde métodos

     * marcados como readOnly.

     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)

    public PoliticaFichajeEmpresa obtenerOCrearPoliticaIndependiente() {

        Long empresaId = ContextoTenant.obtenerEmpresaId();

        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)

                .orElseGet(() -> {

                    try {

                        return instanciarPolitica(empresaId, null);

                    } catch (DataIntegrityViolationException ex) {

                        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)

                                .orElseThrow(() -> ex);

                    }

                });

    }



    @Transactional

    public PoliticaFichajeEmpresa crearPoliticaParaEmpresa(Empresa empresa) {

        return buscarOCrearParaEmpresa(empresa.getId(), empresa);

    }



    private PoliticaFichajeEmpresa buscarOCrearParaEmpresa(Long empresaId, Empresa empresaConocida) {

        return repositorio.findByEmpresaIdAndDeletedFalse(empresaId)

                .orElseGet(() -> instanciarPolitica(empresaId, empresaConocida));

    }



    private PoliticaFichajeEmpresa instanciarPolitica(Long empresaId, Empresa empresaConocida) {

        PoliticaFichajeEmpresa politica = new PoliticaFichajeEmpresa();

        politica.setEmpresa(empresaConocida != null ? empresaConocida : servicioEmpresa.obtenerEntidad(empresaId));

        politica.setTextoTransparencia(TEXTO_TRANSPARENCIA_DEFAULT);

        politica.setVersion(1);

        return repositorio.save(politica);

    }



    private RespuestaPoliticaFichaje mapear(PoliticaFichajeEmpresa politica) {

        return new RespuestaPoliticaFichaje(

                politica.getTextoTransparencia(),

                politica.getVersion(),

                politica.isPermitirFichajeSinGps(),

                politica.getPrecisionMaximaMetros()

        );

    }

}


