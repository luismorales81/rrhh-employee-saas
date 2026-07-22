import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RespuestaAutenticacion,
  SesionUsuario,
  SolicitudLogin
} from '../modelos/autenticacion';
import { ServicioContextoEmpresa } from './servicio-contexto-empresa';

const CLAVE_SESION = 'rrhh_sesion';

@Injectable({ providedIn: 'root' })
export class ServicioAutenticacion {
  readonly sesionActual = signal<SesionUsuario | null>(this.cargarSesion());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly servicioContextoEmpresa: ServicioContextoEmpresa
  ) {
    this.normalizarSesionAlmacenada();
    const sesion = this.sesionActual();
    if (sesion?.empresaId !== null && sesion?.empresaId !== undefined) {
      this.servicioContextoEmpresa.establecerDesdeSesion(sesion);
    }
  }

  private normalizarSesionAlmacenada(): void {
    const sesion = this.sesionActual();
    if (!sesion) {
      return;
    }

    const esPlataforma = this.esUsuarioPlataforma(sesion);
    if (!esPlataforma && this.servicioContextoEmpresa.modoSoporte()) {
      this.servicioContextoEmpresa.desactivarModoSoporte();
    }

    if (esPlataforma && !sesion.esSuperAdmin) {
      const normalizada: SesionUsuario = { ...sesion, esSuperAdmin: true };
      localStorage.setItem(CLAVE_SESION, JSON.stringify(normalizada));
      this.sesionActual.set(normalizada);
    }
  }

  private esUsuarioPlataforma(sesion: SesionUsuario): boolean {
    return sesion.esSuperAdmin || sesion.permisos.includes('empresa.crear');
  }

  iniciarSesion(credenciales: SolicitudLogin): Observable<RespuestaAutenticacion> {
    return this.http
      .post<RespuestaAutenticacion>(`${environment.urlApi}/auth/login`, credenciales)
      .pipe(
        tap((respuesta) => {
          this.guardarSesion(respuesta);
          if (respuesta.empresaId !== null) {
            this.servicioContextoEmpresa.establecerDesdeSesion(this.sesionActual()!);
          } else {
            this.servicioContextoEmpresa.limpiar();
          }
        })
      );
  }

  cerrarSesion(): void {
    const token = this.obtenerToken();
    if (token) {
      this.http.post(`${environment.urlApi}/auth/cerrar-sesion`, {}).subscribe({
        error: () => undefined
      });
    }
    this.limpiarSesionLocal();
    this.router.navigate(['/iniciar-sesion']);
  }

  limpiarSesionLocal(): void {
    localStorage.removeItem(CLAVE_SESION);
    this.servicioContextoEmpresa.limpiar();
    this.sesionActual.set(null);
  }

  obtenerToken(): string | null {
    return this.sesionActual()?.tokenAcceso ?? null;
  }

  estaAutenticado(): boolean {
    return this.sesionActual() !== null;
  }

  private guardarSesion(respuesta: RespuestaAutenticacion): void {
    const sesion: SesionUsuario = {
      tokenAcceso: respuesta.tokenAcceso,
      tokenRefresco: respuesta.tokenRefresco,
      usuarioId: respuesta.usuarioId,
      empresaId: respuesta.empresaId,
      empresaNombre: respuesta.empresaNombre ?? null,
      email: respuesta.email,
      esSuperAdmin: respuesta.esSuperAdmin ?? false,
      permisos: respuesta.permisos ?? []
    };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this.sesionActual.set(sesion);
  }

  private cargarSesion(): SesionUsuario | null {
    const datos = localStorage.getItem(CLAVE_SESION);
    if (!datos) {
      return null;
    }
    try {
      const sesion = JSON.parse(datos) as SesionUsuario;
      return {
        ...sesion,
        esSuperAdmin: sesion.esSuperAdmin ?? false,
        permisos: sesion.permisos ?? []
      };
    } catch {
      return null;
    }
  }
}
