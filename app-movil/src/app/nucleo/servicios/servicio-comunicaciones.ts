import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioComunicaciones {
  private readonly urlBase = `${environment.urlApi}/comunicaciones`;

  constructor(private readonly http: HttpClient) {}

  listarMisComunicaciones(): Observable<DestinatarioComunicacionResumen[]> {
    return this.http.get<DestinatarioComunicacionResumen[]>(`${this.urlBase}/mis-comunicaciones`);
  }

  confirmarLectura(destinatarioId: number): Observable<DestinatarioComunicacionResumen> {
    return this.http.post<DestinatarioComunicacionResumen>(
      `${this.urlBase}/destinatarios/${destinatarioId}/confirmar-lectura`,
      {}
    );
  }
}

export interface DestinatarioComunicacionResumen {
  id: number;
  comunicacionId: number;
  tituloComunicacion: string;
  mensaje: string;
  requiereConfirmacion: boolean;
  fechaPublicacion: string;
  fechaLectura: string | null;
  fechaConfirmacion: string | null;
  leida: boolean;
  confirmada: boolean;
}
