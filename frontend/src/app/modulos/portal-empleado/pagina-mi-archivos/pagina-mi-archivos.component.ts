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
  selector: 'app-pagina-mi-archivos',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="pagina">
      <header class="encabezado-pagina">
        <h1>Documentos</h1>
        <p>Archivos y recibos para firmar o consultar</p>
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
    }
    .tarjeta-hub:hover {
      border-color: var(--color-primario-borde);
    }
    .tarjeta-hub strong {
      font-size: 1rem;
    }
    .tarjeta-hub span {
      font-size: var(--texto-body);
      color: var(--color-texto-secundario);
    }
  `
})
export class PaginaMiArchivosComponent {
  private readonly enlaces: EnlaceHub[] = [
    {
      ruta: '/mi-documentos',
      titulo: 'Documentos',
      descripcion: 'Contratos y documentos a firmar',
      permiso: 'documento.mis.ver'
    },
    {
      ruta: '/mis-recibos',
      titulo: 'Recibos',
      descripcion: 'Recibos de sueldo y conformidad',
      permiso: 'recibo.mis.ver'
    }
  ];

  constructor(private readonly servicioPermisos: ServicioPermisos) {}

  enlacesVisibles(): EnlaceHub[] {
    return this.enlaces.filter((e) => !e.permiso || this.servicioPermisos.tienePermiso(e.permiso));
  }
}
