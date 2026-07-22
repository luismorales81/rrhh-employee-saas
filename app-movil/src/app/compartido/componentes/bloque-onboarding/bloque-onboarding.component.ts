import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonButton, IonProgressBar } from '@ionic/angular/standalone';

export interface TareaOnboardingVista {
  id: number;
  nombre: string;
  completada: boolean;
}

@Component({
  selector: 'app-bloque-onboarding',
  standalone: true,
  imports: [RouterLink, IonButton, IonProgressBar],
  template: `
    <section class="bloque-onboarding">
      <div class="encabezado">
        <h2>Primeros pasos en curso</h2>
        <p>{{ completadas() }} de {{ total() }} tareas completadas</p>
        <ion-progress-bar [value]="progreso()"></ion-progress-bar>
      </div>
      <ul>
        @for (tarea of tareas(); track tarea.id) {
          <li>
            <div class="tarea-info">
              <span>{{ tarea.nombre }}</span>
              @if (!tarea.completada) {
                <a [routerLink]="rutaTarea(tarea.nombre)">Ir a la tarea</a>
              }
            </div>
            @if (!tarea.completada) {
              <ion-button size="small" [disabled]="procesando()" (click)="completar.emit(tarea.id)">
                Listo
              </ion-button>
            } @else {
              <span class="completada">Completada</span>
            }
          </li>
        }
      </ul>
      <div class="enlaces">
        <a routerLink="/tabs/documentos">Documentos</a>
        <a routerLink="/tabs/perfil">Mis datos</a>
      </div>
    </section>
  `,
  styles: `
    .bloque-onboarding {
      background: var(--color-primario-suave);
      border: 1px solid var(--color-primario-borde);
      border-radius: var(--radio-lg);
      padding: var(--espacio-4) var(--espacio-5);
      margin-bottom: var(--espacio-4);
    }

    .encabezado h2 {
      margin: 0 0 0.25rem;
      font-size: 1.05rem;
    }

    .encabezado p {
      margin: 0 0 0.5rem;
      color: var(--color-texto-secundario);
      font-size: 0.9rem;
    }

    ion-progress-bar {
      margin-bottom: var(--espacio-3);
      --progress-background: var(--color-primario);
    }

    ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    li {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.5rem;
      padding: 0.55rem 0;
      border-bottom: 1px solid var(--color-borde-suave);
      font-size: 0.92rem;
    }

    li:last-child { border-bottom: none; }

    .tarea-info {
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
      min-width: 0;
    }

    .tarea-info a {
      margin: 0;
      font-size: 0.8rem;
    }

    .completada {
      color: var(--color-exito-texto);
      font-weight: 600;
      font-size: 0.85rem;
    }

    .enlaces {
      display: flex;
      gap: 1rem;
      margin-top: var(--espacio-3);
    }

    a {
      color: var(--color-primario);
      font-weight: 600;
      text-decoration: none;
      font-size: 0.9rem;
    }

    ion-button {
      min-height: 40px;
      text-transform: none;
    }
  `
})
export class BloqueOnboardingComponent {
  readonly tareas = input.required<TareaOnboardingVista[]>();
  readonly procesando = input(false);
  readonly completar = output<number>();

  total(): number {
    return this.tareas().length;
  }

  completadas(): number {
    return this.tareas().filter((t) => t.completada).length;
  }

  progreso(): number {
    const total = this.total();
    return total === 0 ? 0 : this.completadas() / total;
  }

  rutaTarea(nombre: string): string {
    const n = nombre.toLowerCase();
    if (n.includes('documento') || n.includes('firmar') || n.includes('contrato')) {
      return '/tabs/documentos';
    }
    if (n.includes('recibo')) {
      return '/tabs/recibos';
    }
    if (n.includes('dato') || n.includes('perfil') || n.includes('legajo') || n.includes('dni')) {
      return '/tabs/perfil';
    }
    if (n.includes('foto') || n.includes('equipo') || n.includes('activo')) {
      return '/tabs/mis-activos';
    }
    return '/tabs/documentos';
  }
}
