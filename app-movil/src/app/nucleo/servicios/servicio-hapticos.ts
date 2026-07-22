import { Injectable } from '@angular/core';

/**
 * Feedback háptico discreto. Usa Vibration API cuando está disponible
 * (Android / algunos navegadores). En iOS nativo conviene Capacitor Haptics.
 */
@Injectable({ providedIn: 'root' })
export class ServicioHapticos {
  exito(): void {
    this.vibrar([12]);
  }

  error(): void {
    this.vibrar([30, 40, 30]);
  }

  seleccion(): void {
    this.vibrar([8]);
  }

  private vibrar(patron: number | number[]): void {
    try {
      if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
        navigator.vibrate(patron);
      }
    } catch {
      // Sin soporte háptico: no-op.
    }
  }
}
