import { Injectable } from '@angular/core';
import { Geolocation } from '@capacitor/geolocation';

export interface LecturaUbicacion {
  latitud: number;
  longitud: number;
  precisionMetros: number;
}

@Injectable({ providedIn: 'root' })
export class ServicioGeolocalizacion {

  async obtenerUbicacionActual(): Promise<LecturaUbicacion> {
    try {
      const permiso = await Geolocation.requestPermissions();
      if (permiso.location === 'denied') {
        throw new Error(
          'Necesitamos tu ubicación solo al fichar. Activá el permiso en Ajustes del teléfono y volvé a intentar.'
        );
      }
      const posicion = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000
      });
      return {
        latitud: posicion.coords.latitude,
        longitud: posicion.coords.longitude,
        precisionMetros: posicion.coords.accuracy
      };
    } catch (error) {
      if (error instanceof Error && error.message.includes('Ajustes')) {
        throw error;
      }
      return this.obtenerUbicacionNavegador();
    }
  }

  private obtenerUbicacionNavegador(): Promise<LecturaUbicacion> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Este dispositivo no puede compartir ubicación. Probá desde el celular.'));
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({
          latitud: pos.coords.latitude,
          longitud: pos.coords.longitude,
          precisionMetros: pos.coords.accuracy
        }),
        (err) => {
          if (err.code === err.PERMISSION_DENIED) {
            reject(new Error(
              'Necesitamos tu ubicación solo al fichar. Activá el permiso en Ajustes del teléfono y volvé a intentar.'
            ));
            return;
          }
          if (err.code === err.TIMEOUT) {
            reject(new Error(
              'No encontramos tu ubicación a tiempo. Salí un momento a un lugar con mejor señal y reintentá.'
            ));
            return;
          }
          reject(new Error(
            'No pudimos ubicar el teléfono. Revisá que el GPS esté activo y reintentá.'
          ));
        },
        { enableHighAccuracy: true, timeout: 15000 }
      );
    });
  }

  obtenerIdDispositivo(): string {
    const clave = 'rrhh_id_dispositivo';
    let id = localStorage.getItem(clave);
    if (!id) {
      id = crypto.randomUUID();
      localStorage.setItem(clave, id);
    }
    return id;
  }

  obtenerModeloDispositivo(): string {
    return navigator.userAgent.slice(0, 120);
  }

  obtenerSistemaOperativo(): string {
    const ua = navigator.userAgent;
    if (/Android/i.test(ua)) {
      return 'Android';
    }
    if (/iPhone|iPad/i.test(ua)) {
      return 'iOS';
    }
    return navigator.platform || 'Web';
  }
}
