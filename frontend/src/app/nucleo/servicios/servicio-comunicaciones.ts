import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioComunicaciones {
  private readonly urlBase = `${environment.urlApi}/comunicaciones`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<ComunicacionResumen[]> {
    return this.http.get<ComunicacionResumen[]>(this.urlBase);
  }

  listarProgramadas(): Observable<ComunicacionResumen[]> {
    return this.http.get<ComunicacionResumen[]>(`${this.urlBase}/programadas`);
  }

  publicar(solicitud: SolicitudPublicarComunicacion): Observable<ComunicacionResumen> {
    return this.http.post<ComunicacionResumen>(this.urlBase, solicitud);
  }

  listarMisComunicaciones(): Observable<DestinatarioComunicacionResumen[]> {
    return this.http.get<DestinatarioComunicacionResumen[]>(`${this.urlBase}/mis-comunicaciones`);
  }

  listarDestinatarios(comunicacionId: number): Observable<DestinatarioComunicacionResumen[]> {
    return this.http.get<DestinatarioComunicacionResumen[]>(
      `${this.urlBase}/${comunicacionId}/destinatarios`
    );
  }

  confirmarLectura(destinatarioId: number): Observable<DestinatarioComunicacionResumen> {
    return this.http.post<DestinatarioComunicacionResumen>(
      `${this.urlBase}/destinatarios/${destinatarioId}/confirmar-lectura`,
      {}
    );
  }

  cancelar(id: number): Observable<ComunicacionResumen> {
    return this.http.post<ComunicacionResumen>(`${this.urlBase}/${id}/cancelar`, {});
  }
}

export interface ComunicacionResumen {
  id: number;
  titulo: string;
  mensaje: string;
  tipoDestinatario: string;
  requiereConfirmacion: boolean;
  estado: string;
  fechaPublicacion: string | null;
  fechaPublicacionProgramada: string | null;
  totalDestinatarios: number;
  totalLeidos: number;
  totalConfirmados: number;
}

export interface DestinatarioComunicacionResumen {
  id: number;
  comunicacionId: number;
  tituloComunicacion: string;
  mensaje: string;
  empleadoId?: number;
  nombreEmpleado?: string;
  requiereConfirmacion: boolean;
  fechaPublicacion: string;
  fechaLectura: string | null;
  fechaConfirmacion: string | null;
  leida: boolean;
  confirmada: boolean;
}

export interface SolicitudPublicarComunicacion {
  titulo: string;
  mensaje: string;
  tipoDestinatario: string;
  referenciaDestinatarioId?: number | null;
  requiereConfirmacion: boolean;
  fechaPublicacionProgramada?: string | null;
}
