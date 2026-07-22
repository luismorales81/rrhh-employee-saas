import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioOffboarding {
  private readonly urlBase = `${environment.urlApi}/offboarding`;
  private readonly urlEmpleados = `${environment.urlApi}/empleados`;

  constructor(private readonly http: HttpClient) {}

  listarPlantillas(): Observable<PlantillaTareaOffboardingResumen[]> {
    return this.http.get<PlantillaTareaOffboardingResumen[]>(`${this.urlBase}/plantillas`);
  }

  crearPlantilla(solicitud: SolicitudCrearPlantillaTareaOffboarding): Observable<PlantillaTareaOffboardingResumen> {
    return this.http.post<PlantillaTareaOffboardingResumen>(`${this.urlBase}/plantillas`, solicitud);
  }

  actualizarPlantilla(id: number, solicitud: SolicitudActualizarPlantillaTareaOffboarding): Observable<PlantillaTareaOffboardingResumen> {
    return this.http.put<PlantillaTareaOffboardingResumen>(`${this.urlBase}/plantillas/${id}`, solicitud);
  }

  eliminarPlantilla(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/plantillas/${id}`);
  }

  listarTareasEmpleado(empleadoId: number): Observable<TareaOffboardingEmpleadoResumen[]> {
    return this.http.get<TareaOffboardingEmpleadoResumen[]>(
      `${this.urlEmpleados}/${empleadoId}/offboarding/tareas`
    );
  }

  completarTarea(empleadoId: number, tareaId: number, notas?: string): Observable<TareaOffboardingEmpleadoResumen> {
    return this.http.post<TareaOffboardingEmpleadoResumen>(
      `${this.urlEmpleados}/${empleadoId}/offboarding/tareas/${tareaId}/completar`,
      notas ? { notas } : {}
    );
  }
}

export interface PlantillaTareaOffboardingResumen {
  id: number;
  nombre: string;
  orden: number;
  obligatoria: boolean;
  tipo: string;
}

export interface SolicitudCrearPlantillaTareaOffboarding {
  nombre: string;
  orden: number;
  obligatoria: boolean;
  tipo: string;
}

export interface SolicitudActualizarPlantillaTareaOffboarding {
  nombre?: string;
  orden?: number;
  obligatoria?: boolean;
  tipo?: string;
}

export interface TareaOffboardingEmpleadoResumen {
  id: number;
  plantillaId: number;
  nombre: string;
  orden: number;
  obligatoria: boolean;
  tipo: string;
  completada: boolean;
  fechaCompletado: string | null;
  notas: string | null;
}
