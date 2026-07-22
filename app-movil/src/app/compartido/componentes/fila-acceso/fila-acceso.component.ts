import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  timeOutline,
  calendarOutline,
  medkitOutline,
  cashOutline,
  folderOutline,
  documentTextOutline,
  notificationsOutline,
  megaphoneOutline,
  laptopOutline,
  chevronForwardOutline,
  checkmarkDoneOutline,
  helpCircleOutline,
  settingsOutline
} from 'ionicons/icons';

addIcons({
  timeOutline,
  calendarOutline,
  medkitOutline,
  cashOutline,
  folderOutline,
  documentTextOutline,
  notificationsOutline,
  megaphoneOutline,
  laptopOutline,
  chevronForwardOutline,
  checkmarkDoneOutline,
  helpCircleOutline,
  settingsOutline
});

@Component({
  selector: 'app-fila-acceso',
  standalone: true,
  imports: [RouterLink, IonIcon],
  template: `
    <a class="fila" [routerLink]="ruta()">
      <span class="icono" aria-hidden="true">
        <ion-icon [name]="icono()"></ion-icon>
      </span>
      <span class="textos">
        <strong>{{ titulo() }}</strong>
        @if (subtitulo()) {
          <span class="sub">{{ subtitulo() }}</span>
        }
      </span>
      @if (badge() != null && badge()! > 0) {
        <span class="badge">{{ badge() }}</span>
      }
      <ion-icon class="chevron" name="chevron-forward-outline" aria-hidden="true"></ion-icon>
    </a>
  `,
  styles: `
    .fila {
      display: flex;
      align-items: center;
      gap: var(--espacio-3);
      min-height: 56px;
      padding: var(--espacio-3) var(--espacio-4);
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-lg);
      text-decoration: none;
      color: inherit;
      box-shadow: var(--sombra-sm);
    }

    .icono {
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

    .textos {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 0.125rem;
    }

    strong {
      font-size: 0.9375rem;
      font-weight: 600;
      color: var(--color-texto);
    }

    .sub {
      font-size: 0.8125rem;
      color: var(--color-texto-secundario);
    }

    .badge {
      min-width: 1.5rem;
      height: 1.5rem;
      padding: 0 0.4rem;
      border-radius: var(--radio-completo);
      background: var(--color-alerta-fondo);
      color: var(--color-alerta-texto);
      font-size: 0.75rem;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    .chevron {
      color: var(--color-texto-terciario);
      font-size: 1rem;
      flex-shrink: 0;
    }
  `
})
export class FilaAccesoComponent {
  readonly titulo = input.required<string>();
  readonly subtitulo = input<string | null>(null);
  readonly icono = input.required<string>();
  readonly ruta = input.required<string>();
  readonly badge = input<number | null>(null);
}
