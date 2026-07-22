import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { IconoMenuComponent } from '../icono-menu/icono-menu.component';
import { PaletaComandosComponent } from '../../../ui/paleta-comandos/paleta-comandos.component';
import { ServicioPaletaComandos } from '../../../ui/paleta-comandos/servicio-paleta-comandos';

@Component({
  selector: 'app-layout-plataforma',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    IconoMenuComponent,
    PaletaComandosComponent
  ],
  templateUrl: './layout-plataforma.component.html',
  styleUrl: './layout-plataforma.component.scss'
})
export class LayoutPlataformaComponent {
  private readonly servicioPaleta = inject(ServicioPaletaComandos);
  readonly itemsMenu = computed(() => this.servicioPermisos.menuPlataforma());

  constructor(
    public readonly servicioAutenticacion: ServicioAutenticacion,
    private readonly servicioPermisos: ServicioPermisos
  ) {}

  abrirPaleta(): void {
    this.servicioPaleta.abrir();
  }

  cerrarSesion(): void {
    this.servicioAutenticacion.cerrarSesion();
  }
}
