import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioOnboarding {
  private readonly urlBase = `${environment.urlApi}/onboarding`;
  private readonly urlEmpleados = `${environment.urlApi}/empleados`;

  constructor(private readonly http: HttpClient) {}

  listarPlantillas(): Observable<PlantillaTareaOnboardingResumen[]> {
    return this.http.get<PlantillaTareaOnboardingResumen[]>(`${this.urlBase}/plantillas`);
  }

  crearPlantilla(solicitud: SolicitudCrearPlantillaTareaOnboarding): Observable<PlantillaTareaOnboardingResumen> {
    return this.http.post<PlantillaTareaOnboardingResumen>(`${this.urlBase}/plantillas`, solicitud);
  }

  actualizarPlantilla(id: number, solicitud: SolicitudActualizarPlantillaTareaOnboarding): Observable<PlantillaTareaOnboardingResumen> {
    return this.http.put<PlantillaTareaOnboardingResumen>(`${this.urlBase}/plantillas/${id}`, solicitud);
  }

  eliminarPlantilla(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/plantillas/${id}`);
  }

  listarTareasEmpleado(empleadoId: number): Observable<TareaOnboardingEmpleadoResumen[]> {
    return this.http.get<TareaOnboardingEmpleadoResumen[]>(
      `${this.urlEmpleados}/${empleadoId}/onboarding/tareas`
    );
  }

  completarTarea(empleadoId: number, tareaId: number, notas?: string): Observable<TareaOnboardingEmpleadoResumen> {
    return this.http.post<TareaOnboardingEmpleadoResumen>(
      `${this.urlEmpleados}/${empleadoId}/onboarding/tareas/${tareaId}/completar`,
      notas ? { notas } : {}
    );
  }
}

export interface PlantillaTareaOnboardingResumen {
  id: number;
  nombre: string;
  orden: number;
  obligatoria: boolean;
  tipo: string;
}

export interface SolicitudCrearPlantillaTareaOnboarding {
  nombre: string;
  orden: number;
  obligatoria: boolean;
  tipo: string;
}

export interface SolicitudActualizarPlantillaTareaOnboarding {
  nombre?: string;
  orden?: number;
  obligatoria?: boolean;
  tipo?: string;
}

export interface TareaOnboardingEmpleadoResumen {
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
