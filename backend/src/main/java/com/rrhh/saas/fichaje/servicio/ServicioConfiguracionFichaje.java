package com.rrhh.saas.fichaje.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.repositorio.RepositorioEmpleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.fichaje.dominio.ConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dominio.PoliticaFichajeEmpresa;
import com.rrhh.saas.fichaje.dto.RespuestaConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.dto.SolicitudConfiguracionFichajeEmpleado;
import com.rrhh.saas.fichaje.repositorio.RepositorioConfiguracionFichajeEmpleado;
import com.rrhh.saas.nucleo.servicio.ServicioAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServicioConfiguracionFichaje {

    private final RepositorioConfiguracionFichajeEmpleado repositorioConfig;
    private final RepositorioEmpleado repositorioEmpleado;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioPoliticaFichaje servicioPolitica;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioConfiguracionFichaje(RepositorioConfiguracionFichajeEmpleado repositorioConfig,
                                        RepositorioEmpleado repositorioEmpleado,
                                        ServicioEmpleado servicioEmpleado,
                                        ServicioPoliticaFichaje servicioPolitica,
                                        ServicioAuditoria servicioAuditoria) {
        this.repositorioConfig = repositorioConfig;
        this.repositorioEmpleado = repositorioEmpleado;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioPolitica = servicioPolitica;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional(readOnly = true)
    public List<RespuestaConfiguracionFichajeEmpleado> listarConfiguraciones() {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPoliticaIndependiente();
        List<Empleado> empleados = repositorioEmpleado.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .sorted((a, b) -> {
                    int cmp = a.getApellido().compareToIgnoreCase(b.getApellido());
                    return cmp != 0 ? cmp : a.getNombre().compareToIgnoreCase(b.getNombre());
                })
                .toList();
        Map<Long, ConfiguracionFichajeEmpleado> configs = repositorioConfig.findByEmpresaId(empresaId).stream()
                .collect(Collectors.toMap(c -> c.getEmpleado().getId(), Function.identity()));

        List<RespuestaConfiguracionFichajeEmpleado> resultado = new ArrayList<>();
        for (Empleado empleado : empleados) {
            ConfiguracionFichajeEmpleado config = configs.get(empleado.getId());
            resultado.add(mapear(empleado, config, politica));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public RespuestaConfiguracionFichajeEmpleado obtenerConfiguracionEmpleado(Long empleadoId) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPoliticaIndependiente();
        ConfiguracionFichajeEmpleado config = repositorioConfig.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .orElse(null);
        return mapear(empleado, config, politica);
    }

    @Transactional
    public RespuestaConfiguracionFichajeEmpleado actualizarConfiguracion(
            Long empleadoId, SolicitudConfiguracionFichajeEmpleado solicitud) {
        Long empresaId = ContextoTenant.obtenerEmpresaId();
        Empleado empleado = servicioEmpleado.obtenerEmpleadoEntidad(empleadoId, empresaId);
        ConfiguracionFichajeEmpleado config = repositorioConfig.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .orElseGet(() -> {
                    ConfiguracionFichajeEmpleado nueva = new ConfiguracionFichajeEmpleado();
                    nueva.setEmpleado(empleado);
                    return nueva;
                });
        config.setFichajeMovilHabilitado(solicitud.fichajeMovilHabilitado());
        config.setRestringirSoloZonasAsignadas(solicitud.restringirSoloZonasAsignadas());
        ConfiguracionFichajeEmpleado guardada = repositorioConfig.save(config);
        servicioAuditoria.registrar("ACTUALIZAR", "ConfiguracionFichajeEmpleado", guardada.getId(),
                empleado.getId().toString(), empleado.getNombre());
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPolitica();
        return mapear(empleado, guardada, politica);
    }

    @Transactional
    public void registrarConsentimiento(String idDispositivo) {
        Long usuarioId = ContextoTenant.obtenerUsuarioId();
        if (usuarioId == null) {
            throw new ExcepcionNegocio("Usuario no autenticado", 401);
        }
        Empleado empleado = servicioEmpleado.obtenerPorUsuarioId(usuarioId);
        PoliticaFichajeEmpresa politica = servicioPolitica.obtenerOCrearPolitica();
        ConfiguracionFichajeEmpleado config = obtenerOcrearConfig(empleado);
        if (!config.isFichajeMovilHabilitado()) {
            throw new ExcepcionNegocio("El fichaje móvil no está habilitado para este empleado", 403);
        }
        config.setConsentimientoAceptadoAt(LocalDateTime.now());
        config.setVersionPoliticaAceptada(politica.getVersion());
        if (idDispositivo != null && !idDispositivo.isBlank()) {
            config.setDispositivoRegistradoId(idDispositivo);
        }
        repositorioConfig.save(config);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ConfiguracionFichajeEmpleado obtenerOcrearConfig(Empleado empleado) {
        return repositorioConfig.findByEmpleadoIdAndDeletedFalse(empleado.getId())
                .orElseGet(() -> {
                    ConfiguracionFichajeEmpleado nueva = new ConfiguracionFichajeEmpleado();
                    nueva.setEmpleado(empleado);
                    return repositorioConfig.save(nueva);
                });
    }

    public boolean requiereConsentimiento(ConfiguracionFichajeEmpleado config, PoliticaFichajeEmpresa politica) {
        if (config == null || config.getConsentimientoAceptadoAt() == null) {
            return true;
        }
        return config.getVersionPoliticaAceptada() == null
                || config.getVersionPoliticaAceptada() < politica.getVersion();
    }

    private RespuestaConfiguracionFichajeEmpleado mapear(Empleado empleado,
                                                         ConfiguracionFichajeEmpleado config,
                                                         PoliticaFichajeEmpresa politica) {
        boolean habilitado = config != null && config.isFichajeMovilHabilitado();
        boolean restringir = config != null && config.isRestringirSoloZonasAsignadas();
        return new RespuestaConfiguracionFichajeEmpleado(
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                habilitado,
                restringir,
                config != null ? config.getConsentimientoAceptadoAt() : null,
                config != null ? config.getVersionPoliticaAceptada() : null,
                requiereConsentimiento(config, politica)
        );
    }
}
