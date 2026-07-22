import { Component, inject, input } from '@angular/core';
import { ActionSheetController, IonButton, IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { ellipsisHorizontal } from 'ionicons/icons';
import { liberarFocoActivo } from '../../utilidades/liberar-foco-activo';
import { ChipEstadoComponent } from '../chip-estado/chip-estado.component';
import { TonoEstado } from '../../../nucleo/utilidades/etiquetas-estado';

addIcons({ ellipsisHorizontal });

export interface AccionTarjetaFila {
  id: string;
  etiqueta: string;
  color?: string;
  relleno?: 'solid' | 'outline' | 'clear';
  /** Si es true, se muestra como botón principal (solo la primera cuenta). */
  primaria?: boolean;
  /** Si es true, fuerza la acción al menú ⋯. */
  enMenu?: boolean;
  ejecutar: () => void;
}

@Component({
  selector: 'app-tarjeta-fila',
  standalone: true,
  imports: [IonButton, IonIcon, ChipEstadoComponent],
  template: `
    <article class="tarjeta-fila" [class.tarjeta-alerta]="alerta()">
      <div class="principal">
        <h3>{{ titulo() }}</h3>
        @if (subtitulo()) {
          <p class="subtitulo">{{ subtitulo() }}</p>
        }
        @if (meta()) {
          <p class="meta">{{ meta() }}</p>
        }
      </div>
      <div class="lateral">
        @if (etiqueta()) {
          <app-chip-estado [texto]="etiqueta()!" [tono]="tonoEtiqueta()" />
        }
        @if (acciones().length > 0) {
          <div class="acciones">
            @if (accionPrincipal(); as principal) {
              <ion-button
                size="default"
                type="button"
                [color]="principal.color ?? 'primary'"
                [fill]="principal.relleno ?? 'solid'"
                (click)="ejecutarAccion(principal, $event)"
              >
                {{ principal.etiqueta }}
              </ion-button>
            }
            @if (accionesMenu().length > 0) {
              <ion-button
                class="boton-menu"
                fill="outline"
                type="button"
                aria-label="Más acciones"
                (click)="abrirMenu($event)"
              >
                <ion-icon slot="icon-only" name="ellipsis-horizontal"></ion-icon>
              </ion-button>
            }
          </div>
        }
      </div>
    </article>
  `,
  styles: `
    .tarjeta-fila {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: var(--espacio-3);
      background: var(--color-superficie);
      border-radius: var(--radio-lg);
      padding: var(--espacio-4) var(--espacio-5);
      border: 1px solid var(--color-borde-suave);
      box-shadow: var(--sombra-sm);
    }

    .tarjeta-alerta {
      border-color: var(--color-alerta-borde);
      background: linear-gradient(
        to bottom,
        var(--color-alerta-fondo) 0%,
        var(--color-superficie) 55%
      );
    }

    .principal {
      flex: 1;
      min-width: 0;
    }

    h3 {
      margin: 0 0 0.2rem;
      font-size: 1rem;
      font-weight: 600;
      line-height: 1.3;
      color: var(--color-texto);
    }

    .subtitulo {
      margin: 0 0 0.15rem;
      font-size: 0.88rem;
      color: var(--color-texto);
      line-height: 1.4;
    }

    .meta {
      margin: 0;
      font-size: 0.8rem;
      color: var(--color-texto-secundario);
    }

    .lateral {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 0.5rem;
      flex-shrink: 0;
      position: relative;
      z-index: 1;
    }

    .acciones {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      gap: 0.35rem;
      align-items: center;
      justify-content: flex-end;
    }

    .acciones ion-button {
      margin: 0;
      min-height: 52px;
      font-weight: 600;
      text-transform: none;
    }

    .boton-menu {
      --padding-start: 0.55rem;
      --padding-end: 0.55rem;
      min-width: 44px;
    }
  `
})
export class TarjetaFilaComponent {
  private readonly actionSheet = inject(ActionSheetController);

  readonly titulo = input.required<string>();
  readonly subtitulo = input<string | null>(null);
  readonly meta = input<string | null>(null);
  readonly etiqueta = input<string | null>(null);
  readonly tonoEtiqueta = input<TonoEstado>('neutro');
  readonly alerta = input(false);
  readonly acciones = input<AccionTarjetaFila[]>([]);

  accionPrincipal(): AccionTarjetaFila | null {
    const lista = this.acciones();
    if (lista.length === 0) {
      return null;
    }
    const marcada = lista.find((a) => a.primaria && !a.enMenu);
    if (marcada) {
      return marcada;
    }
    const primeraVisible = lista.find((a) => !a.enMenu);
    return primeraVisible ?? null;
  }

  accionesMenu(): AccionTarjetaFila[] {
    const principal = this.accionPrincipal();
    return this.acciones().filter((a) => a !== principal);
  }

  ejecutarAccion(accion: AccionTarjetaFila, evento: Event): void {
    evento.preventDefault();
    evento.stopPropagation();
    liberarFocoActivo();
    accion.ejecutar();
  }

  async abrirMenu(evento: Event): Promise<void> {
    evento.preventDefault();
    evento.stopPropagation();
    liberarFocoActivo();

    const opciones = this.accionesMenu();
    if (opciones.length === 0) {
      return;
    }

    const sheet = await this.actionSheet.create({
      header: 'Acciones',
      buttons: [
        ...opciones.map((accion) => ({
          text: accion.etiqueta,
          role: accion.color === 'danger' ? ('destructive' as const) : undefined,
          handler: () => {
            accion.ejecutar();
          }
        })),
        { text: 'Cancelar', role: 'cancel' as const }
      ]
    });
    await sheet.present();
  }
}
