import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioUsuarios {
  private readonly urlBase = `${environment.urlApi}/usuarios`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<UsuarioResumen[]> {
    return this.http.get<UsuarioResumen[]>(this.urlBase);
  }

  crear(solicitud: SolicitudCrearUsuario): Observable<UsuarioResumen> {
    return this.http.post<UsuarioResumen>(this.urlBase, solicitud);
  }
}

export interface UsuarioResumen {
  id: number;
  empresaId: number;
  email: string;
  activo: boolean;
  roles: string[];
}

export interface SolicitudCrearUsuario {
  empresaId: number;
  email: string;
  password: string;
  rolesIds: number[];
}
