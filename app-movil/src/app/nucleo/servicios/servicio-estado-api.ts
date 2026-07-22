import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { mensajeSinConexion } from '../utilidades/mensajes-error-producto';

@Injectable({ providedIn: 'root' })
export class ServicioEstadoApi {
  readonly conectada = signal<boolean | null>(null);
  readonly mensaje = signal<string | null>(null);

  constructor(private readonly http: HttpClient) {}

  verificar(): void {
    // /actuator/health no está habilitado en el backend; /api-docs es público (200 sin token).
    const urlSalud = environment.urlApi.startsWith('http')
      ? environment.urlApi.replace(/\/api\/?$/, '') + '/api-docs'
      : '/api-docs';

    this.http.get(urlSalud, { responseType: 'text' }).subscribe({
      next: () => {
        this.conectada.set(true);
        this.mensaje.set(null);
      },
      error: () => {
        this.conectada.set(false);
        this.mensaje.set(mensajeSinConexion());
      }
    });
  }
}
