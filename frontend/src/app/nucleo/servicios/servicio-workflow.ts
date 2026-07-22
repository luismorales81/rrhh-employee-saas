import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioWorkflow {
  private readonly urlBase = `${environment.urlApi}`;

  constructor(private readonly http: HttpClient) {}

  listarFlujos(): Observable<FlujoTrabajoResumen[]> {
    return this.http.get<FlujoTrabajoResumen[]>(`${this.urlBase}/flujos-trabajo`);
  }

  obtenerFlujo(id: number): Observable<FlujoTrabajoResumen> {
    return this.http.get<FlujoTrabajoResumen>(`${this.urlBase}/flujos-trabajo/${id}`);
  }

  crearFlujo(solicitud: SolicitudCrearFlujoTrabajo): Observable<FlujoTrabajoResumen> {
    return this.http.post<FlujoTrabajoResumen>(`${this.urlBase}/flujos-trabajo`, solicitud);
  }

  actualizarFlujo(id: number, solicitud: SolicitudActualizarFlujoTrabajo): Observable<FlujoTrabajoResumen> {
    return this.http.put<FlujoTrabajoResumen>(`${this.urlBase}/flujos-trabajo/${id}`, solicitud);
  }

  eliminarFlujo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/flujos-trabajo/${id}`);
  }

  alternarActivoFlujo(id: number): Observable<FlujoTrabajoResumen> {
    return this.http.post<FlujoTrabajoResumen>(`${this.urlBase}/flujos-trabajo/${id}/activar`, {});
  }

  listarTipos(): Observable<TipoSolicitudResumen[]> {
    return this.http.get<TipoSolicitudResumen[]>(`${this.urlBase}/tipos-solicitud`);
  }

  obtenerTipo(id: number): Observable<TipoSolicitudResumen> {
    return this.http.get<TipoSolicitudResumen>(`${this.urlBase}/tipos-solicitud/${id}`);
  }

  crearTipo(solicitud: SolicitudCrearTipoSolicitud): Observable<TipoSolicitudResumen> {
    return this.http.post<TipoSolicitudResumen>(`${this.urlBase}/tipos-solicitud`, solicitud);
  }

  actualizarTipo(id: number, solicitud: SolicitudActualizarTipoSolicitud): Observable<TipoSolicitudResumen> {
    return this.http.put<TipoSolicitudResumen>(`${this.urlBase}/tipos-solicitud/${id}`, solicitud);
  }

  eliminarTipo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/tipos-solicitud/${id}`);
  }
}

export interface FlujoTrabajoResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  activo: boolean;
  pasos: PasoFlujoResumen[];
}

export interface PasoFlujoResumen {
  id: number;
  orden: number;
  nombre: string;
  tipoPaso: string;
  rolId: number | null;
  usuarioId: number | null;
}

export interface TipoSolicitudResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  flujoTrabajoId: number | null;
  requiereAprobacion: boolean;
  requiereAdjunto: boolean;
}

export interface SolicitudCrearFlujoTrabajo {
  nombre: string;
  descripcion?: string;
  pasos?: {
    orden: number;
    nombre: string;
    tipoPaso: string;
    rolId?: number | null;
    usuarioId?: number | null;
  }[];
}

export interface SolicitudActualizarFlujoTrabajo {
  nombre: string;
  descripcion?: string;
  pasos?: SolicitudCrearFlujoTrabajo['pasos'];
}

export interface SolicitudCrearTipoSolicitud {
  nombre: string;
  descripcion?: string;
  flujoTrabajoId?: number | null;
  requiereAprobacion?: boolean;
  requiereAdjunto?: boolean;
}

export interface SolicitudActualizarTipoSolicitud {
  nombre: string;
  descripcion?: string;
  flujoTrabajoId?: number | null;
  requiereAprobacion?: boolean;
  requiereAdjunto?: boolean;
}
