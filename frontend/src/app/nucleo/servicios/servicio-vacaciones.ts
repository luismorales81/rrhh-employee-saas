import { Injectable } from '@angular/core';

import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';



@Injectable({ providedIn: 'root' })

export class ServicioVacaciones {

  private readonly urlBase = `${environment.urlApi}/vacaciones`;



  constructor(private readonly http: HttpClient) {}



  listarSolicitudes(estado?: string): Observable<SolicitudVacacionesResumen[]> {

    let params = new HttpParams();

    if (estado) {

      params = params.set('estado', estado);

    }

    return this.http.get<SolicitudVacacionesResumen[]>(`${this.urlBase}/solicitudes`, { params });

  }



  listarSaldos(): Observable<SaldoVacacionesResumen[]> {

    return this.http.get<SaldoVacacionesResumen[]>(`${this.urlBase}/saldos`);

  }



  listarPoliticas(): Observable<PoliticaVacacionesResumen[]> {

    return this.http.get<PoliticaVacacionesResumen[]>(`${this.urlBase}/politicas`);

  }



  crearPolitica(solicitud: SolicitudCrearPoliticaVacaciones): Observable<PoliticaVacacionesResumen> {

    return this.http.post<PoliticaVacacionesResumen>(`${this.urlBase}/politicas`, solicitud);

  }



  activarPolitica(id: number): Observable<PoliticaVacacionesResumen> {

    return this.http.post<PoliticaVacacionesResumen>(`${this.urlBase}/politicas/${id}/activar`, {});

  }



  simularFecha(solicitud: SolicitudSimularFechaVacaciones): Observable<RespuestaSimularFechaVacaciones> {

    return this.http.post<RespuestaSimularFechaVacaciones>(`${this.urlBase}/simular-fecha`, solicitud);

  }



  solicitar(solicitud: SolicitudVacacionesDto): Observable<SolicitudVacacionesResumen> {

    return this.http.post<SolicitudVacacionesResumen>(`${this.urlBase}/solicitudes`, solicitud);

  }



  listarPropias(): Observable<SolicitudVacacionesResumen[]> {

    return this.http.get<SolicitudVacacionesResumen[]>(`${this.urlBase}/solicitudes/propias`);

  }



  obtenerDetalle(solicitudVacacionesId: number): Observable<DetalleSolicitudVacaciones> {

    return this.http.get<DetalleSolicitudVacaciones>(`${this.urlBase}/solicitudes/${solicitudVacacionesId}`);

  }



  aprobar(solicitudVacacionesId: number, body?: SolicitudAprobarVacaciones): Observable<SolicitudVacacionesResumen> {

    return this.http.patch<SolicitudVacacionesResumen>(

      `${this.urlBase}/solicitudes/${solicitudVacacionesId}/aprobar`,

      body ?? {}

    );

  }



  contraproponer(

    solicitudVacacionesId: number,

    body: SolicitudContrapropuestaVacaciones

  ): Observable<SolicitudVacacionesResumen> {

    return this.http.patch<SolicitudVacacionesResumen>(

      `${this.urlBase}/solicitudes/${solicitudVacacionesId}/contraproponer`,

      {
        motivo: body.motivo,
        fechaInicioContrapropuesta: body.fechaInicioContrapropuesta,
        fechaFinContrapropuesta: body.fechaFinContrapropuesta
      }

    );

  }



  rechazar(solicitudVacacionesId: number, body: SolicitudRechazarVacaciones): Observable<SolicitudVacacionesResumen> {

    return this.http.patch<SolicitudVacacionesResumen>(

      `${this.urlBase}/solicitudes/${solicitudVacacionesId}/rechazar`,

      body

    );

  }



  aceptarContrapropuesta(solicitudVacacionesId: number): Observable<SolicitudVacacionesResumen> {

    return this.http.post<SolicitudVacacionesResumen>(

      `${this.urlBase}/solicitudes/${solicitudVacacionesId}/aceptar-contrapropuesta`,

      {}

    );

  }



  cancelar(solicitudVacacionesId: number): Observable<SolicitudVacacionesResumen> {

    return this.http.post<SolicitudVacacionesResumen>(

      `${this.urlBase}/solicitudes/${solicitudVacacionesId}/cancelar`,

      {}

    );

  }



  saldoPropio(anio?: number): Observable<SaldoVacacionesResumen> {

    let params = new HttpParams();

    if (anio != null) {

      params = params.set('anio', anio);

    }

    return this.http.get<SaldoVacacionesResumen>(`${this.urlBase}/saldo/propio`, { params });

  }



  historialSaldoPropio(): Observable<SaldoVacacionesResumen[]> {

    return this.http.get<SaldoVacacionesResumen[]>(`${this.urlBase}/saldo/propio/historial`);

  }



  obtenerConfiguracion(): Observable<ConfiguracionVacacionesResumen> {

    return this.http.get<ConfiguracionVacacionesResumen>(`${this.urlBase}/configuracion`);

  }



