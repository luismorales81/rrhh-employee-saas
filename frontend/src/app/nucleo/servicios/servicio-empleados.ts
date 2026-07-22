import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioEmpleados {
  private readonly urlBase = `${environment.urlApi}/empleados`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<EmpleadoResumen[]> {
    return this.http.get<EmpleadoResumen[]>(this.urlBase);
  }

  obtener(id: number): Observable<EmpleadoResumen> {
    return this.http.get<EmpleadoResumen>(`${this.urlBase}/${id}`);
  }

  obtenerPropio(): Observable<EmpleadoResumen> {
    return this.http.get<EmpleadoResumen>(`${this.urlBase}/propio`);
  }

  obtenerLegajo(id: number): Observable<LegajoEmpleadoResumen> {
    return this.http.get<LegajoEmpleadoResumen>(`${this.urlBase}/${id}/legajo`);
  }

  iniciarOnboarding(id: number): Observable<LegajoEmpleadoResumen> {
    return this.http.post<LegajoEmpleadoResumen>(`${this.urlBase}/${id}/onboarding/iniciar`, {});
  }

  completarOnboarding(id: number): Observable<LegajoEmpleadoResumen> {
    return this.http.post<LegajoEmpleadoResumen>(`${this.urlBase}/${id}/onboarding/completar`, {});
  }

  crear(solicitud: SolicitudCrearEmpleado): Observable<EmpleadoResumen> {
    return this.http.post<EmpleadoResumen>(this.urlBase, solicitud);
  }

  actualizar(id: number, solicitud: SolicitudActualizarEmpleado): Observable<EmpleadoResumen> {
    return this.http.put<EmpleadoResumen>(`${this.urlBase}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }
}

export interface SolicitudActualizarEmpleado {
  nombre: string;
  apellido: string;
  numeroEmpleado?: string;
  dni?: string;
  cuil?: string;
  fechaIngreso?: string;
  telefono?: string;
  emailLaboral?: string;
  usuarioId?: number;
  supervisorId?: number;
  gerenciaId?: number;
  areaId?: number;
  sectorId?: number;
  puestoId?: number;
}

export interface SolicitudCrearEmpleado {
  nombre: string;
  apellido: string;
  numeroEmpleado?: string;
  dni?: string;
  cuil?: string;
  fechaIngreso?: string;
  telefono?: string;
  emailLaboral?: string;
  usuarioId?: number;
  supervisorId?: number;
  gerenciaId?: number;
  areaId?: number;
  sectorId?: number;
  puestoId?: number;
}

export interface EmpleadoResumen {
  id: number;
  nombre: string;
  apellido: string;
  numeroEmpleado: string | null;
  dni: string | null;
  cuil: string | null;
  fechaIngreso: string | null;
  estado: string;
  emailLaboral: string | null;
  usuarioId: number | null;
  supervisorId: number | null;
  gerenciaId: number | null;
  areaId: number | null;
  sectorId: number | null;
  puestoId: number | null;
  legajoId: number | null;
}

export interface LegajoEmpleadoResumen {
  legajoId: number;
  empleadoId: number;
  nombre: string;
  apellido: string;
  numeroEmpleado: string | null;
  dni: string | null;
  fechaIngreso: string | null;
  estado: string;
  estadoOnboarding: string | null;
  fechaCompletadoOnboarding: string | null;
  estadoOffboarding?: string | null;
  fechaCompletadoOffboarding?: string | null;
  documentosPendientes: number;
  solicitudesAbiertas: number;
}
