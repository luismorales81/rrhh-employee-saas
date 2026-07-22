import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioPermisosApi {
  private readonly urlBase = `${environment.urlApi}/permisos`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<PermisoResumen[]> {
    return this.http.get<PermisoResumen[]>(this.urlBase);
  }
}

export interface PermisoResumen {
  id: number;
  codigo: string;
  descripcion: string;
}
