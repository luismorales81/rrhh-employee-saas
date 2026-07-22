import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioFichajes {
  private readonly urlBase = `${environment.urlApi}/fichajes`;

  constructor(private readonly http: HttpClient) {}

  obtenerPolitica(): Observable<PoliticaFichajeResumen> {
    return this.http.get<PoliticaFichajeResumen>(`${this.urlBase}/politica`);
  }

  registrarConsentimiento(idDispositivo?: string): Observable<void> {
    return this.http.post<void>(`${this.urlBase}/consentimiento`, { idDispositivo: idDispositivo ?? null });
  }

  obtenerEstadoDia(): Observable<EstadoFichajeDia> {
    return this.http.get<EstadoFichajeDia>(`${this.urlBase}/estado-dia`);
  }

  marcar(solicitud: SolicitudMarcarFichaje): Observable<MarcaFichajeResumen> {
    return this.http.post<MarcaFichajeResumen>(`${this.urlBase}/marcar`, solicitud);
  }

  listarMisMarcas(): Observable<MarcaFichajeResumen[]> {
    return this.http.get<MarcaFichajeResumen[]>(`${this.urlBase}/mis-marcas`);
  }
}

export type TipoMarca = 'ENTRADA' | 'SALIDA';
export type EstadoMarca = 'VALIDADA' | 'PENDIENTE_REVISION' | 'RECHAZADA';

export interface PoliticaFichajeResumen {
  textoTransparencia: string;
  version: number;
  permitirFichajeSinGps: boolean;
  precisionMaximaMetros: number;
}

export interface EstadoFichajeDia {
  siguienteTipo: TipoMarca;
  fichajeHabilitado: boolean;
  requiereConsentimiento: boolean;
  ultimaMarcaHora: string | null;
  ultimaMarcaTipo: TipoMarca | null;
}

export interface SolicitudMarcarFichaje {
  tipo: TipoMarca;
  latitud?: number;
  longitud?: number;
  precisionMetros?: number;
  fechaHoraDispositivo?: string;
  idDispositivo?: string;
  modeloDispositivo?: string;
  sistemaOperativo?: string;
  versionApp?: string;
}

export interface MarcaFichajeResumen {
  id: number;
  empleadoId: number;
  empleadoNombre: string;
  tipo: TipoMarca;
  fechaHoraServidor: string;
  estado: EstadoMarca;
  zonaFichajeNombre: string | null;
  motivoEstado: string | null;
}
