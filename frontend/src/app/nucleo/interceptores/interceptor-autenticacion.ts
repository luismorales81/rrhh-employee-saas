import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ServicioAutenticacion } from '../servicios/servicio-autenticacion';
import { ServicioContextoEmpresa } from '../servicios/servicio-contexto-empresa';

export const interceptorAutenticacion: HttpInterceptorFn = (peticion, siguiente) => {
  const servicioAutenticacion = inject(ServicioAutenticacion);
  const servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  const token = servicioAutenticacion.obtenerToken();
  const sesion = servicioAutenticacion.sesionActual();

  const encabezados: Record<string, string> = {};

  if (token && !peticion.url.includes('/auth/login')) {
    encabezados['Authorization'] = `Bearer ${token}`;
  }

  const empresaId = servicioContextoEmpresa.obtenerEmpresaIdActiva(sesion);
  if (empresaId !== null && sesion?.empresaId === null) {
    encabezados['X-Empresa-Id'] = String(empresaId);
  }

  const esPlataforma = sesion?.esSuperAdmin
    || (sesion?.permisos?.includes('empresa.crear') ?? false);
  if (esPlataforma && servicioContextoEmpresa.modoSoporte()) {
    encabezados['X-Modo-Soporte'] = 'true';
  }

  if (Object.keys(encabezados).length > 0) {
    peticion = peticion.clone({ setHeaders: encabezados });
  }

  return siguiente(peticion);
};
