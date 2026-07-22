import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, tap, catchError, map, finalize, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RespuestaAutenticacion,
  SesionUsuario,
  SolicitudLogin
} from '../modelos/autenticacion';

const CLAVE_SESION = 'rrhh_sesion_movil';

@Injectable({ providedIn: 'root' })
export class ServicioAutenticacion {
  readonly sesionActual = signal<SesionUsuario | null>(this.cargarSesion());

  private refrescoEnCurso: Observable<boolean> | null = null;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {
    this.descartarSesionInvalida();
  }

  iniciarSesion(credenciales: SolicitudLogin): Observable<RespuestaAutenticacion> {
    return this.http
      .post<RespuestaAutenticacion>(`${environment.urlApi}/auth/login`, credenciales)
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }

  refrescarSesion(): Observable<boolean> {
    const sesion = this.sesionActual();
    if (!sesion?.tokenRefresco || this.jwtExpirado(sesion.tokenRefresco)) {
      this.limpiarSesionLocal();
      return of(false);
    }

    if (!this.refrescoEnCurso) {
      this.refrescoEnCurso = this.http
        .post<RespuestaAutenticacion>(`${environment.urlApi}/auth/refrescar`, {
          tokenRefresco: sesion.tokenRefresco
        })
        .pipe(
          tap((respuesta) => this.guardarSesion(respuesta)),
          map(() => true),
          catchError(() => {
            this.limpiarSesionLocal();
            return of(false);
          }),
          finalize(() => {
            this.refrescoEnCurso = null;
          }),
          shareReplay(1)
        );
    }

    return this.refrescoEnCurso;
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
    this.sesionActual.set(null);
  }

  obtenerToken(): string | null {
    return this.sesionActual()?.tokenAcceso ?? null;
  }

  estaAutenticado(): boolean {
    return this.sesionActual() !== null;
  }

  tokenAccesoExpirado(): boolean {
    const token = this.obtenerToken();
    if (!token) {
      return true;
    }
    return this.jwtExpirado(token);
  }

  puedeRefrescarSesion(): boolean {
    const tokenRefresco = this.sesionActual()?.tokenRefresco;
    return !!tokenRefresco && !this.jwtExpirado(tokenRefresco);
  }

  private descartarSesionInvalida(): void {
    const sesion = this.sesionActual();
    if (!sesion) {
      return;
    }

    const accesoVencido = this.tokenAccesoExpirado();
    const refrescoInvalido = !this.puedeRefrescarSesion();

    if (accesoVencido && refrescoInvalido) {
      this.limpiarSesionLocal();
    }
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

  private jwtExpirado(token: string): boolean {
    try {
      const carga = token.split('.')[1];
      if (!carga) {
        return true;
      }
      const payload = JSON.parse(atob(carga)) as { exp?: number };
      if (!payload.exp) {
        return false;
      }
      const margenMs = 30_000;
      return Date.now() >= payload.exp * 1000 - margenMs;
    } catch {
      return true;
    }
  }
}
