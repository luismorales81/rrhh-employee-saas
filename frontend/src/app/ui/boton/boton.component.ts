import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';

export type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma' | 'enlace';
export type TamanoBoton = 'sm' | 'md' | 'lg';

@Component({
  selector: 'ui-boton',
  standalone: true,
  imports: [NgClass],
  template: `
    <button
      [attr.type]="tipo"
      class="ui-boton"
      [ngClass]="['ui-boton--' + variante, 'ui-boton--' + tamano]"
      [disabled]="deshabilitado || cargando"
      (click)="pulsado.emit($event)"
    >
      @if (cargando) {
        <span class="ui-boton__spinner" aria-hidden="true"></span>
      }
      <ng-content />
    </button>
  `,
  styles: `
    :host {
  display: inline-flex;
}

.ui-boton {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: var(--espacio-2);
      border-radius: var(--radio-md);
      font-family: var(--fuente-base);
      font-weight: 500;
      cursor: pointer;
      border: 1px solid transparent;
      transition:
        background var(--transicion-base),
        border-color var(--transicion-base),
        color var(--transicion-base),
        box-shadow var(--transicion-base);
    }
    .ui-boton:focus-visible {
      outline: none;
      box-shadow: var(--anillo-foco);
    }
    .ui-boton:disabled {
      opacity: 0.45;
      cursor: not-allowed;
      pointer-events: none;
    }
    .ui-boton--sm {
      padding: 0.375rem 0.75rem;
      font-size: var(--texto-caption);
    }
    .ui-boton--md {
      padding: 0.5rem 1rem;
      font-size: var(--texto-body);
    }
    .ui-boton--lg {
      padding: 0.75rem 1.25rem;
      font-size: 0.9375rem;
    }
    .ui-boton--primario {
      background: var(--color-primario);
      color: #fff;
    }
    .ui-boton--primario:hover:not(:disabled) {
      background: var(--color-primario-hover);
    }
    .ui-boton--secundario {
      background: var(--color-superficie);
      color: var(--color-texto);
      border-color: var(--color-borde);
    }
    .ui-boton--secundario:hover:not(:disabled) {
      background: var(--color-superficie-secundaria);
    }
    .ui-boton--peligro {
      background: var(--color-error-fondo);
      color: var(--color-error-texto);
      border-color: var(--color-error-borde);
    }
    .ui-boton--fantasma {
      background: transparent;
      color: var(--color-texto-secundario);
    }
    .ui-boton--fantasma:hover:not(:disabled) {
      background: var(--color-superficie-secundaria);
      color: var(--color-texto);
    }
    .ui-boton--enlace {
      background: transparent;
      color: var(--color-primario);
      padding-inline: 0;
    }
    .ui-boton__spinner {
      width: 0.875rem;
      height: 0.875rem;
      border: 2px solid currentColor;
      border-right-color: transparent;
      border-radius: 50%;
      animation: ui-girar 0.6s linear infinite;
    }
    @keyframes ui-girar {
      to {
        transform: rotate(360deg);
      }
    }
  `
})
export class BotonComponent {
  @Input() variante: VarianteBoton = 'primario';
  @Input() tamano: TamanoBoton = 'md';
  @Input() tipo: 'button' | 'submit' | 'reset' = 'button';
  @Input() deshabilitado = false;
  @Input() cargando = false;
  @Output() readonly pulsado = new EventEmitter<MouseEvent>();
}
