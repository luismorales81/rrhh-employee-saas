package com.rrhh.saas.recibo.servicio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.servicio.ServicioUsuario;
import com.rrhh.saas.recibo.dominio.Recibo;
import com.rrhh.saas.recibo.dto.RespuestaEventoFirmaRecibo;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServicioExpedienteRecibo {

    private final ServicioRecibo servicioRecibo;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioUsuario servicioUsuario;
    private final ObjectMapper objectMapper;

    public ServicioExpedienteRecibo(ServicioRecibo servicioRecibo,
                                    ServicioAlmacenamiento servicioAlmacenamiento,
                                    ServicioUsuario servicioUsuario) {
        this.servicioRecibo = servicioRecibo;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioUsuario = servicioUsuario;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional(readOnly = true)
    public ResultadoExpedienteRecibo exportar(Long reciboId) {
        Recibo recibo = servicioRecibo.obtenerReciboEmpresaEntidad(reciboId);
        byte[] contenido = generarBytes(recibo);
        return new ResultadoExpedienteRecibo(contenido, construirNombreArchivo(recibo));
    }

    @Transactional(readOnly = true)
    public byte[] generarExpediente(Long reciboId) {
        Recibo recibo = servicioRecibo.obtenerReciboEmpresaEntidad(reciboId);
        return generarBytes(recibo);
    }

    private byte[] generarBytes(Recibo recibo) {
        if (recibo.getArchivo() == null) {
            throw new ExcepcionNegocio("El recibo no tiene PDF almacenado para el expediente", 400);
        }

        Empleado empleado = recibo.getEmpleado();
        List<RespuestaEventoFirmaRecibo> eventos = servicioRecibo.listarEventosFirma(recibo.getId());
        Map<String, Object> manifest = construirManifest(recibo, empleado, eventos);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(salida)) {
                agregarPdf(zip, recibo);
                agregarJson(zip, "manifest.json", manifest);
                agregarJson(zip, "eventos.json", eventos);
            }
            return salida.toByteArray();
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo generar el expediente");
        }
    }

    public record ResultadoExpedienteRecibo(byte[] contenido, String nombreArchivo) {
    }

    public String construirNombreArchivo(Recibo recibo) {
        Empleado empleado = recibo.getEmpleado();
        String apellido = empleado.getApellido().replaceAll("[^a-zA-Z0-9]", "");
        return "expediente-" + recibo.getPeriodo() + "-" + apellido + ".zip";
    }

    private Map<String, Object> construirManifest(Recibo recibo, Empleado empleado,
                                                  List<RespuestaEventoFirmaRecibo> eventos) {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("versionExpediente", "1.0");
        manifest.put("reciboId", recibo.getId());
        manifest.put("periodo", recibo.getPeriodo());
        manifest.put("titulo", recibo.getTitulo());
        manifest.put("hashPdfSha256", recibo.getHashPdf());
        manifest.put("estado", recibo.getEstado().name());
        manifest.put("fechaPublicacionUtc", formatearUtc(recibo.getFechaPublicacion()));
        manifest.put("fechaFirmaUtc", formatearUtc(recibo.getFechaFirma()));
        manifest.put("montoNeto", recibo.getMontoNeto());
        manifest.put("montoBruto", recibo.getMontoBruto());

        Map<String, Object> datosEmpleado = new LinkedHashMap<>();
        datosEmpleado.put("id", empleado.getId());
        datosEmpleado.put("nombre", empleado.getNombre());
        datosEmpleado.put("apellido", empleado.getApellido());
        datosEmpleado.put("cuil", empleado.getCuil());
        datosEmpleado.put("dni", empleado.getDni());
        datosEmpleado.put("numeroEmpleado", empleado.getNumeroEmpleado());
        manifest.put("empleado", datosEmpleado);

        Map<String, Object> publicador = new LinkedHashMap<>();
        if (recibo.getPublicadoPorId() != null) {
            try {
                Usuario usuario = servicioUsuario.obtenerEntidad(recibo.getPublicadoPorId());
                publicador.put("id", usuario.getId());
                publicador.put("email", usuario.getEmail());
            } catch (ExcepcionNegocio ignored) {
                publicador.put("id", recibo.getPublicadoPorId());
            }
        }
        manifest.put("publicadoPor", publicador);
        manifest.put("cantidadEventos", eventos.size());
        return manifest;
    }

    private void agregarPdf(ZipOutputStream zip, Recibo recibo) throws IOException {
        Resource recurso = servicioAlmacenamiento.obtenerContenido(recibo.getArchivo().getId());
        zip.putNextEntry(new ZipEntry("recibo.pdf"));
        try (var input = recurso.getInputStream()) {
            input.transferTo(zip);
        }
        zip.closeEntry();
    }

    private void agregarJson(ZipOutputStream zip, String nombre, Object datos) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(datos));
        zip.closeEntry();
    }

    private String formatearUtc(java.time.LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
