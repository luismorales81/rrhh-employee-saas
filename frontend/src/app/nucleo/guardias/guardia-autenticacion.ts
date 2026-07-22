import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ServicioAutenticacion } from '../servicios/servicio-autenticacion';
import { ServicioContextoEmpresa } from '../servicios/servicio-contexto-empresa';
import { ServicioPermisos } from '../servicios/servicio-permisos';

export const guardiaAutenticacion: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const router = inject(Router);

  if (servicioAutenticacion.estaAutenticado()) {
    return true;
  }

  return router.createUrlTree(['/iniciar-sesion']);
};

export const guardiaInvitado: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioPermisos = inject(ServicioPermisos);
  const servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  const router = inject(Router);

  if (!servicioAutenticacion.estaAutenticado()) {
    return true;
  }

  const sesion = servicioAutenticacion.sesionActual();
  const empresaId = servicioContextoEmpresa.obtenerEmpresaIdActiva(sesion);

  if (sesion && !servicioPermisos.esUsuarioPlataforma() && !empresaId) {
    servicioAutenticacion.limpiarSesionLocal();
    return true;
  }

  return router.createUrlTree([servicioPermisos.rutaInicio()]);
};

export const guardiaEmpresaActiva: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);

  const sesion = servicioAutenticacion.sesionActual();
  const empresaId = servicioContextoEmpresa.obtenerEmpresaIdActiva(sesion);

  if (!empresaId) {
    if (servicioPermisos.esUsuarioPlataforma()) {
      return router.createUrlTree(['/plataforma/empresas']);
    }
    servicioAutenticacion.limpiarSesionLocal();
    return router.createUrlTree(['/iniciar-sesion']);
  }

  if (servicioPermisos.esUsuarioPlataforma() && !servicioContextoEmpresa.modoSoporte()) {
    return router.createUrlTree([servicioPermisos.rutaInicio()]);
  }

  return true;
};

export const guardiaContextoEmpresaRuta: CanActivateFn = (route) => {
  const servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  const idParam = route.paramMap.get('id');
  if (!idParam) {
    return true;
  }
  const id = Number(idParam);
  if (!Number.isNaN(id)) {
    const nombreActual = servicioContextoEmpresa.empresaSeleccionadaNombre();
    servicioContextoEmpresa.establecer(id, nombreActual);
  }
  return true;
};
