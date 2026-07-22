import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';



@Injectable({ providedIn: 'root' })

export class ServicioRecibos {

  private readonly urlBase = `${environment.urlApi}/recibos`;



  constructor(private readonly http: HttpClient) {}



  listar(): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(this.urlBase);

  }



  listarPropios(): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(`${this.urlBase}/mis-recibos`);

  }



  listarPorEmpleado(empleadoId: number): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(`${this.urlBase}/empleado/${empleadoId}`);

  }



  obtenerAlertasSeguimiento(): Observable<RespuestaAlertasRecibos> {

    return this.http.get<RespuestaAlertasRecibos>(`${this.urlBase}/alertas/seguimiento`);

  }



  obtener(id: number): Observable<ReciboResumen> {

    return this.http.get<ReciboResumen>(`${this.urlBase}/${id}`);

  }



  listarVersiones(id: number): Observable<ReciboResumen[]> {

    return this.http.get<ReciboResumen[]>(`${this.urlBase}/${id}/versiones`);

  }



  cargar(solicitud: SolicitudCargarRecibo): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(this.urlBase, solicitud);

  }



  republicar(id: number, solicitud: SolicitudRepublicarRecibo): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(`${this.urlBase}/${id}/republicar`, solicitud);

  }



  reenviarNotificacion(id: number): Observable<ReciboResumen> {

    return this.http.post<ReciboResumen>(`${this.urlBase}/${id}/reenviar-notificacion`, {});

  }



  analizarCargaMasiva(archivoZip: File, periodo: string): Observable<RespuestaAnalisisCargaMasiva> {

    const formData = new FormData();

    formData.append('archivoZip', archivoZip);

    formData.append('periodo', periodo);

    return this.http.post<RespuestaAnalisisCargaMasiva>(`${this.urlBase}/carga-masiva/analizar`, formData);

  }



  ejecutarCargaMasiva(

    archivoZip: File,

    periodo: string,

    tituloBase: string,

    montoNeto?: number | null,

    montoBruto?: number | null

  ): Observable<RespuestaCargaMasivaRecibos> {

    const formData = new FormData();

    formData.append('archivoZip', archivoZip);

    formData.append('periodo', periodo);

    formData.append('tituloBase', tituloBase);

    if (montoNeto != null) {

      formData.append('montoNeto', String(montoNeto));

    }

    if (montoBruto != null) {

      formData.append('montoBruto', String(montoBruto));

    }

    return this.http.post<RespuestaCargaMasivaRecibos>(`${this.urlBase}/carga-masiva`, formData);

  }



  exportarExpediente(id: number): Observable<Blob> {

    return this.http.get(`${this.urlBase}/${id}/expediente`, { responseType: 'blob' });

  }



  descargarComprobante(id: number): Observable<Blob> {

    return this.http.get(`${this.urlBase}/${id}/comprobante`, { responseType: 'blob' });

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



  listarEventosFirma(id: number): Observable<EventoFirmaReciboResumen[]> {

    return this.http.get<EventoFirmaReciboResumen[]>(`${this.urlBase}/${id}/eventos-firma`);

  }



  descargarPdf(id: number): Observable<Blob> {

    return this.http.get(`${this.urlBase}/${id}/pdf`, { responseType: 'blob' });

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



export interface SolicitudCargarRecibo {

  empleadoId: number;

  periodo: string;

  titulo: string;

  archivoId: number;

  montoNeto?: number | null;

  montoBruto?: number | null;

}



export interface SolicitudRepublicarRecibo {

  archivoId: number;

  motivo: string;

}



export interface SolicitudConfirmarRecepcionRecibo {

  tipo: TipoConfirmacionRecibo;

  comentario?: string | null;

  contrasena?: string | null;

}



export interface EventoFirmaReciboResumen {

  id: number;

  reciboId: number;

  tipo: 'VISTO' | 'CONFIRMADO' | 'DISCONFORMIDAD';

  timestampEvento: string;

  direccionIp: string | null;

  userAgent: string | null;

  sistemaOperativo: string | null;

  hashPdfEnFirma: string;

  comentario: string | null;

  empleadoId: number;

  usuarioId: number;

}



export interface ReciboAlertaResumen {

  id: number;

  empleadoId: number;

  nombreEmpleado: string;

  periodo: string;

  estado: EstadoRecibo;

  fechaPublicacion: string;

  comentarioDisconformidad: string | null;

  notificadoEn: string | null;

}



export interface RespuestaAlertasRecibos {

  totalPendientesFirma: number;

  totalDisconformidades: number;

  totalAntiguosSinFirmar: number;

  diasUmbralAntiguedad: number;

  pendientesAntiguos: ReciboAlertaResumen[];

  disconformidadesRecientes: ReciboAlertaResumen[];

}



export interface FilaAnalisisCargaMasiva {

  nombreArchivo: string;

  empleadoId: number | null;

  nombreEmpleado: string | null;

  estado: string;

  mensaje: string | null;

}



export interface RespuestaAnalisisCargaMasiva {

  filas: FilaAnalisisCargaMasiva[];

  totalArchivos: number;

  emparejados: number;

  conError: number;

}



export interface ReciboCargadoMasivo {

  empleadoId: number;

  nombreEmpleado: string;

  reciboId: number;

  nombreArchivo: string;

}



export interface FilaResultadoCargaMasiva {

  nombreArchivo: string;

  motivo: string;

}



export interface RespuestaCargaMasivaRecibos {

  cargados: ReciboCargadoMasivo[];

  omitidos: FilaResultadoCargaMasiva[];

  errores: FilaResultadoCargaMasiva[];

}



export const TEXTO_LEGAL_FIRMA_RECIBO = `

Al confirmar la recepción de este recibo de sueldo, usted declara bajo firma electrónica simple

(Ley 25.506) haber tomado conocimiento de su contenido. La constancia digital de entrega se rige

por la Ley 27.802 y el Art. 140 de la Ley de Contrato de Trabajo.



Efectos: la confirmación acredita la puesta a disposición del recibo; la disconformidad queda

registrada con fecha, identidad y hash del documento. El empleador conservará el expediente

probatorio por el plazo legal de diez años.



Derechos: puede manifestar disconformidad indicando el motivo; el documento original y la cadena

de eventos permanecen inalterables para fines de auditoría o reclamo.

`.trim();

export function tieneIntegridadVerificable(recibo: ReciboResumen): boolean {
  return !recibo.requiereRepublicacion && !!recibo.urlDescargaPdf;
}