  actualizarConfiguracion(

    solicitud: SolicitudActualizarConfiguracionVacaciones

  ): Observable<ConfiguracionVacacionesResumen> {

    return this.http.put<ConfiguracionVacacionesResumen>(`${this.urlBase}/configuracion`, solicitud);

  }



  listarPatronesDescanso(empleadoId: number): Observable<PatronDescansoResumen[]> {

    return this.http.get<PatronDescansoResumen[]>(`${this.urlBase}/empleados/${empleadoId}/patron-descanso`);

  }



  crearPatronDescanso(

    empleadoId: number,

    solicitud: SolicitudCrearPatronDescanso

  ): Observable<PatronDescansoResumen> {

    return this.http.post<PatronDescansoResumen>(

      `${this.urlBase}/empleados/${empleadoId}/patron-descanso`,

      solicitud

    );

  }



  obtenerCalendario(desde: string, hasta: string): Observable<EventoCalendarioVacaciones[]> {

    return this.http.get<EventoCalendarioVacaciones[]>(`${this.urlBase}/calendario`, {

      params: { desde, hasta }

    });

  }

}



export type DiaSemana =

  | 'LUNES'

  | 'MARTES'

  | 'MIERCOLES'

  | 'JUEVES'

  | 'VIERNES'

  | 'SABADO'

  | 'DOMINGO';



export const DIAS_SEMANA: { valor: DiaSemana; etiqueta: string }[] = [

  { valor: 'LUNES', etiqueta: 'Lunes' },

  { valor: 'MARTES', etiqueta: 'Martes' },

  { valor: 'MIERCOLES', etiqueta: 'Miércoles' },

  { valor: 'JUEVES', etiqueta: 'Jueves' },

  { valor: 'VIERNES', etiqueta: 'Viernes' },

  { valor: 'SABADO', etiqueta: 'Sábado' },

  { valor: 'DOMINGO', etiqueta: 'Domingo' }

];



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

  empleadoNombre: string;

  fechaInicio: string;

  fechaFin: string;

  diasSolicitados: number;

  estado: string;

  motivo: string | null;

  fechaInicioContrapropuesta?: string | null;

  fechaFinContrapropuesta?: string | null;

  motivoContrapropuesta?: string | null;

  fechaInicioAprobada?: string | null;

  fechaFinAprobada?: string | null;

  advertencias?: string[];

}



export interface DetalleSolicitudVacaciones {

  solicitud: SolicitudVacacionesResumen;

  fechaInicioValida: boolean;

  fechaInicioSugerida: string | null;

  motivoFechaInicio: string | null;

  erroresValidacion: string[];

  advertenciasValidacion: string[];

}



export interface SaldoVacacionesResumen {

  empleadoId: number;

  empleadoNombre: string;

  anio: number;

  antiguedadAnios: number;

  diasTotales: number;

  diasDisponibles: number;

  diasPendientes: number;

  diasTomados: number;

  politicaId: number;

  politicaNombre: string;

}



export interface PoliticaVacacionesResumen {

  id: number;

  nombre: string;

  descripcion: string | null;

  activa: boolean;

  rangos: RangoPoliticaResumen[];

}



export interface RangoPoliticaResumen {

  id: number;

  aniosMinimos: number;

  aniosMaximos: number | null;

  diasOtorgados: number;

}



export interface ConfiguracionVacacionesResumen {

  diasMinimosFraccion: number;

  anticipacionMinimaDias: number;

  permiteSolicitudSinSaldo: boolean;

  diasMinimosFraccionEfectivos: number;

}



export interface PatronDescansoResumen {

  id: number;

  empleadoId: number;

  diasDescanso: DiaSemana[];

  vigenciaDesde: string;

  vigenciaHasta: string | null;

}



export interface RespuestaSimularFechaVacaciones {

  esValida: boolean;

  fechaInicioDeseada: string;

  fechaFinCalculada: string;

  fechaSugerida: string | null;

  motivo: string | null;

}



export interface SolicitudCrearPoliticaVacaciones {

  nombre: string;

  descripcion?: string;

  rangos: { aniosMinimos: number; aniosMaximos?: number | null; diasOtorgados: number }[];

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



export interface SolicitudAprobarVacaciones {

  fechaInicioAprobada?: string | null;

  fechaFinAprobada?: string | null;

  comentarios?: string | null;

}



export interface SolicitudContrapropuestaVacaciones {

  fechaInicioContrapropuesta: string;

  fechaFinContrapropuesta: string;

  motivo: string;

}



export interface SolicitudRechazarVacaciones {

  motivoRechazo: string;

}



export interface SolicitudActualizarConfiguracionVacaciones {

  diasMinimosFraccion: number;

  anticipacionMinimaDias: number;

  permiteSolicitudSinSaldo: boolean;

}



export interface SolicitudCrearPatronDescanso {

  diasDescanso: DiaSemana[];

  vigenciaDesde: string;

  vigenciaHasta?: string | null;

}



export interface ErrorValidacionVacaciones {

  errores?: string[];

  advertencias?: string[];

  mensaje?: string;

}


