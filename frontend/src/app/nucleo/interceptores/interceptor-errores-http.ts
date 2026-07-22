import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ServicioAutenticacion } from '../servicios/servicio-autenticacion';

export const interceptorErroresHttp: HttpInterceptorFn = (peticion, siguiente) => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const router = inject(Router);

  return siguiente(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !peticion.url.includes('/auth/login')) {
        servicioAutenticacion.limpiarSesionLocal();
        router.navigate(['/iniciar-sesion'], {
          queryParams: { sesionExpirada: '1' }
        });
      }
      return throwError(() => error);
    })
  );
};
