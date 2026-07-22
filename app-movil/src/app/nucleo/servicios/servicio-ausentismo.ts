import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioAusentismo {
  private readonly urlBase = `${environment.urlApi}/ausentismo`;

  constructor(private readonly http: HttpClient) {}

  listarTipos(): Observable<TipoAusenciaResumen[]> {
    return this.http.get<TipoAusenciaResumen[]>(`${this.urlBase}/tipos`);
  }

  listarMisAusencias(): Observable<AusenciaResumen[]> {
    return this.http.get<AusenciaResumen[]>(`${this.urlBase}/mis-ausencias`);
  }

  registrarPropia(solicitud: SolicitudRegistrarMisAusencias): Observable<AusenciaResumen> {
    return this.http.post<AusenciaResumen>(`${this.urlBase}/mis-ausencias`, solicitud);
  }

  justificarPropia(id: number, urlCertificado: string): Observable<AusenciaResumen> {
    return this.http.put<AusenciaResumen>(`${this.urlBase}/mis-ausencias/${id}/justificar`, {
      urlCertificado
    });
  }
}

export interface TipoAusenciaResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  requiereCertificado: boolean;
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
  urlCertificado: string | null;
}

export interface SolicitudRegistrarMisAusencias {
  tipoAusenciaId: number;
  fecha: string;
  motivo?: string;
  justificada: boolean;
  urlCertificado?: string;
}
