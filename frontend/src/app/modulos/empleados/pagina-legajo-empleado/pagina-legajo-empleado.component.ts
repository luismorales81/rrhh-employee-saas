import { Component, OnInit, signal } from '@angular/core';

import { ActivatedRoute, RouterLink } from '@angular/router';

import { DatePipe } from '@angular/common';

import { forkJoin, of } from 'rxjs';

import { catchError, map, switchMap } from 'rxjs/operators';

import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';

import {

  LegajoEmpleadoResumen,

  ServicioEmpleados

} from '../../../nucleo/servicios/servicio-empleados';

import {

  ServicioOnboarding,

  TareaOnboardingEmpleadoResumen

} from '../../../nucleo/servicios/servicio-onboarding';

import {

  ServicioOffboarding,

  TareaOffboardingEmpleadoResumen

} from '../../../nucleo/servicios/servicio-offboarding';

import {

  AsignacionActivoResumen,

  ServicioActivos

} from '../../../nucleo/servicios/servicio-activos';

import { ReciboResumen, ServicioRecibos } from '../../../nucleo/servicios/servicio-recibos';



@Component({

  selector: 'app-pagina-legajo-empleado',

  standalone: true,

  imports: [RouterLink, DirectivaTienePermiso, DatePipe],

  templateUrl: './pagina-legajo-empleado.component.html',
  styleUrl: './pagina-legajo-empleado.component.scss'
})

export class PaginaLegajoEmpleadoComponent implements OnInit {

  readonly cargando = signal(true);

  readonly procesando = signal(false);

  readonly error = signal<string | null>(null);

  readonly legajo = signal<LegajoEmpleadoResumen | null>(null);

  readonly tareasOnboarding = signal<TareaOnboardingEmpleadoResumen[]>([]);

  readonly tareasOffboarding = signal<TareaOffboardingEmpleadoResumen[]>([]);

  readonly activosAsignados = signal<AsignacionActivoResumen[]>([]);

  readonly recibosEmpleado = signal<ReciboResumen[]>([]);

  readonly cargandoRecibos = signal(false);

  readonly cargandoActivos = signal(false);

  private empleadoId = 0;



  constructor(

    private readonly ruta: ActivatedRoute,

    private readonly servicioEmpleados: ServicioEmpleados,

    private readonly servicioOnboarding: ServicioOnboarding,

    private readonly servicioOffboarding: ServicioOffboarding,

    private readonly servicioActivos: ServicioActivos,

    private readonly servicioRecibos: ServicioRecibos

  ) {}



  ngOnInit(): void {

    const idParam = this.ruta.snapshot.paramMap.get('id');

    this.empleadoId = Number(idParam);



    if (!this.empleadoId) {

      this.error.set('Identificador de empleado inválido');

      this.cargando.set(false);

      return;

    }



    this.cargarLegajo();

  }



  cargarLegajo(): void {

    this.cargando.set(true);

    this.error.set(null);



    this.servicioEmpleados.obtenerLegajo(this.empleadoId).subscribe({

      next: (datos) => {

        this.legajo.set(datos);

        this.cargando.set(false);

        if (datos.estadoOnboarding === 'EN_PROGRESO') {

          this.cargarTareasOnboarding();

        } else {

          this.tareasOnboarding.set([]);

        }

        if (this.mostrarOffboarding(datos)) {

          this.cargarTareasOffboarding();

        } else {

          this.tareasOffboarding.set([]);

        }

        this.cargarActivosAsignados();

        this.cargarRecibosEmpleado();

      },

      error: () => {

        this.error.set('No se pudo cargar el legajo del empleado');

        this.cargando.set(false);

      }

    });

  }



  cargarTareasOnboarding(): void {

    this.servicioOnboarding.listarTareasEmpleado(this.empleadoId).pipe(

      catchError(() => of([]))

    ).subscribe((tareas) => this.tareasOnboarding.set(tareas));

  }



  cargarTareasOffboarding(): void {

    this.servicioOffboarding.listarTareasEmpleado(this.empleadoId).pipe(

      catchError(() => of([]))

    ).subscribe((tareas) => this.tareasOffboarding.set(tareas));

  }



  cargarRecibosEmpleado(): void {

    this.cargandoRecibos.set(true);

    this.servicioRecibos.listarPorEmpleado(this.empleadoId).pipe(

      catchError(() => of([] as ReciboResumen[]))

    ).subscribe({

      next: (lista) => {

        this.recibosEmpleado.set(lista.filter((r) => r.vigente));

        this.cargandoRecibos.set(false);

      },

      error: () => {

        this.recibosEmpleado.set([]);

        this.cargandoRecibos.set(false);

      }

    });

  }



  exportarExpedienteRecibo(reciboId: number, periodo: string): void {

    this.servicioRecibos.exportarExpediente(reciboId).subscribe({

      next: (blob) => {

        const url = URL.createObjectURL(blob);

        const enlace = document.createElement('a');

        enlace.href = url;

        enlace.download = `expediente-legajo-${periodo}.zip`;

        enlace.click();

        URL.revokeObjectURL(url);

      },

      error: () => this.error.set('No se pudo exportar el expediente del recibo')

    });

  }



