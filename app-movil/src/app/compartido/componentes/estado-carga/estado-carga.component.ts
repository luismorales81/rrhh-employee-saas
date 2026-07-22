import { Component, input } from '@angular/core';
import { IonSpinner } from '@ionic/angular/standalone';

@Component({
  selector: 'app-estado-carga',
  standalone: true,
  imports: [IonSpinner],
  template: `
    @if (modo() === 'esqueleto') {
      <div class="esqueleto-grilla-metricas" role="status" aria-live="polite">
        @for (i of [1, 2, 3, 4]; track i) {
          <div class="esqueleto-tarjeta-carga">
            <div class="esqueleto-linea" style="width: 40%; height: 1.25rem;"></div>
            <div class="esqueleto-linea" style="width: 65%;"></div>
          </div>
        }
        <span class="solo-lectores">{{ mensaje() }}</span>
      </div>
    } @else if (modo() === 'lista') {
      <div class="esqueleto-lista" role="status" aria-live="polite">
        @for (i of [1, 2, 3, 4]; track i) {
          <div class="esqueleto-fila">
            <div class="esqueleto-linea" style="width: 55%; height: 1rem;"></div>
            <div class="esqueleto-linea" style="width: 40%;"></div>
            <div class="esqueleto-linea" style="width: 28%; height: 0.75rem; margin-bottom: 0;"></div>
          </div>
        }
        <span class="solo-lectores">{{ mensaje() }}</span>
      </div>
    } @else {
      <div class="estado-carga">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>{{ mensaje() }}</p>
      </div>
    }
  `,
  styles: `
    .estado-carga {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--espacio-3);
      padding: var(--espacio-8) var(--espacio-4);
      color: var(--color-texto-secundario);
    }

    .esqueleto-lista {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }

    .esqueleto-fila {
      background: var(--color-superficie);
      border-radius: var(--radio-lg);
      padding: var(--espacio-4) var(--espacio-5);
      border: 1px solid var(--color-borde-suave);
      min-height: 72px;
    }

    .solo-lectores {
      position: absolute;
      width: 1px;
      height: 1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
    }
  `
})
export class EstadoCargaComponent {
  readonly mensaje = input('Cargando...');
  /** `lista` para pantallas de listado; `esqueleto` para dashboards de métricas; `spinner` simple. */
  readonly modo = input<'lista' | 'esqueleto' | 'spinner'>('lista');
}
