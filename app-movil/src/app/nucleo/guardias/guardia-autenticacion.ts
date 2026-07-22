import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ServicioAutenticacion } from '../servicios/servicio-autenticacion';
import { ServicioPermisos } from '../servicios/servicio-permisos';

export const guardiaAutenticacion: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const router = inject(Router);

  if (!servicioAutenticacion.estaAutenticado()) {
    return router.createUrlTree(['/iniciar-sesion']);
  }

  if (!servicioAutenticacion.tokenAccesoExpirado()) {
    return true;
  }

  if (!servicioAutenticacion.puedeRefrescarSesion()) {
    servicioAutenticacion.limpiarSesionLocal();
    return router.createUrlTree(['/iniciar-sesion'], { queryParams: { sesionExpirada: '1' } });
  }

  return servicioAutenticacion.refrescarSesion().pipe(
    map((refrescado) => {
      if (!refrescado) {
        return router.createUrlTree(['/iniciar-sesion'], { queryParams: { sesionExpirada: '1' } });
      }
      return true;
    }),
    catchError(() => {
      servicioAutenticacion.limpiarSesionLocal();
      return of(router.createUrlTree(['/iniciar-sesion'], { queryParams: { sesionExpirada: '1' } }));
    })
  );
};

export const guardiaInvitado: CanActivateFn = () => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioPermisos = inject(ServicioPermisos);
  const router = inject(Router);

  if (!servicioAutenticacion.estaAutenticado()) {
    return true;
  }

  const redirigir = () => router.createUrlTree([servicioPermisos.rutaInicioPorPortal()]);

  if (servicioAutenticacion.tokenAccesoExpirado()) {
    if (!servicioAutenticacion.puedeRefrescarSesion()) {
      servicioAutenticacion.limpiarSesionLocal();
      return true;
    }

    return servicioAutenticacion.refrescarSesion().pipe(
      map((refrescado) => (refrescado ? redirigir() : true)),
      catchError(() => {
        servicioAutenticacion.limpiarSesionLocal();
        return of(true);
      })
    );
  }

  return redirigir();
};
