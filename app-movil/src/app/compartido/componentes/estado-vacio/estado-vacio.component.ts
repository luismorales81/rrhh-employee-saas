import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonButton, IonIcon } from '@ionic/angular/standalone';
import { DirectivaLiberarFocoNavegacion } from '../../directivas/directiva-liberar-foco-navegacion';
import { addIcons } from 'ionicons';
import {
  documentTextOutline,
  folderOutline,
  notificationsOutline,
  calendarOutline,
  megaphoneOutline,
  cashOutline,
  laptopOutline,
  checkmarkDoneOutline,
  medkitOutline
} from 'ionicons/icons';

addIcons({
  documentTextOutline,
  folderOutline,
  notificationsOutline,
  calendarOutline,
  megaphoneOutline,
  cashOutline,
  laptopOutline,
  checkmarkDoneOutline,
  medkitOutline
});

@Component({
  selector: 'app-estado-vacio',
  standalone: true,
  imports: [RouterLink, IonButton, IonIcon, DirectivaLiberarFocoNavegacion],
  template: `
    <div class="estado-vacio">
      @if (icono()) {
        <div class="icono">
          <ion-icon [name]="icono()!" aria-hidden="true"></ion-icon>
        </div>
      }
      <h3>{{ titulo() }}</h3>
      @if (descripcion()) {
        <p>{{ descripcion() }}</p>
      }
      @if (textoAccion() && rutaAccion()) {
        <ion-button [routerLink]="rutaAccion()!" size="small" color="primary">{{ textoAccion() }}</ion-button>
      }
    </div>
  `,
  styles: `
    .estado-vacio {
      text-align: center;
      padding: var(--espacio-10) var(--espacio-5);
      background: var(--color-superficie);
      border-radius: var(--radio-lg);
      border: 1px solid var(--color-borde-suave);
    }

    .icono {
      width: 3rem;
      height: 3rem;
      margin: 0 auto var(--espacio-4);
      border-radius: var(--radio-lg);
      background: var(--color-superficie-secundaria);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: var(--color-texto-terciario);
    }

    h3 {
      margin: 0 0 var(--espacio-2);
      color: var(--color-texto);
      font-size: 1rem;
      font-weight: 600;
    }

    p {
      margin: 0 0 var(--espacio-4);
      font-size: 0.9375rem;
      line-height: 1.5;
      color: var(--color-texto-secundario);
      max-width: 20rem;
      margin-left: auto;
      margin-right: auto;
    }
  `
})
export class EstadoVacioComponent {
  readonly titulo = input.required<string>();
  readonly descripcion = input<string | null>(null);
  readonly icono = input<string | null>(null);
  readonly textoAccion = input<string | null>(null);
  readonly rutaAccion = input<string | null>(null);
}
