import { Component, input } from '@angular/core';
import { TonoEstado } from '../../../nucleo/utilidades/etiquetas-estado';

@Component({
  selector: 'app-chip-estado',
  standalone: true,
  template: `
    <span class="chip" [class]="'chip--' + tono()">{{ texto() }}</span>
  `,
  styles: `
    .chip {
      display: inline-flex;
      align-items: center;
      max-width: 100%;
      font-size: 0.75rem;
      font-weight: 600;
      line-height: 1.2;
      padding: 0.25rem 0.625rem;
      border-radius: var(--radio-completo);
      white-space: nowrap;
    }

    .chip--neutro {
      background: var(--color-superficie-secundaria);
      color: var(--color-texto-secundario);
    }

    .chip--alerta {
      background: var(--color-alerta-fondo);
      color: var(--color-alerta-texto);
    }

    .chip--exito {
      background: var(--color-exito-fondo);
      color: var(--color-exito-texto);
    }

    .chip--error {
      background: var(--color-error-fondo);
      color: var(--color-error-texto);
    }

    .chip--info {
      background: var(--color-info-fondo);
      color: var(--color-info-texto);
    }
  `
})
export class ChipEstadoComponent {
  readonly texto = input.required<string>();
  readonly tono = input<TonoEstado>('neutro');
}
