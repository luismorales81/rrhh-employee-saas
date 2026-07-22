import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ServicioPermisos } from '../servicios/servicio-permisos';

export const guardiaPortalEmpleado: CanActivateFn = () => {
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);

  if (servicioPermisos.esSuperAdmin()) {
    return router.createUrlTree(['/iniciar-sesion'], { queryParams: { portalNoSoportado: '1' } });
  }
  if (servicioPermisos.esPortalRrhh()) {
    return router.createUrlTree(['/tabs-rrhh/panel']);
  }
  return true;
};

export const guardiaPortalRrhh: CanActivateFn = () => {
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);

  if (servicioPermisos.esSuperAdmin()) {
    return router.createUrlTree(['/iniciar-sesion'], { queryParams: { portalNoSoportado: '1' } });
  }
  if (servicioPermisos.esPortalEmpleado()) {
    return router.createUrlTree(['/tabs/inicio']);
  }
  return true;
};
