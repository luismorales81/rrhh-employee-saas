import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioEmpleados {
  private readonly urlBase = `${environment.urlApi}/empleados`;

  constructor(private readonly http: HttpClient) {}

  obtenerPropio(): Observable<EmpleadoResumen> {
    return this.http.get<EmpleadoResumen>(`${this.urlBase}/propio`);
  }

  listar(): Observable<EmpleadoResumen[]> {
    return this.http.get<EmpleadoResumen[]>(this.urlBase);
  }

  obtenerLegajo(id: number): Observable<LegajoEmpleadoResumen> {
    return this.http.get<LegajoEmpleadoResumen>(`${this.urlBase}/${id}/legajo`);
  }
}

export interface LegajoEmpleadoResumen {
  empleadoId: number;
  estadoOnboarding: string | null;
  estadoOffboarding: string | null;
}

export interface EmpleadoResumen {
  id: number;
  nombre: string;
  apellido: string;
  numeroEmpleado: string | null;
  dni: string | null;
  cuil: string | null;
  fechaIngreso: string | null;
  estado: string;
  supervisorId: number | null;
  gerenciaId: number | null;
  areaId: number | null;
  sectorId: number | null;
  puestoId: number | null;
  legajoId: number | null;
}
