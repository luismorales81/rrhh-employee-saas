import { Component, OnInit, inject, computed } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { IonIcon, IonLabel, IonRouterOutlet } from '@ionic/angular/standalone';
import { filter } from 'rxjs/operators';
import { addIcons } from 'ionicons';
import {
  homeOutline,
  home,
  documentTextOutline,
  folderOutline,
  personOutline,
  person,
  gridOutline,
  timeOutline,
  time,
  newspaperOutline,
  newspaper
} from 'ionicons/icons';
import { BannerConexionApiComponent } from '../compartido/componentes/banner-conexion-api/banner-conexion-api.component';
import { DirectivaLiberarFocoNavegacion } from '../compartido/directivas/directiva-liberar-foco-navegacion';
import { environment } from '../../environments/environment';
import { ServicioContadoresTabs } from '../nucleo/servicios/servicio-contadores-tabs';

addIcons({
  homeOutline,
  home,
  documentTextOutline,
  folderOutline,
  personOutline,
  person,
  gridOutline,
  timeOutline,
  time,
  newspaperOutline,
  newspaper
});

interface ItemTabInferior {
  ruta: string;
  etiqueta: string;
  icono: string;
  iconoActivo: string;
  claveBadge?: 'papeles' | 'notificaciones';
}

@Component({
  selector: 'app-layout-tabs',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    IonIcon,
    IonLabel,
    IonRouterOutlet,
    BannerConexionApiComponent,
    DirectivaLiberarFocoNavegacion
  ],
  template: `
    <app-banner-conexion-api />
    <div class="area-contenido-tabs">
      <ion-router-outlet></ion-router-outlet>
    </div>
    <nav class="barra-tabs-inferior" aria-label="Navegación principal">
      @for (item of itemsTab; track item.ruta) {
        <a
          class="tab-item"
          [routerLink]="item.ruta"
          routerLinkActive="activo"
          [attr.aria-label]="etiquetaConBadge(item)"
        >
          <span class="icono-wrap">
            <ion-icon
              [name]="estaActivo(item.ruta) ? item.iconoActivo : item.icono"
              aria-hidden="true"
            ></ion-icon>
            @if (badgeDe(item); as cantidad) {
              @if (cantidad > 0) {
                <span class="badge-tab">{{ cantidad > 9 ? '9+' : cantidad }}</span>
              }
            }
          </span>
          <ion-label>{{ item.etiqueta }}</ion-label>
        </a>
      }
    </nav>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    .icono-wrap {
      position: relative;
      display: inline-flex;
      line-height: 0;
    }

    .badge-tab {
      position: absolute;
      top: -6px;
      right: -10px;
      min-width: 1.1rem;
      height: 1.1rem;
      padding: 0 0.25rem;
      border-radius: 999px;
      background: var(--color-acento);
      color: #fff;
      font-size: 0.625rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
    }
  `
})
export class LayoutTabsComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly servicioContadores = inject(ServicioContadoresTabs);

  private readonly itemsV1: ItemTabInferior[] = [
    { ruta: '/tabs/inicio', etiqueta: 'Inicio', icono: 'home-outline', iconoActivo: 'home' },
    { ruta: '/tabs/solicitudes', etiqueta: 'Pedidos', icono: 'document-text-outline', iconoActivo: 'document-text-outline' },
    { ruta: '/tabs/documentos', etiqueta: 'Documentos', icono: 'folder-outline', iconoActivo: 'folder-outline' },
    { ruta: '/tabs/mas', etiqueta: 'Más', icono: 'grid-outline', iconoActivo: 'grid-outline' },
    { ruta: '/tabs/perfil', etiqueta: 'Perfil', icono: 'person-outline', iconoActivo: 'person', claveBadge: 'notificaciones' }
  ];

  private readonly itemsV2: ItemTabInferior[] = [
    { ruta: '/tabs/inicio', etiqueta: 'Inicio', icono: 'home-outline', iconoActivo: 'home' },
    { ruta: '/tabs/mi-tiempo', etiqueta: 'Mi tiempo', icono: 'time-outline', iconoActivo: 'time' },
    { ruta: '/tabs/mis-papeles', etiqueta: 'Papeles', icono: 'newspaper-outline', iconoActivo: 'newspaper', claveBadge: 'papeles' },
    { ruta: '/tabs/perfil', etiqueta: 'Perfil', icono: 'person-outline', iconoActivo: 'person', claveBadge: 'notificaciones' }
  ];

  readonly itemsTab = environment.navegacionV2 ? this.itemsV2 : this.itemsV1;
  readonly contadores = this.servicioContadores.contadores;
  private rutaActual = '';

  ngOnInit(): void {
    this.servicioContadores.refrescar();
    this.rutaActual = this.router.url;
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe((e) => {
      this.rutaActual = e.urlAfterRedirects;
      if (
        e.urlAfterRedirects.includes('/tabs/inicio')
        || e.urlAfterRedirects.includes('/tabs/mis-papeles')
        || e.urlAfterRedirects.includes('/tabs/perfil')
      ) {
        this.servicioContadores.refrescar();
      }
    });
  }

  estaActivo(ruta: string): boolean {
    return this.rutaActual === ruta || this.rutaActual.startsWith(ruta + '/');
  }

  badgeDe(item: ItemTabInferior): number {
    if (!item.claveBadge) {
      return 0;
    }
    return this.contadores()[item.claveBadge] ?? 0;
  }

  etiquetaConBadge(item: ItemTabInferior): string {
    const n = this.badgeDe(item);
    return n > 0 ? `${item.etiqueta}, ${n} pendientes` : item.etiqueta;
  }
}
