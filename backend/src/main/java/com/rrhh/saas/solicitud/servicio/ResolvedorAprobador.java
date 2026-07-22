package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;
import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.empleado.servicio.ServicioEmpleado;
import com.rrhh.saas.nucleo.dominio.Rol;
import com.rrhh.saas.nucleo.dominio.Usuario;
import com.rrhh.saas.nucleo.repositorio.RepositorioRol;
import com.rrhh.saas.nucleo.repositorio.RepositorioUsuario;
import com.rrhh.saas.organizacion.dominio.Gerencia;
import com.rrhh.saas.solicitud.dominio.PasoFlujoTrabajo;
import com.rrhh.saas.solicitud.dominio.TipoPaso;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResolvedorAprobador {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;

    public ResolvedorAprobador(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
    }

    public Usuario resolver(PasoFlujoTrabajo paso, Empleado solicitante) {
        return switch (paso.getTipoPaso()) {
            case SUPERVISOR_DIRECTO -> resolverSupervisorDirecto(solicitante);
            case RRHH -> resolverPorRolNombre(solicitante.getEmpresa().getId(), "RRHH");
            case GERENCIA -> resolverGerencia(solicitante);
            case ROL_ESPECIFICO -> resolverPorRol(paso, solicitante.getEmpresa().getId());
            case USUARIO_ESPECIFICO -> resolverUsuarioEspecifico(paso);
        };
    }

    private Usuario resolverSupervisorDirecto(Empleado solicitante) {
        Empleado supervisor = solicitante.getSupervisor();
        if (supervisor == null) {
            throw new ExcepcionNegocio("El empleado no tiene supervisor asignado");
        }
        if (supervisor.getUsuario() == null) {
            throw new ExcepcionNegocio("El supervisor no tiene usuario de acceso asociado");
        }
        return supervisor.getUsuario();
    }

    private Usuario resolverGerencia(Empleado solicitante) {
        Gerencia gerencia = solicitante.getGerencia();
        if (gerencia == null) {
            throw new ExcepcionNegocio("El empleado no tiene gerencia asignada");
        }
        if (gerencia.getResponsableId() != null) {
            return repositorioUsuario.findById(gerencia.getResponsableId())
                    .filter(u -> !u.isDeleted() && u.isActivo())
                    .orElseThrow(() -> new ExcepcionNegocio("Responsable de gerencia no encontrado"));
        }
        return resolverPorRolNombre(solicitante.getEmpresa().getId(), "Administrador");
    }

    private Usuario resolverPorRol(PasoFlujoTrabajo paso, Long empresaId) {
        Rol rol = paso.getRol();
        if (rol == null) {
            throw new ExcepcionNegocio("El paso requiere un rol específico configurado");
        }
        List<Usuario> usuarios = repositorioUsuario.buscarPorRol(empresaId, rol.getId());
        if (usuarios.isEmpty()) {
            throw new ExcepcionNegocio("No hay usuarios con el rol requerido: " + rol.getNombre());
        }
        return usuarios.get(0);
    }

    private Usuario resolverPorRolNombre(Long empresaId, String nombreRol) {
        Rol rol = repositorioRol.findByEmpresaIdAndNombreAndDeletedFalse(empresaId, nombreRol)
                .orElseThrow(() -> new ExcepcionNegocio("Rol no encontrado: " + nombreRol));
        List<Usuario> usuarios = repositorioUsuario.buscarPorRol(empresaId, rol.getId());
        if (usuarios.isEmpty()) {
            throw new ExcepcionNegocio("No hay usuarios con el rol: " + nombreRol);
        }
        return usuarios.get(0);
    }

    private Usuario resolverUsuarioEspecifico(PasoFlujoTrabajo paso) {
        Usuario usuario = paso.getUsuario();
        if (usuario == null || usuario.isDeleted() || !usuario.isActivo()) {
            throw new ExcepcionNegocio("Usuario específico del paso no válido");
        }
        return usuario;
    }
}
