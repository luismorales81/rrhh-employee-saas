import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioSolicitudes {
  private readonly urlBase = `${environment.urlApi}`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<SolicitudResumen[]> {
    return this.http.get<SolicitudResumen[]>(`${this.urlBase}/solicitudes`);
  }

  listarPropias(): Observable<SolicitudResumen[]> {
    return this.http.get<SolicitudResumen[]>(`${this.urlBase}/solicitudes/propias`);
  }

  listarPendientes(): Observable<SolicitudResumen[]> {
    return this.http.get<SolicitudResumen[]>(`${this.urlBase}/solicitudes/pendientes-aprobacion`);
  }

  listarTipos(): Observable<TipoSolicitudResumen[]> {
    return this.http.get<TipoSolicitudResumen[]>(`${this.urlBase}/tipos-solicitud`);
  }

  crear(solicitud: SolicitudCrearSolicitud): Observable<SolicitudResumen> {
    return this.http.post<SolicitudResumen>(`${this.urlBase}/solicitudes`, solicitud);
  }

  enviar(id: number): Observable<SolicitudResumen> {
    return this.http.post<SolicitudResumen>(`${this.urlBase}/solicitudes/${id}/enviar`, {});
  }

  aprobar(id: number, comentarios?: string): Observable<SolicitudResumen> {
    return this.http.post<SolicitudResumen>(`${this.urlBase}/solicitudes/${id}/aprobar`, {
      accion: 'APROBAR',
      comentarios: comentarios ?? null
    });
  }

  rechazar(id: number, comentarios?: string): Observable<SolicitudResumen> {
    return this.http.post<SolicitudResumen>(`${this.urlBase}/solicitudes/${id}/rechazar`, {
      accion: 'RECHAZAR',
      comentarios: comentarios ?? null
    });
  }

  cancelar(id: number): Observable<SolicitudResumen> {
    return this.http.post<SolicitudResumen>(`${this.urlBase}/solicitudes/${id}/cancelar`, {});
  }
}

export interface SolicitudResumen {
  id: number;
  empleadoId: number;
  empleadoNombre?: string;
  tipoSolicitudId: number;
  tipoSolicitudNombre: string;
  estado: string;
  motivo: string | null;
  instanciaFlujoId: number | null;
  aprobadorActualId: number | null;
  createdAt: string;
  fechaInicioVacaciones?: string | null;
  fechaFinVacaciones?: string | null;
  solicitudVacacionesId?: number | null;
}

export interface TipoSolicitudResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  flujoTrabajoId: number | null;
  requiereAprobacion: boolean;
  requiereAdjunto: boolean;
}

export interface SolicitudCrearSolicitud {
  tipoSolicitudId: number;
  motivo?: string;
}
