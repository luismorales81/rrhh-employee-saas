import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';

interface EnlaceHub {
  ruta: string;
  titulo: string;
  descripcion: string;
  permiso?: string;
}

@Component({
  selector: 'app-pagina-mi-pedidos',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="pagina">
      <header class="encabezado-pagina">
        <h1>Pedidos</h1>
        <p>Solicitudes y vacaciones en un solo lugar</p>
      </header>
      <div class="lista-hub">
        @for (enlace of enlacesVisibles(); track enlace.ruta) {
          <a class="tarjeta-hub" [routerLink]="enlace.ruta">
            <strong>{{ enlace.titulo }}</strong>
            <span>{{ enlace.descripcion }}</span>
          </a>
        }
      </div>
    </div>
  `,
  styles: `
    .lista-hub {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }
    .tarjeta-hub {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-1);
      padding: var(--espacio-5);
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-lg);
      text-decoration: none;
      color: inherit;
      box-shadow: var(--sombra-sm);
      transition: border-color var(--transicion-base);
    }
    .tarjeta-hub:hover {
      border-color: var(--color-primario-borde);
    }
    .tarjeta-hub strong {
      font-size: 1rem;
      color: var(--color-texto);
    }
    .tarjeta-hub span {
      font-size: var(--texto-body);
      color: var(--color-texto-secundario);
    }
  `
})
export class PaginaMiPedidosComponent {
  private readonly enlaces: EnlaceHub[] = [
    {
      ruta: '/mi-solicitudes',
      titulo: 'Solicitudes',
      descripcion: 'Pedidos generales al equipo de personas',
      permiso: 'solicitud.crear'
    },
    {
      ruta: '/mi-vacaciones',
      titulo: 'Vacaciones',
      descripcion: 'Saldo, calendario y nuevas solicitudes',
      permiso: 'vacacion.solicitar'
    }
  ];

  constructor(private readonly servicioPermisos: ServicioPermisos) {}

  enlacesVisibles(): EnlaceHub[] {
    return this.enlaces.filter((e) => !e.permiso || this.servicioPermisos.tienePermiso(e.permiso));
  }
}
