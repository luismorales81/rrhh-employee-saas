import { Component, computed, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioContextoEmpresa } from '../../../nucleo/servicios/servicio-contexto-empresa';
import { SeccionMenu, ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { IconoMenuComponent } from '../icono-menu/icono-menu.component';
import { PaletaComandosComponent } from '../../../ui/paleta-comandos/paleta-comandos.component';
import { ServicioPaletaComandos } from '../../../ui/paleta-comandos/servicio-paleta-comandos';

@Component({
  selector: 'app-layout-principal',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    IconoMenuComponent,
    PaletaComandosComponent
  ],
  templateUrl: './layout-principal.component.html',
  styleUrl: './layout-principal.component.scss'
})
export class LayoutPrincipalComponent {
  readonly esSupervisor = computed(() => this.servicioPermisos.esPortalSupervisor());
  readonly seccionesMenu = computed(() =>
    this.esSupervisor() ? null : this.servicioPermisos.menuRrhhSecciones()
  );
  readonly itemsSupervisor = computed(() =>
    this.esSupervisor() ? this.servicioPermisos.menuSupervisor() : []
  );
  readonly seccionesColapsadas = signal(new Set<string>());

  constructor(
    public readonly servicioAutenticacion: ServicioAutenticacion,
    public readonly servicioContextoEmpresa: ServicioContextoEmpresa,
    private readonly servicioPermisos: ServicioPermisos,
    private readonly router: Router,
    private readonly servicioPaleta: ServicioPaletaComandos
  ) {
    this.seccionesColapsadas.set(this.servicioPermisos.seccionesColapsadas());
  }

  seccionAbierta(seccion: SeccionMenu): boolean {
    return !this.seccionesColapsadas().has(seccion.id);
  }

  alternarSeccion(id: string): void {
    this.servicioPermisos.alternarSeccionColapsada(id);
    this.seccionesColapsadas.set(this.servicioPermisos.seccionesColapsadas());
  }

  abrirPaleta(): void {
    this.servicioPaleta.abrir();
  }

  cerrarSesion(): void {
    this.servicioAutenticacion.cerrarSesion();
  }

  salirModoSoporte(): void {
    this.servicioContextoEmpresa.desactivarModoSoporte();
    this.router.navigate(['/plataforma/empresas']);
  }
}
