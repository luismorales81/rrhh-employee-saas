import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioActivos {
  private readonly urlBase = `${environment.urlApi}/activos`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<ActivoResumen[]> {
    return this.http.get<ActivoResumen[]>(this.urlBase);
  }

  crear(solicitud: SolicitudCrearActivo): Observable<ActivoResumen> {
    return this.http.post<ActivoResumen>(this.urlBase, solicitud);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }

  asignar(activoId: number, solicitud: SolicitudAsignarActivo): Observable<AsignacionActivoResumen> {
    return this.http.post<AsignacionActivoResumen>(`${this.urlBase}/${activoId}/asignar`, solicitud);
  }

  devolver(activoId: number, solicitud?: SolicitudDevolverActivo): Observable<AsignacionActivoResumen> {
    return this.http.post<AsignacionActivoResumen>(`${this.urlBase}/${activoId}/devolver`, solicitud ?? {});
  }

  listarAsignacionesEmpleado(empleadoId: number): Observable<AsignacionActivoResumen[]> {
    return this.http.get<AsignacionActivoResumen[]>(
      `${this.urlBase}/asignaciones/empleado/${empleadoId}`
    );
  }
}

export type EstadoActivo = 'DISPONIBLE' | 'ASIGNADO' | 'BAJA' | 'MANTENIMIENTO';

export interface ActivoResumen {
  id: number;
  nombre: string;
  tipo: string;
  numeroSerie: string | null;
  estado: EstadoActivo;
}

export interface SolicitudCrearActivo {
  nombre: string;
  tipo: string;
  numeroSerie?: string;
}

export interface SolicitudAsignarActivo {
  empleadoId: number;
  fechaEntrega: string;
  notas?: string;
}

export interface SolicitudDevolverActivo {
  fechaDevolucion?: string;
  notas?: string;
}

export interface AsignacionActivoResumen {
  id: number;
  activoId: number;
  nombreActivo: string;
  empleadoId: number;
  nombreEmpleado: string;
  fechaEntrega: string;
  fechaDevolucion: string | null;
}
