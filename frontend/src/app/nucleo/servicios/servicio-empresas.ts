import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioEmpresas {
  private readonly urlBase = `${environment.urlApi}/empresas`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<EmpresaResumen[]> {
    return this.http.get<EmpresaResumen[]>(this.urlBase);
  }

  obtener(id: number): Observable<EmpresaResumen> {
    return this.http.get<EmpresaResumen>(`${this.urlBase}/${id}`);
  }

  crear(solicitud: SolicitudCrearEmpresa): Observable<EmpresaResumen> {
    return this.http.post<EmpresaResumen>(this.urlBase, solicitud);
  }

  actualizar(id: number, solicitud: SolicitudActualizarEmpresa): Observable<EmpresaResumen> {
    return this.http.put<EmpresaResumen>(`${this.urlBase}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }
}

export interface EmpresaResumen {
  id: number;
  nombre: string;
  razonSocial: string | null;
  cuit: string | null;
  email: string | null;
  telefono: string | null;
  estado: string;
}

export interface SolicitudCrearEmpresa {
  nombre: string;
  razonSocial?: string;
  cuit?: string;
  email?: string;
  telefono?: string;
}

export interface SolicitudActualizarEmpresa {
  nombre: string;
  razonSocial?: string;
  cuit?: string;
  email?: string;
  telefono?: string;
}
