import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';
import { liberarFocoActivo } from './compartido/utilidades/liberar-foco-activo';
import { ServicioPreferencias } from './nucleo/servicios/servicio-preferencias';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [IonApp, IonRouterOutlet],
  template: `
    <ion-app>
      @if (mostrarSplash()) {
        <div class="splash" aria-hidden="true">
          <img src="assets/marca/ligo-icono.svg" alt="" width="72" height="72" />
          <strong>Ligo</strong>
        </div>
      }
      <ion-router-outlet></ion-router-outlet>
    </ion-app>
  `,
  styles: `
    .splash {
      position: fixed;
      inset: 0;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      background: linear-gradient(160deg, #ecfaf6, #f5f8f7 60%);
      animation: fade-splash 1.2s ease forwards;
    }

    .splash strong {
      font-size: 1.5rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: #14201c;
    }

    @keyframes fade-splash {
      0% { opacity: 1; }
      70% { opacity: 1; }
      100% { opacity: 0; visibility: hidden; }
    }
  `
})
export class AppComponent implements OnInit {
  private readonly preferencias = inject(ServicioPreferencias);
  readonly mostrarSplash = signal(false);

  constructor() {
    const router = inject(Router);
    const destruirRef = inject(DestroyRef);

    router.events
      .pipe(
        filter((evento): evento is NavigationStart => evento instanceof NavigationStart),
        takeUntilDestroyed(destruirRef)
      )
      .subscribe(() => liberarFocoActivo());
  }

  ngOnInit(): void {
    this.preferencias.aplicarTemaActual();
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (this.preferencias.tema() === 'sistema') {
        this.preferencias.aplicarTemaActual();
      }
    });

    if (this.preferencias.debeMostrarSplashSesion()) {
      this.mostrarSplash.set(true);
      setTimeout(() => this.mostrarSplash.set(false), 1200);
    }
  }
}
