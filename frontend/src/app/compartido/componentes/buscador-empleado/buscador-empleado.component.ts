import {
  Component,
  Input,
  forwardRef,
  signal,
  computed
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { EmpleadoResumen } from '../../../nucleo/servicios/servicio-empleados';

@Component({
  selector: 'app-buscador-empleado',
  standalone: true,
  template: `
    <div class="buscador-empleado">
      @if (empleadoSeleccionado(); as empleado) {
        <div class="empleado-seleccionado">
          <span class="nombre">{{ etiquetaEmpleado(empleado) }}</span>
          <button type="button" class="btn-limpiar" [disabled]="deshabilitado" (click)="limpiar()">
            Cambiar
          </button>
        </div>
      } @else {
        <input
          type="search"
          class="campo-busqueda-empleado"
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
              @for (empleado of sugerencias(); track empleado.id) {
                <li role="option" (mousedown)="seleccionar(empleado)">
                  <span class="nombre">{{ empleado.nombre }} {{ empleado.apellido }}</span>
                  <span class="detalle">
                    @if (empleado.numeroEmpleado) {
                      Legajo {{ empleado.numeroEmpleado }}
                    }
                    @if (empleado.dni) {
                      @if (empleado.numeroEmpleado) { · }
                      DNI {{ empleado.dni }}
                    }
                  </span>
                </li>
              }
            </ul>
          } @else {
            <p class="sin-resultados">No se encontraron empleados</p>
          }
        } @else if (mostrarSugerencias() && textoBusqueda().trim().length === 0) {
          <p class="ayuda-busqueda">Escribí nombre, legajo o DNI</p>
        }
      }
    </div>
  `,
  styles: `
    .buscador-empleado {
      position: relative;
      width: 100%;
    }

    .campo-busqueda-empleado {
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--color-borde);
      border-radius: var(--radio-md);
      font-size: 0.875rem;
      font-family: inherit;
      background: var(--color-superficie);
    }

    .empleado-seleccionado {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.75rem;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--color-borde);
      border-radius: var(--radio-md);
      background: var(--color-fondo);
    }

    .empleado-seleccionado .nombre {
      font-weight: 500;
    }

    .btn-limpiar {
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

    .lista-sugerencias .nombre {
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
      useExisting: forwardRef(() => BuscadorEmpleadoComponent),
      multi: true
    }
  ]
})
export class BuscadorEmpleadoComponent implements ControlValueAccessor {
  @Input() empleados: EmpleadoResumen[] = [];
  @Input() placeholder = 'Buscar por nombre, legajo o DNI...';
  @Input() soloActivos = true;

  readonly textoBusqueda = signal('');
  readonly mostrarSugerencias = signal(false);
  readonly valorInterno = signal<number | null>(null);
  deshabilitado = false;

  readonly empleadoSeleccionado = computed(() => {
    const id = this.valorInterno();
    if (id == null) {
      return null;
    }
    return this.empleadosDisponibles().find((e) => e.id === id) ?? null;
  });

  readonly empleadosDisponibles = computed(() => {
    if (!this.soloActivos) {
      return this.empleados;
    }
    return this.empleados.filter((e) => e.estado === 'ACTIVO');
  });

  readonly sugerencias = computed(() => {
    const texto = this.textoBusqueda().trim().toLowerCase();
    if (!texto) {
      return [];
    }
    return this.empleadosDisponibles()
      .filter((e) => this.coincideBusqueda(e, texto))
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

  seleccionar(empleado: EmpleadoResumen): void {
    this.valorInterno.set(empleado.id);
    this.textoBusqueda.set('');
    this.mostrarSugerencias.set(false);
    this.notificarCambio(empleado.id);
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

  etiquetaEmpleado(empleado: EmpleadoResumen): string {
    const legajo = empleado.numeroEmpleado ? ` (${empleado.numeroEmpleado})` : '';
    return `${empleado.nombre} ${empleado.apellido}${legajo}`;
  }

  private coincideBusqueda(empleado: EmpleadoResumen, texto: string): boolean {
    const nombreCompleto = `${empleado.nombre} ${empleado.apellido}`.toLowerCase();
    const legajo = empleado.numeroEmpleado?.toLowerCase() ?? '';
    const dni = empleado.dni?.toLowerCase() ?? '';
    return nombreCompleto.includes(texto) || legajo.includes(texto) || dni.includes(texto);
  }
}
