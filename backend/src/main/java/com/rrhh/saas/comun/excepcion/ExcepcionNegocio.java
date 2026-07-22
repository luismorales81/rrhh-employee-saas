package com.rrhh.saas.comun.excepcion;

public class ExcepcionNegocio extends RuntimeException {

    private final int codigoHttp;

    public ExcepcionNegocio(String mensaje) {
        this(mensaje, 400);
    }

    public ExcepcionNegocio(String mensaje, int codigoHttp) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}
