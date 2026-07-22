import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioOnboarding {
  private readonly urlEmpleados = `${environment.urlApi}/empleados`;

  constructor(private readonly http: HttpClient) {}

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

export interface TareaOnboardingEmpleadoResumen {
  id: number;
  nombre: string;
  completada: boolean;
}
