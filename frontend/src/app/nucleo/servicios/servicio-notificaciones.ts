import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioNotificaciones {
  private readonly urlBase = `${environment.urlApi}/notificaciones`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<NotificacionResumen[]> {
    return this.http.get<NotificacionResumen[]>(this.urlBase);
  }

  contarNoLeidas(): Observable<{ cantidad: number }> {
    return this.http.get<{ cantidad: number }>(`${this.urlBase}/no-leidas`);
  }

  marcarLeida(id: number): Observable<NotificacionResumen> {
    return this.http.post<NotificacionResumen>(`${this.urlBase}/${id}/marcar-leida`, {});
  }
}

export interface NotificacionResumen {
  id: number;
  titulo: string;
  mensaje: string;
  tipo: string;
  referenciaId: number | null;
  leida: boolean;
  createdAt: string;
}
