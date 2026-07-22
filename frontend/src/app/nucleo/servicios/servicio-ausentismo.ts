import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioAusentismo {
  private readonly urlBase = `${environment.urlApi}/ausentismo`;

  constructor(private readonly http: HttpClient) {}

  listarAusencias(): Observable<AusenciaResumen[]> {
    return this.http.get<AusenciaResumen[]>(`${this.urlBase}/ausencias`);
  }

  registrar(solicitud: SolicitudRegistrarAusencia): Observable<AusenciaResumen> {
    return this.http.post<AusenciaResumen>(`${this.urlBase}/ausencias`, solicitud);
  }

  listarTipos(): Observable<TipoAusenciaResumen[]> {
    return this.http.get<TipoAusenciaResumen[]>(`${this.urlBase}/tipos`);
  }

  actualizar(id: number, datos: SolicitudActualizarAusencia): Observable<AusenciaResumen> {
    return this.http.put<AusenciaResumen>(`${this.urlBase}/ausencias/${id}`, datos);
  }
}

export interface AusenciaResumen {
  id: number;
  empleadoId: number;
  empleadoNombre: string;
  tipoAusenciaId: number;
  tipoAusenciaNombre: string;
  fecha: string;
  motivo: string | null;
  justificada: boolean;
  urlCertificado?: string | null;
}

export interface TipoAusenciaResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  requiereCertificado: boolean;
}

export interface SolicitudRegistrarAusencia {
  empleadoId: number;
  tipoAusenciaId: number;
  fecha: string;
  motivo?: string;
  justificada: boolean;
  urlCertificado?: string;
}

export interface SolicitudActualizarAusencia {
  motivo?: string;
  justificada?: boolean;
  urlCertificado?: string;
}
