import {
  Component,
  Input,
  forwardRef,
  signal,
  computed
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface OpcionBuscador {
  id: number;
  etiqueta: string;
  detalle?: string | null;
}

@Component({
  selector: 'app-buscador-opcion',
  standalone: true,
  template: `
    <div class="buscador-opcion">
      @if (opcionSeleccionada(); as opcion) {
        <div class="opcion-seleccionada">
          <span class="etiqueta">{{ opcion.etiqueta }}</span>
          @if (opcion.detalle) {
            <span class="detalle-seleccionada">{{ opcion.detalle }}</span>
          }
          <button type="button" class="btn-limpiar" [disabled]="deshabilitado" (click)="limpiar()">
            Cambiar
          </button>
        </div>
      } @else {
        <input
          type="search"
          class="campo-busqueda"
          [placeholder]="placeholder"
          [disabled]="deshabilitado"
          [value]="textoBusqueda()"
          (input)="alEscribir($event)"
          (focus)="mostrarSugerencias.set(true)"
          (blur)="ocultarSugerenciasConRetraso()"
          autocomplete="off"
        />
        @if (mostrarSugerencias() && textoBusqueda().trim().length > 0) {
          @if (sugerencias().length > 0) {
            <ul class="lista-sugerencias" role="listbox">
              @for (opcion of sugerencias(); track opcion.id) {
                <li role="option" (mousedown)="seleccionar(opcion)">
                  <span class="etiqueta">{{ opcion.etiqueta }}</span>
                  @if (opcion.detalle) {
                    <span class="detalle">{{ opcion.detalle }}</span>
                  }
                </li>
              }
            </ul>
          } @else {
            <p class="sin-resultados">No se encontraron resultados</p>
          }
        } @else if (mostrarSugerencias() && textoBusqueda().trim().length === 0 && opciones.length > 0) {
          <p class="ayuda-busqueda">{{ textoAyuda }}</p>
        }
      }
    </div>
  `,
  styles: `
    .buscador-opcion {
      position: relative;
      width: 100%;
    }

    .campo-busqueda {
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--color-borde);
      border-radius: var(--radio-md);
      font-size: 0.875rem;
      font-family: inherit;
      background: var(--color-superficie);
    }

    .opcion-seleccionada {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.35rem 0.75rem;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--color-borde);
      border-radius: var(--radio-md);
      background: var(--color-fondo);
    }

    .opcion-seleccionada .etiqueta {
      font-weight: 500;
    }

    .detalle-seleccionada {
      font-size: 0.8125rem;
      color: var(--color-texto-secundario);
    }

    .btn-limpiar {
      margin-left: auto;
      border: none;
      background: transparent;
      color: var(--color-primario);
      cursor: pointer;
      font-size: 0.8125rem;
      white-space: nowrap;
      font-weight: 500;
    }

    .btn-limpiar:disabled {
      color: var(--color-texto-terciario);
      cursor: not-allowed;
    }

    .lista-sugerencias {
      position: absolute;
      z-index: 20;
      top: calc(100% + 4px);
      left: 0;
      right: 0;
      max-height: 240px;
      overflow-y: auto;
      margin: 0;
      padding: 0;
      list-style: none;
      border: 1px solid var(--color-borde);
      border-radius: var(--radio-md);
      background: var(--color-superficie);
      box-shadow: var(--sombra-lg);
    }

    .lista-sugerencias li {
      padding: 0.6rem 0.75rem;
      cursor: pointer;
      border-bottom: 1px solid var(--color-borde-suave);
    }

    .lista-sugerencias li:last-child {
      border-bottom: none;
    }

    .lista-sugerencias li:hover {
      background: var(--color-primario-suave);
    }

    .lista-sugerencias .etiqueta {
      display: block;
      font-weight: 500;
    }

    .lista-sugerencias .detalle {
      display: block;
      font-size: 0.75rem;
      color: var(--color-texto-secundario);
      margin-top: 0.15rem;
    }

    .sin-resultados,
    .ayuda-busqueda {
      margin: 0.35rem 0 0;
      font-size: 0.8125rem;
      color: var(--color-texto-terciario);
    }
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BuscadorOpcionComponent),
      multi: true
    }
  ]
})
export class BuscadorOpcionComponent implements ControlValueAccessor {
  @Input() opciones: OpcionBuscador[] = [];
  @Input() placeholder = 'Buscar...';
  @Input() textoAyuda = 'Escribí para filtrar opciones';

  readonly textoBusqueda = signal('');
  readonly mostrarSugerencias = signal(false);
  readonly valorInterno = signal<number | null>(null);
  deshabilitado = false;

  readonly opcionSeleccionada = computed(() => {
    const id = this.valorInterno();
    if (id == null) {
      return null;
    }
    return this.opciones.find((o) => o.id === id) ?? null;
  });

  readonly sugerencias = computed(() => {
    const texto = this.textoBusqueda().trim().toLowerCase();
    if (!texto) {
      return this.opciones.slice(0, 12);
    }
    return this.opciones
      .filter((o) => this.coincideBusqueda(o, texto))
      .slice(0, 12);
  });

  private notificarCambio: (valor: number | null) => void = () => {};
  private notificarTocado: () => void = () => {};

  writeValue(valor: number | string | null): void {
    if (valor === '' || valor == null) {
      this.valorInterno.set(null);
      return;
    }
    this.valorInterno.set(Number(valor));
  }

  registerOnChange(fn: (valor: number | null) => void): void {
    this.notificarCambio = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.notificarTocado = fn;
  }

  setDisabledState(estaDeshabilitado: boolean): void {
    this.deshabilitado = estaDeshabilitado;
  }

  alEscribir(evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    this.textoBusqueda.set(valor);
    this.mostrarSugerencias.set(true);
    this.valorInterno.set(null);
    this.notificarCambio(null);
  }

  seleccionar(opcion: OpcionBuscador): void {
    this.valorInterno.set(opcion.id);
    this.textoBusqueda.set('');
    this.mostrarSugerencias.set(false);
    this.notificarCambio(opcion.id);
    this.notificarTocado();
  }

  limpiar(): void {
    this.valorInterno.set(null);
    this.textoBusqueda.set('');
    this.mostrarSugerencias.set(false);
    this.notificarCambio(null);
    this.notificarTocado();
  }

  ocultarSugerenciasConRetraso(): void {
    setTimeout(() => this.mostrarSugerencias.set(false), 150);
  }

  private coincideBusqueda(opcion: OpcionBuscador, texto: string): boolean {
    const etiqueta = opcion.etiqueta.toLowerCase();
    const detalle = opcion.detalle?.toLowerCase() ?? '';
    return etiqueta.includes(texto) || detalle.includes(texto);
  }
}