  cargarActivosAsignados(): void {

    this.cargandoActivos.set(true);

    this.servicioActivos.listarAsignacionesEmpleado(this.empleadoId).pipe(

      catchError(() => this.obtenerActivosPorConsultasIndividuales())

    ).subscribe({

      next: (asignaciones) => {

        this.activosAsignados.set(asignaciones.filter((a) => !a.fechaDevolucion));

        this.cargandoActivos.set(false);

      },

      error: () => {

        this.activosAsignados.set([]);

        this.cargandoActivos.set(false);

      }

    });

  }



  private obtenerActivosPorConsultasIndividuales() {

    return this.servicioActivos.listar().pipe(

      switchMap((activos) => {

        const asignados = activos.filter((a) => a.estado === 'ASIGNADO');

        if (asignados.length === 0) {

          return of([] as AsignacionActivoResumen[]);

        }

        return forkJoin(

          asignados.map((activo) =>

            this.servicioActivos.listarAsignacionesActivo(activo.id).pipe(

              map((asignaciones) =>

                asignaciones.filter(

                  (a) => a.empleadoId === this.empleadoId && !a.fechaDevolucion

                )

              ),

              catchError(() => of([] as AsignacionActivoResumen[]))

            )

          )

        ).pipe(map((grupos) => grupos.flat()));

      })

    );

  }



  iniciarOnboarding(): void {

    this.procesando.set(true);

    this.servicioEmpleados.iniciarOnboarding(this.empleadoId).subscribe({

      next: (datos) => {

        this.legajo.set(datos);

        this.procesando.set(false);

        this.cargarTareasOnboarding();

      },

      error: (err) => {

        this.procesando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo iniciar el onboarding');

      }

    });

  }



  completarOnboarding(): void {

    this.procesando.set(true);

    this.servicioEmpleados.completarOnboarding(this.empleadoId).subscribe({

      next: (datos) => {

        this.legajo.set(datos);

        this.procesando.set(false);

        this.tareasOnboarding.set([]);

      },

      error: (err) => {

        this.procesando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo completar el onboarding');

      }

    });

  }



  marcarTareaOnboardingCompletada(tarea: TareaOnboardingEmpleadoResumen): void {

    if (tarea.completada) return;

    const notas = prompt('Notas opcionales al completar la tarea:') ?? undefined;

    this.procesando.set(true);

    this.servicioOnboarding.completarTarea(this.empleadoId, tarea.id, notas || undefined).subscribe({

      next: () => {

        this.procesando.set(false);

        this.cargarTareasOnboarding();

        this.servicioEmpleados.obtenerLegajo(this.empleadoId).subscribe({

          next: (datos) => this.legajo.set(datos)

        });

      },

      error: (err) => {

        this.procesando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo completar la tarea');

      }

    });

  }



  marcarTareaOffboardingCompletada(tarea: TareaOffboardingEmpleadoResumen): void {

    if (tarea.completada) return;

    const notas = prompt('Notas opcionales al completar la tarea:') ?? undefined;

    this.procesando.set(true);

    this.servicioOffboarding.completarTarea(this.empleadoId, tarea.id, notas || undefined).subscribe({

      next: () => {

        this.procesando.set(false);

        this.cargarTareasOffboarding();

        this.servicioEmpleados.obtenerLegajo(this.empleadoId).subscribe({

          next: (datos) => this.legajo.set(datos)

        });

      },

      error: (err) => {

        this.procesando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo completar la tarea de offboarding');

      }

    });

  }



  puedeIniciarOnboarding(): boolean {

    const datos = this.legajo();

    return datos !== null && datos.estadoOnboarding === 'PENDIENTE';

  }



  puedeCompletarOnboarding(): boolean {

    const datos = this.legajo();

    return datos !== null && datos.estadoOnboarding === 'EN_PROGRESO';

  }



  mostrarOffboarding(datos: LegajoEmpleadoResumen): boolean {

    return datos.estado === 'DESVINCULADO' || datos.estadoOffboarding === 'EN_PROGRESO';

  }



  tareasCompletadasOnboarding(): number {

    return this.tareasOnboarding().filter((t) => t.completada).length;

  }



  tareasCompletadasOffboarding(): number {

    return this.tareasOffboarding().filter((t) => t.completada).length;

  }



  porcentajeOnboarding(): number {

    const total = this.tareasOnboarding().length;

    if (total === 0) return 0;

    return Math.round((this.tareasCompletadasOnboarding() / total) * 100);

  }



  porcentajeOffboarding(): number {

    const total = this.tareasOffboarding().length;

    if (total === 0) return 0;

    return Math.round((this.tareasCompletadasOffboarding() / total) * 100);

  }

}


