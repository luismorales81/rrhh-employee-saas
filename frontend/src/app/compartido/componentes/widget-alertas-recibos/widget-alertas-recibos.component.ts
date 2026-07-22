import { DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EsqueletoCargaComponent } from '../esqueleto-carga/esqueleto-carga.component';
import {
  ReciboResumen,
  RespuestaAlertasRecibos,
  ServicioRecibos
} from '../../../nucleo/servicios/servicio-recibos';

interface PendientesPorPeriodo {
  periodo: string;
  cantidad: number;
}

@Component({
  selector: 'app-widget-alertas-recibos',
  standalone: true,
  imports: [RouterLink, DatePipe, EsqueletoCargaComponent],
  templateUrl: './widget-alertas-recibos.component.html',
  styleUrl: './widget-alertas-recibos.component.scss'
})
export class WidgetAlertasRecibosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly alertas = signal<RespuestaAlertasRecibos | null>(null);
  readonly pendientesPorPeriodo = signal<PendientesPorPeriodo[]>([]);

  constructor(private readonly servicioRecibos: ServicioRecibos) {}

  ngOnInit(): void {
    forkJoin({
      alertas: this.servicioRecibos.obtenerAlertasSeguimiento().pipe(catchError(() => of(null))),
      recibos: this.servicioRecibos.listar().pipe(catchError(() => of([] as ReciboResumen[])))
    }).subscribe(({ alertas, recibos }) => {
      this.alertas.set(alertas);
      this.pendientesPorPeriodo.set(this.agruparPendientesPorPeriodo(recibos));
      this.cargando.set(false);
    });
  }

  diasDesde(fechaPublicacion: string): number {
    const diff = Date.now() - new Date(fechaPublicacion).getTime();
    return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
  }

  etiquetaEstado(estado: string): string {
    const mapa: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      VISTO: 'Visto',
      FIRMADO: 'Firmado',
      DISCONFORMIDAD: 'Disconformidad'
    };
    return mapa[estado] ?? estado;
  }

  private agruparPendientesPorPeriodo(recibos: ReciboResumen[]): PendientesPorPeriodo[] {
    const conteo = new Map<string, number>();
    for (const recibo of recibos) {
      if (recibo.estado !== 'PENDIENTE' && recibo.estado !== 'VISTO') {
        continue;
      }
      conteo.set(recibo.periodo, (conteo.get(recibo.periodo) ?? 0) + 1);
    }
    return [...conteo.entries()]
      .map(([periodo, cantidad]) => ({ periodo, cantidad }))
      .sort((a, b) => b.periodo.localeCompare(a.periodo));
  }
}
