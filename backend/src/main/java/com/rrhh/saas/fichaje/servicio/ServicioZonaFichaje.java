package com.rrhh.saas.fichaje.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.fichaje.dominio.AlcanceZonaFichaje;
import com.rrhh.saas.fichaje.dominio.AsignacionZonaFichaje;
import com.rrhh.saas.fichaje.dominio.ZonaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaAsignacionZonaFichaje;
import com.rrhh.saas.fichaje.dto.RespuestaZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudActualizarZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudAsignacionZonaFichaje;
import com.rrhh.saas.fichaje.dto.SolicitudCrearZonaFichaje;
import com.rrhh.saas.fichaje.repositorio.RepositorioAsignacionZonaFichaje;
import com.rrhh.saas.fichaje.repositorio.RepositorioZonaFichaje;
import com.rrhh.saas.nucleo.dominio.Empresa;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import com.rrhh.saas.nucleo.servicio.ServicioEmpresa;
import com.rrhh.saas.organizacion.dominio.Area;
import com.rrhh.saas.organizacion.dominio.Gerencia;
import com.rrhh.saas.organizacion.dominio.Sector;
import com.rrhh.saas.organizacion.repositorio.RepositorioArea;
import com.rrhh.saas.organizacion.repositorio.RepositorioGerencia;
import com.rrhh.saas.organizacion.repositorio.RepositorioSector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioZonaFichaje {

    private final RepositorioZonaFichaje repositorioZona;
    private final RepositorioAsignacionZonaFichaje repositorioAsignacion;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioAuditoria servicioAuditoria;
    private final UtilidadCoordenadasFichaje utilidadCoordenadas;
    private final RepositorioGerencia repositorioGerencia;
    private final RepositorioArea repositorioArea;
    private final RepositorioSector repositorioSector;

    public ServicioZonaFichaje(RepositorioZonaFichaje repositorioZona,
                               RepositorioAsignacionZonaFichaje repositorioAsignacion,
                               ServicioEmpresa servicioEmpresa,
                               ServicioEmpleado servicioEmpleado,
                               ServicioAuditoria servicioAuditoria,
                               UtilidadCoordenadasFichaje utilidadCoordenadas,
                               RepositorioGerencia repositorioGerencia,
                               RepositorioArea repositorioArea,
                               RepositorioSector repositorioSector) {
        this.repositorioZona = repositorioZona;
        this.repositorioAsignacion = repositorioAsignacion;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioAuditoria = servicioAuditoria;
        this.utilidadCoordenadas = utilidadCoordenadas;
        this.repositorioGerencia = repositorioGerencia;
        this.repositorioArea = repositorioArea;
        this.repositorioSector = repositorioSector;
    }

    @Transactional(readOnly = true)
    public List<RespuestaZonaFichaje> listar() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioZona.findByEmpresaIdAndDeletedFalseOrderByNombreAsc(empresaId).stream()
                .map(this::mapearZona)
                .toList();
    }

    @Transactional(readOnly = true)
    public RespuestaZonaFichaje obtenerPorId(Long id) {
        return mapearZona(obtenerEntidad(id));
    }

    @Transactional
    public RespuestaZonaFichaje crear(SolicitudCrearZonaFichaje solicitud) {
        Empresa empresa = obtenerEmpresaActual();
        ZonaFichaje zona = new ZonaFichaje();
        zona.setEmpresa(empresa);
        aplicarDatos(zona, solicitud.nombre(), solicitud.descripcion(),
                solicitud.coordenadas(), solicitud.alcance(), solicitud.activa());
        ZonaFichaje guardada = repositorioZona.save(zona);
        servicioAuditoria.registrar("CREAR", "ZonaFichaje", guardada.getId(), null, guardada.getNombre());
        return mapearZona(guardada);
    }

    @Transactional
    public RespuestaZonaFichaje actualizar(Long id, SolicitudActualizarZonaFichaje solicitud) {
        ZonaFichaje zona = obtenerEntidad(id);
        if (solicitud.nombre() != null) {
            zona.setNombre(solicitud.nombre());
        }
        if (solicitud.descripcion() != null) {
            zona.setDescripcion(solicitud.descripcion());
        }
        if (solicitud.coordenadas() != null) {
            zona.setCoordenadas(utilidadCoordenadas.serializar(solicitud.coordenadas()));
        }
        if (solicitud.alcance() != null) {
            zona.setAlcance(solicitud.alcance());
        }
        if (solicitud.activa() != null) {
            zona.setActiva(solicitud.activa());
        }
        ZonaFichaje guardada = repositorioZona.save(zona);
        servicioAuditoria.registrar("ACTUALIZAR", "ZonaFichaje", guardada.getId(), null, guardada.getNombre());
        return mapearZona(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        ZonaFichaje zona = obtenerEntidad(id);
        zona.setDeleted(true);
        zona.setDeletedAt(java.time.LocalDateTime.now());
        repositorioZona.save(zona);
        servicioAuditoria.registrar("ELIMINAR", "ZonaFichaje", zona.getId(), null, zona.getNombre());
    }

    @Transactional
    public RespuestaAsignacionZonaFichaje agregarAsignacion(Long zonaId, SolicitudAsignacionZonaFichaje solicitud) {
        ZonaFichaje zona = obtenerEntidad(zonaId);
        if (zona.getAlcance() != AlcanceZonaFichaje.ASIGNADA) {
            throw new ExcepcionNegocio("Solo se pueden asignar zonas con alcance ASIGNADA", 400);
        }
        validarDestinoAsignacion(solicitud);
        Long empresaId = ContextoTenant.obtenerEmpresaId();

        AsignacionZonaFichaje asignacion = new AsignacionZonaFichaje();
        asignacion.setZonaFichaje(zona);
        asignacion.setTipo(solicitud.tipo());

        if (solicitud.empleadoId() != null) {
            Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(solicitud.empleadoId(), empresaId);
            asignacion.setEmpleado(empleado);
        }
        if (solicitud.gerenciaId() != null) {
            asignacion.setGerencia(obtenerGerencia(solicitud.gerenciaId(), empresaId));
        }
        if (solicitud.areaId() != null) {
            asignacion.setArea(obtenerArea(solicitud.areaId(), empresaId));
        }
        if (solicitud.sectorId() != null) {
            asignacion.setSector(obtenerSector(solicitud.sectorId(), empresaId));
        }

        AsignacionZonaFichaje guardada = repositorioAsignacion.save(asignacion);
        servicioAuditoria.registrar("ASIGNAR", "AsignacionZonaFichaje", guardada.getId(), null, zona.getNombre());
        return mapearAsignacion(guardada);
    }

    @Transactional
    public void eliminarAsignacion(Long zonaId, Long asignacionId) {
        ZonaFichaje zona = obtenerEntidad(zonaId);
        AsignacionZonaFichaje asignacion = repositorioAsignacion.findById(asignacionId)
                .filter(a -> !a.isDeleted() && a.getZonaFichaje().getId().equals(zona.getId()))
                .orElseThrow(() -> new ExcepcionNegocio("Asignación no encontrada", 404));
        asignacion.setDeleted(true);
        asignacion.setDeletedAt(java.time.LocalDateTime.now());
        repositorioAsignacion.save(asignacion);
    }

    @Transactional(readOnly = true)
    public List<ZonaFichaje> resolverZonasHabilitadas(Empleado empleado, boolean restringirSoloAsignadas) {
        Long empresaId = empleado.getEmpresa().getId();
        List<ZonaFichaje> todas = repositorioZona.findByEmpresaIdAndDeletedFalseOrderByNombreAsc(empresaId);
        List<ZonaFichaje> resultado = new ArrayList<>();

        for (ZonaFichaje zona : todas) {
            if (!zona.isActiva()) {
                continue;
            }
            if (zona.getAlcance() == AlcanceZonaFichaje.GLOBAL && !restringirSoloAsignadas) {
                resultado.add(zona);
            } else if (zona.getAlcance() == AlcanceZonaFichaje.ASIGNADA
                    && coincideAsignacion(zona, empleado)) {
                resultado.add(zona);
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Optional<ZonaFichaje> buscarZonaCoincidente(Empleado empleado, double latitud, double longitud,
                                                         boolean restringirSoloAsignadas,
                                                         ServicioGeocerca servicioGeocerca) {
        List<ZonaFichaje> zonas = resolverZonasHabilitadas(empleado, restringirSoloAsignadas);
        for (ZonaFichaje zona : zonas) {
            List<List<Double>> vertices = utilidadCoordenadas.deserializar(zona.getCoordenadas());
            if (servicioGeocerca.puntoDentroPoligono(latitud, longitud, vertices)) {
                return Optional.of(zona);
            }
        }
        return Optional.empty();
    }

    private boolean coincideAsignacion(ZonaFichaje zona, Empleado empleado) {
        List<AsignacionZonaFichaje> asignaciones =
                repositorioAsignacion.findByZonaFichajeIdAndDeletedFalse(zona.getId());
        for (AsignacionZonaFichaje asignacion : asignaciones) {
            if (asignacion.getEmpleado() != null
                    && asignacion.getEmpleado().getId().equals(empleado.getId())) {
                return true;
            }
            if (asignacion.getSector() != null && empleado.getSector() != null
                    && asignacion.getSector().getId().equals(empleado.getSector().getId())) {
                return true;
            }
            if (asignacion.getArea() != null && empleado.getArea() != null
                    && asignacion.getArea().getId().equals(empleado.getArea().getId())) {
                return true;
            }
            if (asignacion.getGerencia() != null && empleado.getGerencia() != null
                    && asignacion.getGerencia().getId().equals(empleado.getGerencia().getId())) {
                return true;
            }
        }
        return false;
    }

    private void validarDestinoAsignacion(SolicitudAsignacionZonaFichaje solicitud) {
        int destinos = 0;
        if (solicitud.empleadoId() != null) {
            destinos++;
        }
        if (solicitud.gerenciaId() != null) {
            destinos++;
        }
        if (solicitud.areaId() != null) {
            destinos++;
        }
        if (solicitud.sectorId() != null) {
            destinos++;
        }
        if (destinos != 1) {
            throw new ExcepcionNegocio("Debe indicar exactamente un destino de asignación", 400);
        }
    }

    private void aplicarDatos(ZonaFichaje zona, String nombre, String descripcion,
                              List<List<Double>> coordenadas, AlcanceZonaFichaje alcance, boolean activa) {
        zona.setNombre(nombre);
        zona.setDescripcion(descripcion);
        zona.setCoordenadas(utilidadCoordenadas.serializar(coordenadas));
        zona.setAlcance(alcance);
        zona.setActiva(activa);
    }

    private ZonaFichaje obtenerEntidad(Long id) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        return repositorioZona.findByIdAndEmpresaIdAndDeletedFalse(id, empresaId)
                .orElseThrow(() -> new ExcepcionNegocio("Zona de fichaje no encontrada", 404));
    }

    private Empresa obtenerEmpresaActual() {
        return servicioEmpresa.obtenerEntidad(ContextoTenant.obtenerEmpresaId());
    }

    private Gerencia obtenerGerencia(Long id, Long empresaId) {
        return repositorioGerencia.findById(id)
                .filter(g -> !g.isDeleted() && g.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Gerencia no encontrada", 404));
    }

    private Area obtenerArea(Long id, Long empresaId) {
        return repositorioArea.findById(id)
                .filter(a -> !a.isDeleted() && a.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Área no encontrada", 404));
    }

    private Sector obtenerSector(Long id, Long empresaId) {
        return repositorioSector.findById(id)
                .filter(s -> !s.isDeleted() && s.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ExcepcionNegocio("Sector no encontrado", 404));
    }

    private RespuestaZonaFichaje mapearZona(ZonaFichaje zona) {
        List<RespuestaAsignacionZonaFichaje> asignaciones =
                repositorioAsignacion.findByZonaFichajeIdAndDeletedFalse(zona.getId()).stream()
                        .map(this::mapearAsignacion)
                        .toList();
        return new RespuestaZonaFichaje(
                zona.getId(),
                zona.getNombre(),
                zona.getDescripcion(),
                utilidadCoordenadas.deserializar(zona.getCoordenadas()),
                zona.getAlcance(),
                zona.isActiva(),
                asignaciones
        );
    }

    private RespuestaAsignacionZonaFichaje mapearAsignacion(AsignacionZonaFichaje asignacion) {
        Empleado empleado = asignacion.getEmpleado();
        Gerencia gerencia = asignacion.getGerencia();
        Area area = asignacion.getArea();
        Sector sector = asignacion.getSector();
        return new RespuestaAsignacionZonaFichaje(
                asignacion.getId(),
                empleado != null ? empleado.getId() : null,
                empleado != null ? empleado.getNombre() + " " + empleado.getApellido() : null,
                gerencia != null ? gerencia.getId() : null,
                gerencia != null ? gerencia.getNombre() : null,
                area != null ? area.getId() : null,
                area != null ? area.getNombre() : null,
                sector != null ? sector.getId() : null,
                sector != null ? sector.getNombre() : null,
                asignacion.getTipo()
        );
    }
}
