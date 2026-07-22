import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { IconoMenuComponent } from '../icono-menu/icono-menu.component';
import { PaletaComandosComponent } from '../../../ui/paleta-comandos/paleta-comandos.component';
import { ServicioPaletaComandos } from '../../../ui/paleta-comandos/servicio-paleta-comandos';

@Component({
  selector: 'app-layout-empleado',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    IconoMenuComponent,
    PaletaComandosComponent
  ],
  templateUrl: './layout-empleado.component.html',
  styleUrl: './layout-empleado.component.scss'
})
export class LayoutEmpleadoComponent {
  private readonly servicioPaleta = inject(ServicioPaletaComandos);
  readonly itemsMenu = computed(() => this.servicioPermisos.menuEmpleado());
  readonly puedeVerNotificaciones = computed(() =>
    this.servicioPermisos.tienePermiso('notificacion.ver')
  );

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
