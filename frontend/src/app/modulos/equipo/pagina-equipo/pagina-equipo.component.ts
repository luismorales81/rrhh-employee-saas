import { Component, OnInit, computed, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServicioSolicitudes, SolicitudResumen } from '../../../nucleo/servicios/servicio-solicitudes';
import { ServicioVacaciones } from '../../../nucleo/servicios/servicio-vacaciones';
import { ServicioAusentismo } from '../../../nucleo/servicios/servicio-ausentismo';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { formatearEstadoSolicitud } from '../../../nucleo/utilidades/formatear-estado-solicitud';
import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { EstadoVacioComponent, InsigniaComponent, MigasComponent } from '../../../ui';

@Component({
  selector: 'app-pagina-equipo',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    EsqueletoCargaComponent,
    EstadoVacioComponent,
    InsigniaComponent,
    MigasComponent
  ],
  templateUrl: './pagina-equipo.component.html',
  styleUrl: './pagina-equipo.component.scss'
})
export class PaginaEquipoComponent implements OnInit {
  readonly cargando = signal(true);
  readonly pendientes = signal<SolicitudResumen[]>([]);
  readonly vacacionesPendientes = signal(0);
  readonly ausenciasHoy = signal(0);

  readonly saludo = computed(() => {
    const h = new Date().getHours();
    if (h < 12) return 'Buenos días';
    if (h < 19) return 'Buenas tardes';
    return 'Buenas noches';
  });

  readonly nombre = computed(() => {
    const email = this.servicioAutenticacion.sesionActual()?.email ?? '';
    return email.split('@')[0] || 'equipo';
  });

  readonly migas = [
    { etiqueta: 'Mi equipo', ruta: '/equipo' },
    { etiqueta: 'Inicio' }
  ];

  constructor(
    private readonly servicioSolicitudes: ServicioSolicitudes,
    private readonly servicioVacaciones: ServicioVacaciones,
    private readonly servicioAusentismo: ServicioAusentismo,
    private readonly servicioAutenticacion: ServicioAutenticacion
  ) {}

  ngOnInit(): void {
    forkJoin({
      solicitudes: this.servicioSolicitudes.listarPendientes().pipe(catchError(() => of([]))),
      vacaciones: this.servicioVacaciones.listarSolicitudes().pipe(catchError(() => of([]))),
      ausencias: this.servicioAusentismo.listarAusencias().pipe(catchError(() => of([])))
    }).subscribe((datos) => {
      this.pendientes.set(datos.solicitudes.slice(0, 8));
      this.vacacionesPendientes.set(
        datos.vacaciones.filter((v) => v.estado === 'EN_REVISION' || v.estado === 'PENDIENTE').length
      );
      const hoy = new Date().toISOString().slice(0, 10);
      this.ausenciasHoy.set(datos.ausencias.filter((a) => a.fecha === hoy).length);
      this.cargando.set(false);
    });
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }
}
