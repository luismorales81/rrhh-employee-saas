import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { ComandoPaleta, ServicioPaletaComandos } from './servicio-paleta-comandos';

@Component({
  selector: 'ui-paleta-comandos',
  standalone: true,
  template: `
    @if (servicio.abierta()) {
      <div class="ui-paleta-fondo" (click)="servicio.cerrar()" role="presentation"></div>
      <div
        class="ui-paleta"
        role="dialog"
        aria-modal="true"
        aria-label="Paleta de comandos"
      >
        <input
          #campo
          class="ui-paleta__input"
          type="search"
          placeholder="Buscar pantallas y acciones…"
          [value]="servicio.consulta()"
          (input)="alEscribir($event)"
          (keydown)="alTecla($event)"
          autofocus
        />
        <ul class="ui-paleta__lista" role="listbox">
          @for (comando of resultados(); track comando.id; let i = $index) {
            <li>
              <button
                type="button"
                class="ui-paleta__item"
                [class.activo]="i === indiceActivo()"
                role="option"
                [attr.aria-selected]="i === indiceActivo()"
                (click)="servicio.ejecutar(comando)"
                (mouseenter)="indiceActivo.set(i)"
              >
                <span class="ui-paleta__etiqueta">{{ comando.etiqueta }}</span>
                <span class="ui-paleta__cat">{{ etiquetaCategoria(comando) }}</span>
              </button>
            </li>
          } @empty {
            <li class="ui-paleta__vacio">Sin resultados</li>
          }
        </ul>
        <footer class="ui-paleta__pie">
          <span><kbd>↑</kbd><kbd>↓</kbd> navegar</span>
          <span><kbd>Enter</kbd> abrir</span>
          <span><kbd>Esc</kbd> cerrar</span>
        </footer>
      </div>
    }
  `,
  styles: `
    .ui-paleta-fondo {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.4);
      z-index: var(--z-paleta);
    }
    .ui-paleta {
      position: fixed;
      top: 12vh;
      left: 50%;
      transform: translateX(-50%);
      width: min(560px, calc(100vw - 2rem));
      background: var(--color-superficie);
      border-radius: var(--radio-lg);
      box-shadow: var(--sombra-xl);
      z-index: calc(var(--z-paleta) + 1);
      overflow: hidden;
      border: 1px solid var(--color-borde);
    }
    .ui-paleta__input {
      width: 100%;
      border: none;
      border-bottom: 1px solid var(--color-borde-suave);
      padding: var(--espacio-4) var(--espacio-5);
      font-size: 1rem;
      font-family: var(--fuente-base);
      background: transparent;
      color: var(--color-texto);
      outline: none;
    }
    .ui-paleta__lista {
      list-style: none;
      margin: 0;
      padding: var(--espacio-2);
      max-height: 320px;
      overflow: auto;
    }
    .ui-paleta__item {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: var(--espacio-3);
      padding: var(--espacio-3) var(--espacio-3);
      border: none;
      border-radius: var(--radio-md);
      background: transparent;
      cursor: pointer;
      font-family: var(--fuente-base);
      text-align: left;
      color: var(--color-texto);
    }
    .ui-paleta__item.activo,
    .ui-paleta__item:hover {
      background: var(--color-primario-suave);
    }
    .ui-paleta__etiqueta {
      font-size: var(--texto-body);
      font-weight: 500;
    }
    .ui-paleta__cat {
      font-size: var(--texto-caption);
      color: var(--color-texto-terciario);
      text-transform: capitalize;
    }
    .ui-paleta__vacio {
      padding: var(--espacio-6);
      text-align: center;
      color: var(--color-texto-secundario);
      font-size: var(--texto-body);
    }
    .ui-paleta__pie {
      display: flex;
      gap: var(--espacio-4);
      padding: var(--espacio-3) var(--espacio-5);
      border-top: 1px solid var(--color-borde-suave);
      font-size: var(--texto-caption);
      color: var(--color-texto-terciario);
    }
    kbd {
      font-family: var(--fuente-base);
      background: var(--color-superficie-secundaria);
      border-radius: 4px;
      padding: 0 0.35rem;
      margin-right: 0.15rem;
    }
  `
})
export class PaletaComandosComponent {
  readonly servicio = inject(ServicioPaletaComandos);
  readonly indiceActivo = signal(0);

  readonly resultados = computed(() => this.servicio.filtrar(this.servicio.consulta()));

  @HostListener('document:keydown', ['$event'])
  alAtajoGlobal(evento: KeyboardEvent): void {
    if ((evento.ctrlKey || evento.metaKey) && evento.key.toLowerCase() === 'k') {
      evento.preventDefault();
      this.servicio.alternar();
      this.indiceActivo.set(0);
    }
  }

  alEscribir(evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    this.servicio.consulta.set(valor);
    this.indiceActivo.set(0);
  }

  alTecla(evento: KeyboardEvent): void {
    const total = this.resultados().length;
    if (evento.key === 'ArrowDown') {
      evento.preventDefault();
      this.indiceActivo.update((i) => (total === 0 ? 0 : (i + 1) % total));
    } else if (evento.key === 'ArrowUp') {
      evento.preventDefault();
      this.indiceActivo.update((i) => (total === 0 ? 0 : (i - 1 + total) % total));
    } else if (evento.key === 'Enter') {
      evento.preventDefault();
      const comando = this.resultados()[this.indiceActivo()];
      if (comando) {
        this.servicio.ejecutar(comando);
      }
    } else if (evento.key === 'Escape') {
      this.servicio.cerrar();
    }
  }

  etiquetaCategoria(comando: ComandoPaleta): string {
    if (comando.categoria === 'navegacion') return 'Ir a';
    if (comando.categoria === 'accion') return 'Acción';
    return 'Reciente';
  }
}
