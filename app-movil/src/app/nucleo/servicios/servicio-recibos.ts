import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';



@Injectable({ providedIn: 'root' })

export class ServicioRecibos {

  private readonly urlBase = `${environment.urlApi}/recibos`;



  constructor(private readonly http: HttpClient) {}



  listarPropios(): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(`${this.urlBase}/mis-recibos`);

  }



  obtener(id: number): Observable<ReciboResumen> {

    return this.http.get<ReciboResumen>(`${this.urlBase}/${id}`);

  }



  listarVersiones(id: number): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(`${this.urlBase}/${id}/versiones`);

  }



  visualizar(id: number): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(`${this.urlBase}/${id}/visualizar`, {});

  }



  confirmarRecepcion(id: number, solicitud: SolicitudConfirmarRecepcionRecibo): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(`${this.urlBase}/${id}/confirmar-recepcion`, solicitud);

  }



  firmar(id: number): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(`${this.urlBase}/${id}/firmar`, {});

  }



  descargarPdf(id: number): Observable<Blob> {

    return this.http.get(`${this.urlBase}/${id}/pdf`, { responseType: 'blob' });

  }



  descargarComprobante(id: number): Observable<Blob> {

    return this.http.get(`${this.urlBase}/${id}/comprobante`, { responseType: 'blob' });

  }



  contarPendientesFirma(): Observable<number> {

    return new Observable((observador) => {

      this.listarPropios().subscribe({

        next: (lista) => {

          observador.next(lista.filter((r) => r.estado === 'PENDIENTE' || r.estado === 'VISTO').length);

          observador.complete();

        },

        error: (err) => observador.error(err)

      });

    });

  }

}



export type EstadoRecibo = 'PENDIENTE' | 'VISTO' | 'FIRMADO' | 'DISCONFORMIDAD';

export type TipoConfirmacionRecibo = 'CONFIRMADO' | 'DISCONFORMIDAD';



export interface ReciboResumen {
  id: number;
  empleadoId: number;
  nombreEmpleado: string;
  periodo: string;
  titulo: string;
  urlDescargaPdf: string | null;
  estado: EstadoRecibo;
  fechaPublicacion: string;
  vistoPorEmpleadoEn: string | null;
  fechaFirma: string | null;
  montoNeto: number | null;
  montoBruto: number | null;
  hashPdf: string | null;
  requiereRepublicacion: boolean;
  version: number;
  reciboAnteriorId: number | null;
  vigente: boolean;
  notificadoEn: string | null;
  canalNotificacion: string | null;
  comentarioDisconformidad: string | null;
}



export interface SolicitudConfirmarRecepcionRecibo {

  tipo: TipoConfirmacionRecibo;

  comentario?: string | null;

  contrasena?: string | null;

}



export const TEXTO_LEGAL_FIRMA_RECIBO = `Al confirmar la recepción de este recibo de sueldo, usted declara bajo firma electrónica simple (Ley 25.506) haber tomado conocimiento de su contenido. La constancia digital de entrega se rige por la Ley 27.802 y el Art. 140 de la LCT. Puede manifestar disconformidad indicando el motivo; el documento y la cadena de eventos permanecen inalterables.`;

export function tieneIntegridadVerificable(recibo: ReciboResumen): boolean {
  // El hash SHA-256 no se expone al empleado; requiereRepublicacion y PDF disponible bastan.
  return !recibo.requiereRepublicacion && !!recibo.urlDescargaPdf;
}


