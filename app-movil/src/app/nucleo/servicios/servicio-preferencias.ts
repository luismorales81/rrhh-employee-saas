import { Injectable, signal } from '@angular/core';

export type PreferenciaTema = 'sistema' | 'claro' | 'oscuro';

const CLAVE_ONBOARDING = 'ligo_onboarding_producto_visto';
const CLAVE_TEMA = 'ligo_tema';
const CLAVE_SPLASH = 'ligo_splash_sesion';

@Injectable({ providedIn: 'root' })
export class ServicioPreferencias {
  readonly tema = signal<PreferenciaTema>(this.leerTema());
  readonly onboardingProductoVisto = signal(localStorage.getItem(CLAVE_ONBOARDING) === '1');

  marcarOnboardingProductoVisto(): void {
    localStorage.setItem(CLAVE_ONBOARDING, '1');
    this.onboardingProductoVisto.set(true);
  }

  tipNavegacionVisto(): boolean {
    return localStorage.getItem('ligo_tip_navegacion_v2') === '1';
  }

  marcarTipNavegacionVisto(): void {
    localStorage.setItem('ligo_tip_navegacion_v2', '1');
  }

  guardarTema(tema: PreferenciaTema): void {
    localStorage.setItem(CLAVE_TEMA, tema);
    this.tema.set(tema);
    this.aplicarTema(tema);
  }

  aplicarTemaActual(): void {
    this.aplicarTema(this.tema());
  }

  debeMostrarSplashSesion(): boolean {
    if (sessionStorage.getItem(CLAVE_SPLASH) === '1') {
      return false;
    }
    sessionStorage.setItem(CLAVE_SPLASH, '1');
    return true;
  }

  private leerTema(): PreferenciaTema {
    const valor = localStorage.getItem(CLAVE_TEMA);
    if (valor === 'claro' || valor === 'oscuro' || valor === 'sistema') {
      return valor;
    }
    return 'sistema';
  }

  private aplicarTema(tema: PreferenciaTema): void {
    const raiz = document.documentElement;
    let oscuro = false;
    if (tema === 'oscuro') {
      oscuro = true;
    } else if (tema === 'sistema') {
      oscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    if (oscuro) {
      raiz.setAttribute('data-tema', 'oscuro');
    } else {
      raiz.removeAttribute('data-tema');
    }
    const meta = document.querySelector('meta[name="color-scheme"]');
    if (meta) {
      meta.setAttribute('content', oscuro ? 'dark' : 'light');
    }
  }
}
