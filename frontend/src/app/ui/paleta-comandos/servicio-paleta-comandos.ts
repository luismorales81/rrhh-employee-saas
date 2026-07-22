import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ServicioPermisos, ItemMenu } from '../../nucleo/servicios/servicio-permisos';

export interface ComandoPaleta {
  id: string;
  etiqueta: string;
  categoria: 'navegacion' | 'accion' | 'reciente';
  ruta?: string;
  palabrasClave?: string[];
  ejecutar?: () => void;
}

@Injectable({ providedIn: 'root' })
export class ServicioPaletaComandos {
  readonly abierta = signal(false);
  readonly consulta = signal('');
  private readonly recientes = signal<ComandoPaleta[]>([]);

  constructor(
    private readonly router: Router,
    private readonly servicioPermisos: ServicioPermisos
  ) {}

  abrir(): void {
    this.consulta.set('');
    this.abierta.set(true);
  }

  cerrar(): void {
    this.abierta.set(false);
    this.consulta.set('');
  }

  alternar(): void {
    if (this.abierta()) {
      this.cerrar();
    } else {
      this.abrir();
    }
  }

  comandosDisponibles(): ComandoPaleta[] {
    let items: ItemMenu[];
    if (this.servicioPermisos.esPortalEmpleado()) {
      items = this.servicioPermisos.menuEmpleadoCompleto();
    } else if (this.servicioPermisos.esUsuarioPlataforma() && !this.servicioPermisos.esPortalRrhh()) {
      items = this.servicioPermisos.menuPlataforma();
    } else if (this.servicioPermisos.esPortalSupervisor()) {
      items = this.servicioPermisos.menuSupervisor();
    } else {
      items = this.servicioPermisos.menuRrhhPlano();
    }

    const navegacion: ComandoPaleta[] = items.map((item) => ({
      id: `nav-${item.ruta}`,
      etiqueta: `Ir a ${item.etiqueta}`,
      categoria: 'navegacion',
      ruta: item.ruta,
      palabrasClave: [item.etiqueta, item.ruta]
    }));

    const acciones: ComandoPaleta[] = [];
    if (this.servicioPermisos.tienePermiso('empleado.crear')) {
      acciones.push({
        id: 'accion-nuevo-empleado',
        etiqueta: 'Nuevo empleado',
        categoria: 'accion',
        ruta: '/empleados',
        palabrasClave: ['crear', 'alta', 'persona']
      });
    }
    if (this.servicioPermisos.tienePermiso('solicitud.crear') || this.servicioPermisos.tienePermiso('solicitud.ver')) {
      acciones.push({
        id: 'accion-solicitudes',
        etiqueta: 'Ver solicitudes',
        categoria: 'accion',
        ruta: this.servicioPermisos.esPortalEmpleado() ? '/mi-solicitudes' : '/solicitudes',
        palabrasClave: ['aprobar', 'pedido']
      });
    }

    return [...this.recientes(), ...acciones, ...navegacion];
  }

  filtrar(texto: string): ComandoPaleta[] {
    const q = texto.trim().toLowerCase();
    const todos = this.comandosDisponibles();
    if (!q) {
      return todos.slice(0, 12);
    }
    return todos
      .filter((c) => {
        const haystack = [c.etiqueta, ...(c.palabrasClave ?? [])].join(' ').toLowerCase();
        return haystack.includes(q);
      })
      .slice(0, 12);
  }

  ejecutar(comando: ComandoPaleta): void {
    this.registrarReciente(comando);
    this.cerrar();
    if (comando.ejecutar) {
      comando.ejecutar();
      return;
    }
    if (comando.ruta) {
      void this.router.navigateByUrl(comando.ruta);
    }
  }

  private registrarReciente(comando: ComandoPaleta): void {
    const actual = this.recientes().filter((c) => c.id !== comando.id);
    this.recientes.set(
      [{ ...comando, categoria: 'reciente' as const }, ...actual].slice(0, 5)
    );
  }
}
