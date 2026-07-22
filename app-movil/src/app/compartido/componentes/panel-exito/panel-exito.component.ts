import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonButton, IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { checkmarkCircle } from 'ionicons/icons';

addIcons({ checkmarkCircle });

@Component({
  selector: 'app-panel-exito',
  standalone: true,
  imports: [RouterLink, IonButton, IonIcon],
  template: `
    <div class="panel-exito" role="status">
      <ion-icon class="icono-exito" name="checkmark-circle" aria-hidden="true"></ion-icon>
      <h2>{{ titulo() }}</h2>
      <p>{{ mensaje() }}</p>
      @if (textoAccion()) {
        @if (rutaAccion()) {
          <ion-button expand="block" [routerLink]="rutaAccion()!">{{ textoAccion() }}</ion-button>
        } @else {
          <ion-button expand="block" type="button" (click)="accion.emit()">{{ textoAccion() }}</ion-button>
        }
      }
    </div>
  `,
  styles: `
    .panel-exito {
      text-align: center;
      padding: var(--espacio-8, 2rem) var(--espacio-4, 1rem);
      background: var(--color-superficie, #fff);
      border-radius: var(--radio-lg, 12px);
      border: 1px solid var(--color-borde-suave, #e2e8f0);
      animation: panel-exito-entrada 420ms ease-out;
    }

    .icono-exito {
      font-size: 3.5rem;
      color: var(--color-exito-icono, #1a9a74);
      margin-bottom: 0.75rem;
      animation: icono-exito-pop 480ms ease-out;
    }

    @keyframes panel-exito-entrada {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes icono-exito-pop {
      0% {
        transform: scale(0.7);
        opacity: 0.4;
      }
      70% {
        transform: scale(1.08);
      }
      100% {
        transform: scale(1);
        opacity: 1;
      }
    }

    h2 {
      margin: 0 0 0.5rem;
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--color-texto, #0f172a);
    }

    p {
      margin: 0 0 1.25rem;
      font-size: 0.95rem;
      line-height: 1.5;
      color: var(--color-texto-secundario, #64748b);
    }
  `
})
export class PanelExitoComponent {
  readonly titulo = input.required<string>();
  readonly mensaje = input.required<string>();
  readonly textoAccion = input<string | null>(null);
  readonly rutaAccion = input<string | null>(null);
  readonly accion = output<void>();
}
