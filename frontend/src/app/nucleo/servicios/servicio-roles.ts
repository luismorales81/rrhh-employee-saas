import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioRoles {
  private readonly urlBase = `${environment.urlApi}/roles`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<RolResumen[]> {
    return this.http.get<RolResumen[]>(this.urlBase);
  }

  obtener(id: number): Observable<RolResumen> {
    return this.http.get<RolResumen>(`${this.urlBase}/${id}`);
  }

  crear(solicitud: SolicitudCrearRol): Observable<RolResumen> {
    return this.http.post<RolResumen>(this.urlBase, solicitud);
  }

  actualizar(id: number, solicitud: SolicitudActualizarRol): Observable<RolResumen> {
    return this.http.put<RolResumen>(`${this.urlBase}/${id}`, solicitud);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }
}

export interface SolicitudCrearRol {
  nombre: string;
  descripcion?: string;
  permisosIds: number[];
}

export interface SolicitudActualizarRol {
  nombre: string;
  descripcion?: string;
  permisosIds: number[];
}

export interface RolResumen {
  id: number;
  nombre: string;
  descripcion: string;
  permisos: string[];
}
