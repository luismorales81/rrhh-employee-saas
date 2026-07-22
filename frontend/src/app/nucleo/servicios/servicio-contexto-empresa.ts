import { Injectable, signal } from '@angular/core';
import { SesionUsuario } from '../modelos/autenticacion';

const CLAVE_EMPRESA = 'rrhh_empresa_activa';
const CLAVE_MODO_SOPORTE = 'rrhh_modo_soporte';

@Injectable({ providedIn: 'root' })
export class ServicioContextoEmpresa {
  readonly empresaSeleccionadaId = signal<number | null>(this.cargarId());
  readonly empresaSeleccionadaNombre = signal<string | null>(this.cargarNombre());
  readonly modoSoporte = signal(this.cargarModoSoporte());

  establecerDesdeSesion(sesion: SesionUsuario): void {
    if (sesion.empresaId !== null) {
      this.establecer(sesion.empresaId, sesion.empresaNombre ?? this.empresaSeleccionadaNombre());
    }
  }

  establecer(id: number, nombre: string | null): void {
    localStorage.setItem(CLAVE_EMPRESA, JSON.stringify({ id, nombre }));
    this.empresaSeleccionadaId.set(id);
    this.empresaSeleccionadaNombre.set(nombre);
  }

  activarModoSoporte(id: number, nombre: string | null): void {
    this.establecer(id, nombre);
    localStorage.setItem(CLAVE_MODO_SOPORTE, 'true');
    this.modoSoporte.set(true);
  }

  desactivarModoSoporte(): void {
    localStorage.removeItem(CLAVE_MODO_SOPORTE);
    this.modoSoporte.set(false);
    localStorage.removeItem(CLAVE_EMPRESA);
    this.empresaSeleccionadaId.set(null);
    this.empresaSeleccionadaNombre.set(null);
  }

  limpiar(): void {
    localStorage.removeItem(CLAVE_EMPRESA);
    localStorage.removeItem(CLAVE_MODO_SOPORTE);
    this.empresaSeleccionadaId.set(null);
    this.empresaSeleccionadaNombre.set(null);
    this.modoSoporte.set(false);
  }

  obtenerEmpresaIdActiva(sesion: SesionUsuario | null): number | null {
    if (!sesion) {
      return null;
    }
    return sesion.empresaId ?? this.empresaSeleccionadaId();
  }

  esSuperAdminSinEmpresa(sesion: SesionUsuario | null): boolean {
    return sesion !== null && sesion.empresaId === null && !this.modoSoporte();
  }

  private cargarModoSoporte(): boolean {
    return localStorage.getItem(CLAVE_MODO_SOPORTE) === 'true';
  }

  private cargarId(): number | null {
    const datos = this.leerAlmacenamiento();
    return datos?.id ?? null;
  }

  private cargarNombre(): string | null {
    const datos = this.leerAlmacenamiento();
    return datos?.nombre ?? null;
  }

  private leerAlmacenamiento(): { id: number; nombre: string | null } | null {
    const raw = localStorage.getItem(CLAVE_EMPRESA);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
}
