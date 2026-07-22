import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { ServicioAutenticacion } from './servicio-autenticacion';
import { ServicioEstadoApi } from './servicio-estado-api';
import { esPeticionPublica } from './rutas-publicas-api';
import { mensajeSinConexion } from '../utilidades/mensajes-error-producto';

export const interceptorErroresHttp: HttpInterceptorFn = (peticion, siguiente) => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioEstadoApi = inject(ServicioEstadoApi);
  const router = inject(Router);

  return siguiente(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        servicioEstadoApi.conectada.set(false);
        servicioEstadoApi.mensaje.set(mensajeSinConexion());
      }

      if (error.status !== 401 || esPeticionPublica(peticion.url)) {
        return throwError(() => error);
      }

      if (!servicioAutenticacion.puedeRefrescarSesion()) {
        servicioAutenticacion.limpiarSesionLocal();
        router.navigate(['/iniciar-sesion'], {
          queryParams: { sesionExpirada: '1' }
        });
        return throwError(() => error);
      }

      return servicioAutenticacion.refrescarSesion().pipe(
        switchMap((refrescado) => {
          if (!refrescado) {
            servicioAutenticacion.limpiarSesionLocal();
            router.navigate(['/iniciar-sesion'], {
              queryParams: { sesionExpirada: '1' }
            });
            return throwError(() => error);
          }

          const token = servicioAutenticacion.obtenerToken();
          const reintentar = peticion.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
          });
          return siguiente(reintentar);
        })
      );
    })
  );
};
