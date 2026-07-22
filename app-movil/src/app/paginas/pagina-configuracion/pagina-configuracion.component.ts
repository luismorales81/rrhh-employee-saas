import { Component, inject, OnInit, signal } from '@angular/core';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  PreferenciaTema,
  ServicioPreferencias
} from '../../nucleo/servicios/servicio-preferencias';

@Component({
  selector: 'app-pagina-configuracion',
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonSelect,
    IonSelectOption,
    EncabezadoPaginaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/tabs/perfil"></ion-back-button>
        </ion-buttons>
        <ion-title>Configuración</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Configuración"
        subtitulo="Ajustes de tu app"
      />

      <ion-list class="lista">
        <ion-item>
          <ion-select
            label="Apariencia"
            labelPlacement="stacked"
            interface="action-sheet"
            [value]="tema()"
            (ionChange)="cambiarTema($event.detail.value)"
          >
            <ion-select-option value="sistema">Igual que el sistema</ion-select-option>
            <ion-select-option value="claro">Claro</ion-select-option>
            <ion-select-option value="oscuro">Oscuro</ion-select-option>
          </ion-select>
        </ion-item>
        <ion-item>
          <ion-label>
            <h3>Versión de la app</h3>
            <p>{{ versionApp }}</p>
          </ion-label>
        </ion-item>
        <ion-item>
          <ion-label>
            <h3>Idioma</h3>
            <p>Español (Argentina)</p>
          </ion-label>
        </ion-item>
      </ion-list>
    </ion-content>
  `,
  styles: `
    .lista {
      border-radius: var(--radio-lg);
      overflow: hidden;
      border: 1px solid var(--color-borde-suave);
      background: var(--color-superficie);
    }

    h3 {
      margin: 0;
      font-size: 0.95rem;
      color: var(--color-texto);
    }

    p {
      margin: 0.2rem 0 0;
      color: var(--color-texto-secundario);
      font-size: 0.85rem;
    }
  `
})
export class PaginaConfiguracionComponent implements OnInit {
  private readonly preferencias = inject(ServicioPreferencias);

  readonly versionApp = '0.1.0';
  readonly tema = signal<PreferenciaTema>('sistema');

  ngOnInit(): void {
    this.tema.set(this.preferencias.tema());
  }

  cambiarTema(valor: PreferenciaTema): void {
    this.preferencias.guardarTema(valor);
    this.tema.set(valor);
  }
}
