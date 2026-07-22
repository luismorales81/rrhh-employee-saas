import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ServicioAutenticacion } from '../servicios/servicio-autenticacion';
import { ServicioContextoEmpresa } from '../servicios/servicio-contexto-empresa';
import { ServicioPermisos } from '../servicios/servicio-permisos';

export const guardiaPermiso: CanActivateFn = (route) => {
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);
  const permiso = route.data['permiso'] as string | undefined;
  const permisos = route.data['permisos'] as string[] | undefined;

  if (permiso && !servicioPermisos.tienePermiso(permiso)) {
    return router.createUrlTree([servicioPermisos.rutaInicio()]);
  }
  if (permisos && !servicioPermisos.tieneAlgunPermiso(permisos)) {
    return router.createUrlTree([servicioPermisos.rutaInicio()]);
  }
  return true;
};

export const guardiaPlataforma: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);

  if (servicioPermisos.esUsuarioPlataforma()) {
    return true;
  }

  return router.createUrlTree([servicioPermisos.rutaInicio()]);
};

export const guardiaPortalRrhh: CanActivateFn = () => {
  const servicioPermisos = inject(ServicioPermisos);
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  const router = inject(Router);

  const sesion = servicioAutenticacion.sesionActual();
  if (servicioPermisos.esUsuarioPlataforma() && !servicioContextoEmpresa.modoSoporte()) {
    return router.createUrlTree(['/plataforma/inicio']);
  }

  if (servicioPermisos.esPortalRrhh()) {
    return true;
  }

  return router.createUrlTree(['/mi-panel']);
};

export const guardiaPortalEmpleado: CanActivateFn = () => {
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);
  if (servicioPermisos.esPortalEmpleado()) {
    return true;
  }
  return router.createUrlTree([servicioPermisos.rutaInicio()]);
};
