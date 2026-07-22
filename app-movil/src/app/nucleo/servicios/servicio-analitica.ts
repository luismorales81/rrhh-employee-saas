import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

/**
 * Stub de analítica. Hoy registra en consola (solo no-prod).
 * Después se puede cablear a un proveedor real sin tocar las pantallas.
 */
@Injectable({ providedIn: 'root' })
export class ServicioAnalitica {
  registrar(evento: string, propiedades?: Record<string, string | number | boolean | null | undefined>): void {
    if (environment.produccion) {
      // Placeholder: integrar proveedor (Amplitude, Firebase, etc.).
      return;
    }
    // eslint-disable-next-line no-console
    console.info('[analitica]', evento, propiedades ?? {});
  }

  fichajeOk(tipo: string): void {
    this.registrar('fichar_ok', { tipo });
  }

  reciboFirmado(id: number): void {
    this.registrar('recibo_firmado', { id });
  }

  documentoFirmado(id: number): void {
    this.registrar('documento_firmado', { id });
  }

  vacacionesEnviadas(): void {
    this.registrar('vacaciones_enviada');
  }

  certificadoSubido(): void {
    this.registrar('certificado_subido');
  }

  loginOk(portal: string): void {
    this.registrar('login_ok', { portal });
  }

  homePendienteTap(destino: string): void {
    this.registrar('home_pendiente_tap', { destino });
  }
}
