package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import com.rrhh.saas.vacacion.dominio.SaldoVacaciones;
import com.rrhh.saas.vacacion.validacion.CalculadorAntiguedadVacaciones;
import com.rrhh.saas.vacacion.validacion.CalculadorDiasProporcionales;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ServicioRecalculoSaldoVacaciones {

    public void aplicarRecalculo(SaldoVacaciones saldo, Empleado empleado, PoliticaVacaciones politica, int anio) {
        int antiguedad = CalculadorAntiguedadVacaciones.calcularAniosAntiguedad(
                empleado.getFechaIngreso(), anio);
        int diasAnuales = CalculadorVacaciones.calcularDiasOtorgados(politica, antiguedad);
        int diasTotales = CalculadorDiasProporcionales.calcularDiasProporcionales(
                empleado.getFechaIngreso(), anio, diasAnuales);

        BigDecimal pendientes = saldo.getDiasPendientes() != null ? saldo.getDiasPendientes() : BigDecimal.ZERO;
        BigDecimal tomados = saldo.getDiasTomados() != null ? saldo.getDiasTomados() : BigDecimal.ZERO;
        BigDecimal ajuste = saldo.getAjusteManual() != null ? saldo.getAjusteManual() : BigDecimal.ZERO;

        saldo.setEmpleado(empleado);
        saldo.setAnio(anio);
        saldo.setAntiguedadAnios(antiguedad);
        saldo.setPolitica(politica);
        saldo.setDiasTotales(BigDecimal.valueOf(diasTotales));
        saldo.setDiasTomados(tomados);
        saldo.setDiasPendientes(pendientes);
        saldo.setAjusteManual(ajuste);
        saldo.recalcularDisponibles();
    }

    public SaldoVacaciones crearSaldoInicial(Empleado empleado, PoliticaVacaciones politica) {
        int anio = LocalDate.now().getYear();
        SaldoVacaciones saldo = new SaldoVacaciones();
        saldo.setDiasTomados(BigDecimal.ZERO);
        saldo.setDiasPendientes(BigDecimal.ZERO);
        saldo.setAjusteManual(BigDecimal.ZERO);
        aplicarRecalculo(saldo, empleado, politica, anio);
        return saldo;
    }
}
