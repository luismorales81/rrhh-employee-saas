import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { clavesLocal } from '../../../environments/claves-local';

type LibreriaGoogleMaps = 'marker' | 'places' | 'geometry' | 'drawing';

@Injectable({ providedIn: 'root' })
export class ServicioCargaGoogleMaps {
  private promesaCarga: Promise<void> | null = null;
  private libreriasCargadas = new Set<string>();
  private apiLista = false;

  obtenerClave(): string {
    return clavesLocal.claveGoogleMaps || environment.claveGoogleMaps;
  }

  obtenerIdMapa(): string | null {
    const id = clavesLocal.idMapaGoogle || environment.idMapaGoogle;
    return id?.trim() ? id.trim() : null;
  }

  opcionesMapa(centro: google.maps.LatLngLiteral, zoom: number): google.maps.MapOptions {
    const idMapa = this.obtenerIdMapa();
    if (idMapa) {
      return { center: centro, zoom, mapId: idMapa };
    }
    return { center: centro, zoom, mapTypeId: 'roadmap' };
  }

  async cargar(librerias: LibreriaGoogleMaps[] = []): Promise<void> {
    await this.cargarApiBase();
    for (const libreria of librerias) {
      if (!this.libreriasCargadas.has(libreria)) {
        await google.maps.importLibrary(libreria);
        this.libreriasCargadas.add(libreria);
      }
    }
  }

  async colocarMarcadorPunto(
    mapa: google.maps.Map,
    posicion: google.maps.LatLngLiteral,
    opciones?: { titulo?: string; color?: string; radioMetros?: number }
  ): Promise<{ limpiar: () => void }> {
    const color = opciones?.color ?? '#2563eb';
    const idMapa = this.obtenerIdMapa();

    if (idMapa) {
      await this.cargar(['marker']);
      const libreria = await google.maps.importLibrary('marker') as google.maps.MarkerLibrary;
      const pin = new libreria.PinElement({
        background: color,
        borderColor: '#ffffff',
        glyphColor: '#ffffff'
      });
      const marcador = new libreria.AdvancedMarkerElement({
        map: mapa,
        position: posicion,
        title: opciones?.titulo,
        content: pin.element
      });
      return {
        limpiar: () => {
          marcador.map = null;
        }
      };
    }

    const circulo = new google.maps.Circle({
      map: mapa,
      center: posicion,
      radius: opciones?.radioMetros ?? 10,
      fillColor: color,
      fillOpacity: 1,
      strokeColor: '#ffffff',
      strokeWeight: 2,
      clickable: false,
      zIndex: 10
    });
    return {
      limpiar: () => circulo.setMap(null)
    };
  }

  private async cargarApiBase(): Promise<void> {
    if (this.apiLista) {
      return;
    }
    if (this.promesaCarga) {
      await this.promesaCarga;
      return;
    }

    const clave = this.obtenerClave();
    if (!clave) {
      throw new Error('Falta configurar claveGoogleMaps en src/environments/claves-local.ts');
    }

    const nombreCallback = '__rrhhGoogleMapsInit';
    this.promesaCarga = new Promise((resolve, reject) => {
      (window as unknown as Record<string, () => void>)[nombreCallback] = () => {
        delete (window as unknown as Record<string, unknown>)[nombreCallback];
        this.apiLista = true;
        resolve();
      };

      const parametros = new URLSearchParams({
        key: clave,
        v: 'weekly',
        loading: 'async',
        callback: nombreCallback
      });

      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?${parametros.toString()}`;
      script.async = true;
      script.onerror = () => reject(new Error('No se pudo cargar Google Maps'));
      document.head.appendChild(script);
    });

    return this.promesaCarga;
  }
}
