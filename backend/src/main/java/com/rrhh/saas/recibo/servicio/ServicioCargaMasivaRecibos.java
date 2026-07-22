package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.archivo.dto.RespuestaArchivoAlmacenado;
import com.rrhh.saas.archivo.servicio.ServicioAlmacenamiento;
import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.recibo.dto.FilaAnalisisCargaMasiva;
import com.rrhh.saas.recibo.dto.FilaResultadoCargaMasiva;
import com.rrhh.saas.recibo.dto.ReciboCargadoMasivo;
import com.rrhh.saas.recibo.dto.RespuestaAnalisisCargaMasiva;
import com.rrhh.saas.recibo.dto.RespuestaCargaMasivaRecibos;
import com.rrhh.saas.recibo.dto.RespuestaRecibo;
import com.rrhh.saas.recibo.repositorio.RepositorioRecibo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ServicioCargaMasivaRecibos {

    private static final int MAX_ENTRADAS_ZIP = 500;
    private static final long MAX_BYTES_DESCOMPRIMIDOS = 50L * 1024 * 1024;
    private static final int MAX_RATIO_COMPRESION = 100;

    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final ServicioRecibo servicioRecibo;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioRecibo repositorioRecibo;

    public ServicioCargaMasivaRecibos(ServicioAlmacenamiento servicioAlmacenamiento,
                                      ServicioRecibo servicioRecibo,
                                      RepositorioEmpleado repositorioEmpleado,
                                      RepositorioRecibo repositorioRecibo) {
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.servicioRecibo = servicioRecibo;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioRecibo = repositorioRecibo;
    }

    @Transactional(readOnly = true)
    public RespuestaAnalisisCargaMasiva analizar(MultipartFile archivoZip, String periodo) {
        List<EntradaPdfZip> entradas = extraerPdfs(archivoZip);
        List<Empleado> empleados = listarEmpleadosActivos();
        List<FilaAnalisisCargaMasiva> filas = new ArrayList<>();
        int emparejados = 0;
        int conError = 0;

        for (EntradaPdfZip entrada : entradas) {
            var resultado = EmparejadorArchivoEmpleado.emparejar(entrada.nombre(), empleados);
            if (resultado.esExitoso()) {
                Empleado empleado = resultado.empleado().orElseThrow();
                String mensaje = mensajeEstadoEmparejamiento(empleado.getId(), periodo);
                if (mensaje == null) {
                    filas.add(new FilaAnalisisCargaMasiva(
                            entrada.nombre(), empleado.getId(),
                            empleado.getNombre() + " " + empleado.getApellido(),
                            "LISTO", null));
                    emparejados++;
                } else {
                    filas.add(new FilaAnalisisCargaMasiva(
                            entrada.nombre(), empleado.getId(),
                            empleado.getNombre() + " " + empleado.getApellido(),
                            "OMITIDO", mensaje));
                    conError++;
                }
            } else {
                filas.add(new FilaAnalisisCargaMasiva(
                        entrada.nombre(), null, null, "ERROR",
                        resultado.error() != null ? resultado.error() : "Sin coincidencia"));
                conError++;
            }
        }

        return new RespuestaAnalisisCargaMasiva(filas, entradas.size(), emparejados, conError);
    }

    @Transactional
    public RespuestaCargaMasivaRecibos ejecutar(MultipartFile archivoZip, String periodo, String tituloBase,
                                                BigDecimal montoNeto, BigDecimal montoBruto) {
        List<EntradaPdfZip> entradas = extraerPdfs(archivoZip);
        List<Empleado> empleados = listarEmpleadosActivos();
        List<ReciboCargadoMasivo> cargados = new ArrayList<>();
        List<FilaResultadoCargaMasiva> omitidos = new ArrayList<>();
        List<FilaResultadoCargaMasiva> errores = new ArrayList<>();
        Set<Long> empleadosProcesados = new HashSet<>();

        for (EntradaPdfZip entrada : entradas) {
            var resultado = EmparejadorArchivoEmpleado.emparejar(entrada.nombre(), empleados);
            if (!resultado.esExitoso()) {
                errores.add(new FilaResultadoCargaMasiva(
                        entrada.nombre(),
                        resultado.error() != null ? resultado.error() : "Sin coincidencia"));
                continue;
            }

            Empleado empleado = resultado.empleado().orElseThrow();
            if (empleadosProcesados.contains(empleado.getId())) {
                omitidos.add(new FilaResultadoCargaMasiva(entrada.nombre(), "Empleado duplicado en el ZIP"));
                continue;
            }

            String motivoOmitido = mensajeEstadoEmparejamiento(empleado.getId(), periodo);
            if (motivoOmitido != null) {
                omitidos.add(new FilaResultadoCargaMasiva(entrada.nombre(), motivoOmitido));
                continue;
            }

            try {
                RespuestaArchivoAlmacenado archivo = servicioAlmacenamiento.guardarDesdeBase64(
                        entrada.nombre(), "application/pdf", entrada.contenido());
                String titulo = tituloBase + " — " + empleado.getApellido() + ", " + empleado.getNombre();
                RespuestaRecibo recibo = servicioRecibo.publicarRecibo(
                        empleado.getId(), periodo, titulo, archivo.id(), montoNeto, montoBruto);
                cargados.add(new ReciboCargadoMasivo(
                        empleado.getId(),
                        empleado.getNombre() + " " + empleado.getApellido(),
                        recibo.id(),
                        entrada.nombre()));
                empleadosProcesados.add(empleado.getId());
            } catch (ExcepcionNegocio ex) {
                errores.add(new FilaResultadoCargaMasiva(entrada.nombre(), ex.getMessage()));
            }
        }

        return new RespuestaCargaMasivaRecibos(cargados, omitidos, errores);
    }

    private String mensajeEstadoEmparejamiento(Long empleadoId, String periodo) {
        if (repositorioRecibo.existsByEmpleadoIdAndPeriodoAndVigenteTrueAndDeletedFalse(empleadoId, periodo)) {
            return "Ya existe recibo para el período " + periodo;
        }
        return null;
    }

    private List<Empleado> listarEmpleadosActivos() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId);
    }

    private List<EntradaPdfZip> extraerPdfs(MultipartFile archivoZip) {
        if (archivoZip == null || archivoZip.isEmpty()) {
            throw new ExcepcionNegocio("Debe adjuntar un archivo ZIP");
        }
        String nombre = archivoZip.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".zip")) {
            throw new ExcepcionNegocio("El archivo debe ser un ZIP");
        }

        List<EntradaPdfZip> entradas = new ArrayList<>();
        long bytesDescomprimidos = 0;
        try (ZipInputStream zip = new ZipInputStream(archivoZip.getInputStream())) {
            ZipEntry entrada;
            while ((entrada = zip.getNextEntry()) != null) {
                if (entrada.isDirectory()) {
                    continue;
                }
                if (entradas.size() >= MAX_ENTRADAS_ZIP) {
                    throw new ExcepcionNegocio("El ZIP contiene demasiados archivos");
                }
                String nombreEntrada = entrada.getName();
                if (!nombreEntrada.toLowerCase().endsWith(".pdf")) {
                    continue;
                }
                long tamanoEntrada = entrada.getSize();
                if (tamanoEntrada > 0 && tamanoEntrada > MAX_BYTES_DESCOMPRIMIDOS) {
                    throw new ExcepcionNegocio("El ZIP contiene archivos demasiado grandes");
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                zip.transferTo(buffer);
                int tamanoLeido = buffer.size();
                if (tamanoLeido == 0) {
                    continue;
                }
                bytesDescomprimidos += tamanoLeido;
                if (bytesDescomprimidos > MAX_BYTES_DESCOMPRIMIDOS) {
                    throw new ExcepcionNegocio("El contenido descomprimido del ZIP supera el límite permitido");
                }
                long tamanoComprimido = entrada.getCompressedSize();
                if (tamanoComprimido > 0 && tamanoLeido / tamanoComprimido > MAX_RATIO_COMPRESION) {
                    throw new ExcepcionNegocio("El ZIP tiene una relación de compresión sospechosa");
                }
                byte[] contenido = buffer.toByteArray();
                if (contenido.length >= 4 && contenido[0] == 0x25 && contenido[1] == 0x50
                        && contenido[2] == 0x44 && contenido[3] == 0x46) {
                    entradas.add(new EntradaPdfZip(nombreEntrada, contenido));
                }
            }
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo ZIP");
        }

        if (entradas.isEmpty()) {
            throw new ExcepcionNegocio("El ZIP no contiene archivos PDF");
        }
        return entradas;
    }

    private record EntradaPdfZip(String nombre, byte[] contenido) {
    }
}
