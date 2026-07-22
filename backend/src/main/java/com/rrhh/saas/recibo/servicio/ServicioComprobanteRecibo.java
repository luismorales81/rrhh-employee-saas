package com.rrhh.saas.recibo.servicio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.EventoFirmaRecibo;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.dominio.TipoEventoFirmaRecibo;
import com.rrhh.saas.recibo.repositorio.RepositorioEventoFirmaRecibo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioComprobanteRecibo {

    private final RepositorioEventoFirmaRecibo repositorioEventoFirma;
    private final ObjectMapper objectMapper;

    public ServicioComprobanteRecibo(RepositorioEventoFirmaRecibo repositorioEventoFirma) {
        this.repositorioEventoFirma = repositorioEventoFirma;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional(readOnly = true)
    public byte[] generarComprobanteJson(Recibo recibo) {
        if (recibo.getEstado() != EstadoRecibo.FIRMADO && recibo.getEstado() != EstadoRecibo.DISCONFORMIDAD) {
            throw new ExcepcionNegocio("El comprobante solo está disponible para recibos firmados", 400);
        }

        List<EventoFirmaRecibo> eventos = repositorioEventoFirma
                .findByReciboIdOrderByTimestampEventoAsc(recibo.getId());
        EventoFirmaRecibo eventoFirma = eventos.stream()
                .filter(e -> e.getTipo() == TipoEventoFirmaRecibo.CONFIRMADO
                        || e.getTipo() == TipoEventoFirmaRecibo.DISCONFORMIDAD)
                .reduce((primero, segundo) -> segundo)
                .orElseThrow(() -> new ExcepcionNegocio("No hay evento de firma registrado", 404));

        Empleado empleado = recibo.getEmpleado();
        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("tipoDocumento", "COMPROBANTE_FIRMA_RECIBO");
        comprobante.put("reciboId", recibo.getId());
        comprobante.put("periodo", recibo.getPeriodo());
        comprobante.put("titulo", recibo.getTitulo());
        comprobante.put("version", recibo.getVersion());
        comprobante.put("estado", recibo.getEstado().name());
        comprobante.put("montoNeto", recibo.getMontoNeto());
        comprobante.put("montoBruto", recibo.getMontoBruto());
        comprobante.put("hashPdf", recibo.getHashPdf());
        comprobante.put("empleado", Map.of(
                "id", empleado.getId(),
                "nombre", empleado.getNombre() + " " + empleado.getApellido(),
                "cuil", empleado.getCuil() != null ? empleado.getCuil() : "",
                "dni", empleado.getDni() != null ? empleado.getDni() : ""
        ));
        comprobante.put("eventoFirma", Map.of(
                "id", eventoFirma.getId(),
                "tipo", eventoFirma.getTipo().name(),
                "timestamp", eventoFirma.getTimestampEvento(),
                "hashPdfEnFirma", eventoFirma.getHashPdfEnFirma(),
                "comentario", eventoFirma.getComentario() != null ? eventoFirma.getComentario() : ""
        ));
        comprobante.put("fechaFirma", recibo.getFechaFirma());
        comprobante.put("leyenda",
                "Constancia de recepción conforme Ley 27.802 y firma electrónica simple Ley 25.506.");

        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(comprobante)
                    .getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ExcepcionNegocio("No se pudo generar el comprobante", 500);
        }
    }

    public String construirNombreArchivo(Recibo recibo) {
        return "comprobante-recibo-" + recibo.getPeriodo() + "-v" + recibo.getVersion() + ".json";
    }
}
