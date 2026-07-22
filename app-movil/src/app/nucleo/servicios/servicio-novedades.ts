import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioNovedades {
  private readonly urlBase = `${environment.urlApi}/novedades`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<NovedadLiquidacionResumen[]> {
    return this.http.get<NovedadLiquidacionResumen[]>(this.urlBase);
  }

  crear(solicitud: SolicitudCrearNovedadLiquidacion): Observable<NovedadLiquidacionResumen> {
    return this.http.post<NovedadLiquidacionResumen>(this.urlBase, solicitud);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/${id}`);
  }

  exportarCsv(periodo: string): Observable<Blob> {
    const params = new HttpParams().set('periodo', periodo);
    return this.http.get(`${this.urlBase}/exportar`, { params, responseType: 'blob' });
  }

  confirmarLote(periodo: string): Observable<RespuestaConfirmacionNovedades> {
    const params = new HttpParams().set('periodo', periodo);
    return this.http.post<RespuestaConfirmacionNovedades>(`${this.urlBase}/confirmar`, {}, { params });
  }
}

export type EstadoNovedadLiquidacion = 'BORRADOR' | 'CONFIRMADA' | 'EXPORTADA';

export type TipoNovedadLiquidacion =
  | 'HORAS_EXTRAS'
  | 'BONIFICACION'
  | 'COMISION'
  | 'AUSENCIA'
  | 'LICENCIA'
  | 'OTRO';

export interface NovedadLiquidacionResumen {
  id: number;
  empleadoId: number;
  nombreEmpleado: string;
  periodo: string;
  tipo: TipoNovedadLiquidacion;
  descripcion: string | null;
  cantidad: number | null;
  monto: number | null;
  fecha: string;
  estado: EstadoNovedadLiquidacion;
}

export interface RespuestaConfirmacionNovedades {
  periodo: string;
  cantidadConfirmadas: number;
}

export interface SolicitudCrearNovedadLiquidacion {
  empleadoId: number;
  periodo: string;
  tipo: TipoNovedadLiquidacion;
  descripcion?: string;
  cantidad?: number;
  monto?: number;
  fecha: string;
}
