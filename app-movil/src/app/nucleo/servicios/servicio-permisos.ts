import { Injectable, inject } from '@angular/core';
import { ServicioAutenticacion } from './servicio-autenticacion';

@Injectable({ providedIn: 'root' })
export class ServicioPermisos {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);

  esSuperAdmin(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion) {
      return false;
    }
    return sesion.esSuperAdmin || sesion.permisos.includes('empresa.crear');
  }

  tienePermiso(codigo: string): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    return sesion?.permisos.includes(codigo) ?? false;
  }

  tieneAlgunPermiso(codigos: string[]): boolean {
    return codigos.some((c) => this.tienePermiso(c));
  }

  esPortalEmpleado(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion || this.esSuperAdmin()) {
      return false;
    }
    if (this.tienePermiso('empresa.crear') || this.tienePermiso('usuario.crear')
        || this.tienePermiso('solicitud.aprobar')) {
      return false;
    }
    return this.tieneAlgunPermiso([
      'solicitud.crear',
      'comunicacion.mis.ver',
      'documento.mis.ver',
      'recibo.mis.ver'
    ]);
  }

  esPortalRrhh(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion || this.esPortalEmpleado()) {
      return false;
    }
    if (this.esSuperAdmin()) {
      return false;
    }
    return true;
  }

  rutaInicioPorPortal(): string {
    if (this.esPortalRrhh()) {
      return '/tabs-rrhh/panel';
    }
    return '/tabs/inicio';
  }
}
