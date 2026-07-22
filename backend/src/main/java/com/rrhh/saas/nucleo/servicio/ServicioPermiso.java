package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.comun.tenant.ContextoTenant;
import com.rrhh.saas.nucleo.dominio.Permiso;
import com.rrhh.saas.nucleo.dto.RespuestaPermiso;
import com.rrhh.saas.nucleo.repositorio.RepositorioPermiso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicioPermiso {

    private final RepositorioPermiso repositorioPermiso;

    public ServicioPermiso(RepositorioPermiso repositorioPermiso) {
        this.repositorioPermiso = repositorioPermiso;
    }

    @Transactional(readOnly = true)
    public List<RespuestaPermiso> listar() {
        return repositorioPermiso.findByDeletedFalse().stream().map(this::mapear).toList();
    }

    public Set<Permiso> obtenerPorIds(Set<Long> ids) {
        return ids.stream()
                .map(id -> repositorioPermiso.findById(id)
                        .filter(p -> !p.isDeleted())
                        .orElseThrow(() -> new ExcepcionNegocio("Permiso no encontrado: " + id)))
                .collect(Collectors.toSet());
    }

    private RespuestaPermiso mapear(Permiso permiso) {
        return new RespuestaPermiso(permiso.getId(), permiso.getCodigo(), permiso.getDescripcion());
    }
}
