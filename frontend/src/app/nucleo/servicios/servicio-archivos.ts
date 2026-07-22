import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioArchivos {
  private readonly urlBase = `${environment.urlApi}/archivos`;

  constructor(private readonly http: HttpClient) {}

  subir(archivo: File): Observable<ArchivoSubidoResumen> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<ArchivoSubidoResumen>(this.urlBase, formData);
  }

  urlContenido(id: number): string {
    return `${environment.urlApi}/archivos/${id}/contenido`;
  }

  /** Resuelve rutas relativas del API a URL absoluta. */
  resolverUrl(ruta: string): string {
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) {
      return ruta;
    }
    const origen = environment.urlApi.replace(/\/api\/?$/, '');
    return `${origen}${ruta.startsWith('/') ? ruta : `/${ruta}`}`;
  }

  /** Descarga el contenido con el interceptor de autenticación (para el visor integrado). */
  descargarPorUrl(ruta: string): Observable<Blob> {
    return this.http.get(this.resolverUrl(ruta), { responseType: 'blob' });
  }
}

export interface ArchivoSubidoResumen {
  id: number;
  nombreOriginal: string;
  tipoMime: string;
  tamano: number;
  hashSha256: string;
  urlContenido: string;
}
