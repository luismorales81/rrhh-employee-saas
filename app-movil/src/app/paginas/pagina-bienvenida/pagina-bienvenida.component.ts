import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonButton, IonContent } from '@ionic/angular/standalone';
import { ServicioPreferencias } from '../../nucleo/servicios/servicio-preferencias';

interface SlideBienvenida {
  titulo: string;
  texto: string;
  imagen: string;
}

@Component({
  selector: 'app-pagina-bienvenida',
  standalone: true,
  imports: [IonContent, IonButton],
  template: `
    <ion-content class="contenido-bienvenida">
      <div class="slide">
        <img [src]="slideActual().imagen" alt="" width="96" height="96" />
        <h1>{{ slideActual().titulo }}</h1>
        <p>{{ slideActual().texto }}</p>

        <div class="puntos" aria-hidden="true">
          @for (s of slides; track s.titulo; let i = $index) {
            <span [class.activo]="i === indice()"></span>
          }
        </div>

        <div class="acciones">
          @if (indice() < slides.length - 1) {
            <ion-button expand="block" type="button" (click)="siguiente()">Continuar</ion-button>
            <ion-button expand="block" fill="clear" type="button" (click)="finalizar()">Omitir</ion-button>
          } @else {
            <ion-button expand="block" type="button" (click)="finalizar()">Empezar</ion-button>
          }
        </div>
      </div>
    </ion-content>
  `,
  styles: `
    .contenido-bienvenida {
      --background: linear-gradient(160deg, var(--color-primario-suave), var(--color-fondo) 55%);
    }

    .slide {
      min-height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 2rem 1.5rem 2.5rem;
      max-width: 420px;
      margin: 0 auto;
      text-align: center;
    }

    img {
      margin: 0 auto 1.5rem;
    }

    h1 {
      margin: 0 0 0.75rem;
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--color-texto);
    }

    p {
      margin: 0;
      color: var(--color-texto-secundario);
      font-size: 1rem;
      line-height: 1.5;
    }

    .puntos {
      display: flex;
      justify-content: center;
      gap: 0.4rem;
      margin: 1.75rem 0;
    }

    .puntos span {
      width: 8px;
      height: 8px;
      border-radius: 999px;
      background: var(--color-borde-fuerte);
    }

    .puntos span.activo {
      width: 22px;
      background: var(--color-primario);
    }

    .acciones ion-button {
      min-height: 52px;
      font-weight: 700;
    }
  `
})
export class PaginaBienvenidaComponent {
  private readonly preferencias = inject(ServicioPreferencias);
  private readonly router = inject(Router);

  readonly slides: SlideBienvenida[] = [
    {
      titulo: 'Tu trabajo, más simple',
      texto: 'Consultá recibos, pedí vacaciones y resolvé trámites sin vueltas.',
      imagen: 'assets/marca/ligo-icono.svg'
    },
    {
      titulo: 'Todo lo importante, a mano',
      texto: 'Firmá documentos, avisá una falta y fichá tu jornada en segundos.',
      imagen: 'assets/marca/ligo-icono.svg'
    },
    {
      titulo: 'Hecha para vos',
      texto: 'Ligo es tu espacio personal. Tranquilo, claro y listo cuando lo necesités.',
      imagen: 'assets/marca/ligo-icono.svg'
    }
  ];

  readonly indice = signal(0);

  slideActual(): SlideBienvenida {
    return this.slides[this.indice()];
  }

  siguiente(): void {
    if (this.indice() < this.slides.length - 1) {
      this.indice.update((v) => v + 1);
    }
  }

  finalizar(): void {
    this.preferencias.marcarOnboardingProductoVisto();
    void this.router.navigate(['/iniciar-sesion'], { replaceUrl: true });
  }
}
