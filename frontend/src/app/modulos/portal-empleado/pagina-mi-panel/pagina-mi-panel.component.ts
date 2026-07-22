import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ServicioSolicitudes } from '../../../nucleo/servicios/servicio-solicitudes';
import { ServicioDocumentos } from '../../../nucleo/servicios/servicio-documentos';
import { ServicioComunicaciones } from '../../../nucleo/servicios/servicio-comunicaciones';
import { ServicioNotificaciones } from '../../../nucleo/servicios/servicio-notificaciones';
import { ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import {
  ServicioOnboarding,
  TareaOnboardingEmpleadoResumen
} from '../../../nucleo/servicios/servicio-onboarding';
import { ServicioRecibos } from '../../../nucleo/servicios/servicio-recibos';

interface TarjetaIndicador {
  titulo: string;
  valor: number;
  icono: string;
  ruta?: string;
  alerta?: boolean;
}

@Component({
  selector: 'app-pagina-mi-panel',
  standalone: true,
  imports: [RouterLink, EsqueletoCargaComponent],
  templateUrl: './pagina-mi-panel.component.html',
  styleUrl: './pagina-mi-panel.component.scss'
})
export class PaginaMiPanelComponent implements OnInit {
  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly error = signal<string | null>(null);
  readonly tarjetas = signal<TarjetaIndicador[]>([]);
  readonly estadoOnboarding = signal<string | null>(null);
  readonly tareasOnboarding = signal<TareaOnboardingEmpleadoResumen[]>([]);
  private empleadoId = 0;

  constructor(
    private readonly servicioSolicitudes: ServicioSolicitudes,
    private readonly servicioDocumentos: ServicioDocumentos,
    private readonly servicioComunicaciones: ServicioComunicaciones,
    private readonly servicioNotificaciones: ServicioNotificaciones,
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly servicioOnboarding: ServicioOnboarding,
    private readonly servicioRecibos: ServicioRecibos
  ) {}

  ngOnInit(): void {
    this.servicioEmpleados.obtenerPropio().pipe(
      switchMap((empleado) => {
        this.empleadoId = empleado.id;
        return this.servicioEmpleados.obtenerLegajo(empleado.id).pipe(catchError(() => of(null)));
      }),
      catchError(() => of(null))
    ).subscribe((legajo) => {
      if (legajo?.estadoOnboarding === 'EN_PROGRESO') {
        this.estadoOnboarding.set('EN_PROGRESO');
        this.cargarTareasOnboarding();
      }
    });

    forkJoin({
      solicitudes: this.servicioSolicitudes.listarPropias().pipe(catchError(() => of([]))),
      documentos: this.servicioDocumentos.listarMisAsignaciones().pipe(catchError(() => of([]))),
      comunicaciones: this.servicioComunicaciones.listarMisComunicaciones().pipe(catchError(() => of([]))),
      notificaciones: this.servicioNotificaciones.contarNoLeidas().pipe(catchError(() => of({ cantidad: 0 }))),
      recibos: this.servicioRecibos.listarPropios().pipe(catchError(() => of([])))
    }).subscribe({
      next: (datos) => {
        const solicitudesPendientes = datos.solicitudes.filter(
          (s) => s.estado === 'EN_REVISION' || s.estado === 'BORRADOR'
        ).length;
        const documentosSinFirmar = datos.documentos.filter(
          (d) => d.requiereFirma && !d.firmado
        ).length;
        const recibosSinFirmar = datos.recibos.filter(
          (r) => r.estado === 'PENDIENTE' || r.estado === 'VISTO'
        ).length;
        const notificacionesNoLeidas = datos.notificaciones.cantidad;

        this.tarjetas.set([
          {
            titulo: 'Solicitudes pendientes',
            valor: solicitudesPendientes,
            icono: '📋',
            ruta: '/mi-solicitudes',
            alerta: solicitudesPendientes > 0
          },
          {
            titulo: 'Documentos sin firmar',
            valor: documentosSinFirmar,
            icono: '📄',
            ruta: '/mi-documentos',
            alerta: documentosSinFirmar > 0
          },
          {
            titulo: 'Recibos sin firmar',
            valor: recibosSinFirmar,
            icono: '💵',
            ruta: '/mis-recibos',
            alerta: recibosSinFirmar > 0
          },
          {
            titulo: 'Notificaciones',
            valor: notificacionesNoLeidas,
            icono: '🔔',
            ruta: '/mi-notificaciones',
            alerta: notificacionesNoLeidas > 0
          }
        ]);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el panel');
        this.cargando.set(false);
      }
    });
  }

  cargarTareasOnboarding(): void {
    if (!this.empleadoId) return;
    this.servicioOnboarding.listarTareasEmpleado(this.empleadoId).pipe(
      catchError(() => of([]))
    ).subscribe((tareas) => this.tareasOnboarding.set(tareas));
  }

  marcarTareaCompletada(tarea: TareaOnboardingEmpleadoResumen): void {
    if (tarea.completada || !this.empleadoId) return;
    this.procesando.set(true);
    this.servicioOnboarding.completarTarea(this.empleadoId, tarea.id).subscribe({
      next: () => {
        this.procesando.set(false);
        this.cargarTareasOnboarding();
      },
      error: () => {
        this.procesando.set(false);
        this.error.set('No se pudo completar la tarea');
      }
    });
  }

  tareasCompletadas(): number {
    return this.tareasOnboarding().filter((t) => t.completada).length;
  }
}
