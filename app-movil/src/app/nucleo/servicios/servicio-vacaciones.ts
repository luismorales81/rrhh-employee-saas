import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioVacaciones {
  private readonly urlBase = `${environment.urlApi}/vacaciones`;

  constructor(private readonly http: HttpClient) {}

  solicitar(solicitud: SolicitudVacacionesDto): Observable<SolicitudVacacionesResumen> {
    return this.http.post<SolicitudVacacionesResumen>(`${this.urlBase}/solicitudes`, solicitud);
  }

  simularFecha(solicitud: SolicitudSimularFechaVacaciones): Observable<RespuestaSimularFechaVacaciones> {
    return this.http.post<RespuestaSimularFechaVacaciones>(`${this.urlBase}/simular-fecha`, solicitud);
  }

  listarPropias(): Observable<SolicitudVacacionesResumen[]> {
    return this.http.get<SolicitudVacacionesResumen[]>(`${this.urlBase}/solicitudes/propias`);
  }

  saldoPropio(anio?: number): Observable<SaldoVacacionesResumen> {
    const opciones = anio != null ? { params: { anio: String(anio) } } : {};
    return this.http.get<SaldoVacacionesResumen>(`${this.urlBase}/saldo/propio`, opciones);
  }

  obtenerCalendario(desde: string, hasta: string): Observable<EventoCalendarioVacaciones[]> {
    return this.http.get<EventoCalendarioVacaciones[]>(`${this.urlBase}/calendario`, {
      params: { desde, hasta }
    });
  }
}

export interface EventoCalendarioVacaciones {
  solicitudId: number;
  empleadoId: number;
  nombreEmpleado: string;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
  diasSolicitados: number;
}

export interface SolicitudVacacionesResumen {
  solicitudId: number;
  solicitudVacacionesId: number;
  empleadoId: number;
  fechaInicio: string;
  fechaFin: string;
  diasSolicitados: number;
  estado: string;
  motivo: string | null;
  advertencias?: string[];
}

export interface SaldoVacacionesResumen {
  empleadoId: number;
  anio?: number;
  diasDisponibles: number;
  diasPendientes: number;
  diasUsados?: number;
  diasTomados?: number;
  politicaId: number;
  politicaNombre: string;
}

export interface SolicitudVacacionesDto {
  fechaInicio: string;
  fechaFin: string;
  motivo?: string;
}

export interface SolicitudSimularFechaVacaciones {
  fechaInicioDeseada: string;
  diasDeseados: number;
}

export interface RespuestaSimularFechaVacaciones {
  esValida: boolean;
  fechaInicioDeseada: string;
  fechaFinCalculada: string;
  fechaSugerida: string | null;
  motivo: string | null;
}

export interface ErrorValidacionVacaciones {
  errores?: string[];
  advertencias?: string[];
  mensaje?: string;
}
