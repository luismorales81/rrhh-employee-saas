import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ServicioAutenticacion } from './servicio-autenticacion';
import { esPeticionPublica } from './rutas-publicas-api';

export const interceptorAutenticacion: HttpInterceptorFn = (peticion, siguiente) => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const token = servicioAutenticacion.obtenerToken();
  const sesion = servicioAutenticacion.sesionActual();

  if (esPeticionPublica(peticion.url)) {
    return siguiente(peticion);
  }

  const encabezados: Record<string, string> = {};

  if (token) {
    encabezados['Authorization'] = `Bearer ${token}`;
  }

  if (sesion?.empresaId != null) {
    encabezados['X-Empresa-Id'] = String(sesion.empresaId);
  }

  if (Object.keys(encabezados).length > 0) {
    peticion = peticion.clone({ setHeaders: encabezados });
  }

  return siguiente(peticion);
};
