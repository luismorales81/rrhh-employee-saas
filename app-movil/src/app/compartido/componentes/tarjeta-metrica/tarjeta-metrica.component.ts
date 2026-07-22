import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonIcon } from '@ionic/angular/standalone';
import { DirectivaLiberarFocoNavegacion } from '../../directivas/directiva-liberar-foco-navegacion';
import { addIcons } from 'ionicons';
import {
  calendarOutline,
  documentTextOutline,
  folderOutline,
  notificationsOutline,
  cashOutline,
  megaphoneOutline,
  laptopOutline,
  checkmarkDoneOutline,
  gridOutline,
  medkitOutline,
  timeOutline
} from 'ionicons/icons';

addIcons({
  calendarOutline,
  documentTextOutline,
  folderOutline,
  notificationsOutline,
  cashOutline,
  megaphoneOutline,
  laptopOutline,
  checkmarkDoneOutline,
  gridOutline,
  medkitOutline,
  timeOutline
});

@Component({
  selector: 'app-tarjeta-metrica',
  standalone: true,
  imports: [RouterLink, IonIcon, DirectivaLiberarFocoNavegacion],
  template: `
    @if (ruta()) {
      <a
        class="tarjeta"
        [class.alerta]="alerta()"
        [routerLink]="ruta()!"
      >
        <span class="icono-contenedor">
          <ion-icon [name]="icono()" aria-hidden="true"></ion-icon>
        </span>
        <div class="contenido">
          <span class="valor">{{ valor() }}</span>
          <span class="titulo">{{ titulo() }}</span>
        </div>
      </a>
    } @else {
      <div class="tarjeta" [class.alerta]="alerta()">
        <span class="icono-contenedor">
          <ion-icon [name]="icono()" aria-hidden="true"></ion-icon>
        </span>
        <div class="contenido">
          <span class="valor">{{ valor() }}</span>
          <span class="titulo">{{ titulo() }}</span>
        </div>
      </div>
    }
  `,
  styles: `
    .tarjeta {
      display: flex;
      align-items: flex-start;
      gap: var(--espacio-3);
      background: var(--color-superficie);
      border-radius: var(--radio-xl);
      padding: var(--espacio-5) var(--espacio-4);
      text-decoration: none;
      color: inherit;
      border: 1px solid var(--color-borde-suave);
      box-shadow: var(--sombra-sm);
      min-height: 88px;
      transition: border-color 0.12s, box-shadow 0.12s;
    }

    .tarjeta.alerta {
      border-color: var(--color-alerta-borde);
      background: linear-gradient(to bottom, var(--color-alerta-fondo) 0%, var(--color-superficie) 45%);
    }

    .icono-contenedor {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 2.5rem;
      height: 2.5rem;
      border-radius: var(--radio-lg);
      background: var(--color-primario-suave);
      color: var(--color-primario);
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .contenido {
      display: flex;
      flex-direction: column;
      gap: 0.125rem;
      min-width: 0;
    }

    .valor {
      font-size: 1.375rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      line-height: 1.2;
      color: var(--color-texto);
    }

    .titulo {
      font-size: 0.8125rem;
      font-weight: 500;
      color: var(--color-texto-secundario);
      line-height: 1.3;
    }
  `
})
export class TarjetaMetricaComponent {
  readonly titulo = input.required<string>();
  readonly valor = input.required<string | number>();
  readonly icono = input.required<string>();
  readonly ruta = input<string | null>(null);
  readonly alerta = input(false);
}
