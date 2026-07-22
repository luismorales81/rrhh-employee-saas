import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonIcon, IonLabel, IonRouterOutlet } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  gridOutline,
  laptopOutline,
  statsChartOutline,
  documentTextOutline,
  personOutline
} from 'ionicons/icons';
import { BannerConexionApiComponent } from '../compartido/componentes/banner-conexion-api/banner-conexion-api.component';
import { DirectivaLiberarFocoNavegacion } from '../compartido/directivas/directiva-liberar-foco-navegacion';

addIcons({
  gridOutline,
  laptopOutline,
  statsChartOutline,
  documentTextOutline,
  personOutline
});

interface ItemTabInferior {
  ruta: string;
  etiqueta: string;
  icono: string;
}

@Component({
  selector: 'app-layout-tabs-rrhh',
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
    <nav class="barra-tabs-inferior">
      @for (item of itemsTab; track item.ruta) {
        <a
          class="tab-item"
          [routerLink]="item.ruta"
          routerLinkActive="activo"
        >
          <ion-icon [name]="item.icono" aria-hidden="true"></ion-icon>
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
  `
})
export class LayoutTabsRrhhComponent {
  readonly itemsTab: ItemTabInferior[] = [
    { ruta: '/tabs-rrhh/panel', etiqueta: 'Inicio', icono: 'grid-outline' },
    { ruta: '/tabs-rrhh/activos', etiqueta: 'Activos', icono: 'laptop-outline' },
    { ruta: '/tabs-rrhh/novedades', etiqueta: 'Novedades', icono: 'stats-chart-outline' },
    { ruta: '/tabs-rrhh/solicitudes', etiqueta: 'Solicitudes', icono: 'document-text-outline' },
    { ruta: '/tabs-rrhh/perfil', etiqueta: 'Perfil', icono: 'person-outline' }
  ];
}
