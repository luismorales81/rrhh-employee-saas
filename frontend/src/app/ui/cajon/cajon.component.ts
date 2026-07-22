import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'ui-cajon',
  standalone: true,
  template: `
    @if (abierto) {
      <div class="ui-cajon-fondo" (click)="cerrarSiFondo()" role="presentation"></div>
      <aside
        class="ui-cajon"
        [class.ui-cajon--ancho]="ancho === 'lg'"
        role="dialog"
        [attr.aria-label]="titulo || 'Panel lateral'"
        aria-modal="true"
      >
        <header class="ui-cajon__cabecera">
          <div>
            @if (titulo) {
              <h2 class="ui-cajon__titulo">{{ titulo }}</h2>
            }
            @if (subtitulo) {
              <p class="ui-cajon__subtitulo">{{ subtitulo }}</p>
            }
          </div>
          <button type="button" class="ui-cajon__cerrar" (click)="cerrar.emit()" aria-label="Cerrar">
            ×
          </button>
        </header>
        <div class="ui-cajon__cuerpo">
          <ng-content />
        </div>
        <footer class="ui-cajon__pie">
          <ng-content select="[pie]" />
        </footer>
      </aside>
    }
  `,
  styles: `
    .ui-cajon-fondo {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.35);
      z-index: var(--z-cajon);
      animation: ui-fade 160ms ease;
    }
    .ui-cajon {
      position: fixed;
      top: 0;
      right: 0;
      bottom: 0;
      width: min(100vw, var(--ancho-cajon));
      background: var(--color-superficie);
      z-index: calc(var(--z-cajon) + 1);
      display: flex;
      flex-direction: column;
      box-shadow: var(--sombra-xl);
      animation: ui-deslizar 180ms ease;
    }
    .ui-cajon--ancho {
      width: min(100vw, 560px);
    }
    .ui-cajon__cabecera {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: var(--espacio-4);
      padding: var(--espacio-5) var(--espacio-6);
      border-bottom: 1px solid var(--color-borde-suave);
    }
    .ui-cajon__titulo {
      margin: 0;
      font-size: var(--texto-h2);
      font-weight: 600;
      color: var(--color-texto);
    }
    .ui-cajon__subtitulo {
      margin: var(--espacio-1) 0 0;
      font-size: var(--texto-body);
      color: var(--color-texto-secundario);
    }
    .ui-cajon__cerrar {
      border: none;
      background: transparent;
      font-size: 1.5rem;
      line-height: 1;
      color: var(--color-texto-terciario);
      cursor: pointer;
      padding: var(--espacio-1);
      border-radius: var(--radio-sm);
    }
    .ui-cajon__cerrar:hover {
      background: var(--color-superficie-secundaria);
      color: var(--color-texto);
    }
    .ui-cajon__cuerpo {
      flex: 1;
      overflow: auto;
      padding: var(--espacio-6);
    }
    .ui-cajon__pie {
      display: flex;
      gap: var(--espacio-3);
      justify-content: flex-end;
      padding: var(--espacio-4) var(--espacio-6);
      border-top: 1px solid var(--color-borde-suave);
      background: var(--color-superficie);
    }
    @keyframes ui-fade {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }
    @keyframes ui-deslizar {
      from {
        transform: translateX(100%);
      }
      to {
        transform: translateX(0);
      }
    }
  `
})
export class CajonComponent {
  @Input() abierto = false;
  @Input() titulo = '';
  @Input() subtitulo = '';
  @Input() ancho: 'md' | 'lg' = 'md';
  @Input() cerrarAlFondo = true;
  @Output() readonly cerrado = new EventEmitter<void>();
  @Output() readonly cerrar = this.cerrado;

  @HostListener('document:keydown.escape')
  alEscape(): void {
    if (this.abierto) {
      this.cerrar.emit();
    }
  }

  cerrarSiFondo(): void {
    if (this.cerrarAlFondo) {
      this.cerrar.emit();
    }
  }
}
