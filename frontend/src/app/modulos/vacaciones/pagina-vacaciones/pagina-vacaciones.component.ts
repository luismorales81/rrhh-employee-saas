import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import { BuscadorEmpleadoComponent } from '../../../compartido/componentes/buscador-empleado/buscador-empleado.component';
import { CalendarioVacacionesComponent } from '../../../compartido/componentes/calendario-vacaciones/calendario-vacaciones.component';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import {
  ConfiguracionVacacionesResumen,
  DetalleSolicitudVacaciones,
  DIAS_SEMANA,
  DiaSemana,
  ErrorValidacionVacaciones,
  EventoCalendarioVacaciones,
  PatronDescansoResumen,
  PoliticaVacacionesResumen,
  ServicioVacaciones,
  SolicitudVacacionesResumen,
  SaldoVacacionesResumen
} from '../../../nucleo/servicios/servicio-vacaciones';

type PestanaVacaciones =
  | 'bandeja'
  | 'saldos'
  | 'politicas'
  | 'calendario'
  | 'configuracion'
  | 'patron-descanso';

@Component({
  selector: 'app-pagina-vacaciones',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    DirectivaTienePermiso,
    BuscadorEmpleadoComponent,
    CalendarioVacacionesComponent
  ],
  templateUrl: './pagina-vacaciones.component.html'
})
export class PaginaVacacionesComponent implements OnInit, OnDestroy {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly solicitudes = signal<SolicitudVacacionesResumen[]>([]);
  readonly saldos = signal<SaldoVacacionesResumen[]>([]);
  readonly politicas = signal<PoliticaVacacionesResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly patrones = signal<PatronDescansoResumen[]>([]);
  readonly configuracion = signal<ConfiguracionVacacionesResumen | null>(null);
  readonly detalle = signal<DetalleSolicitudVacaciones | null>(null);
  readonly pestanaActiva = signal<PestanaVacaciones>('bandeja');
  readonly eventosCalendario = signal<EventoCalendarioVacaciones[]>([]);
  readonly mostrarFormularioPolitica = signal(false);
  readonly filtroEstado = signal('EN_REVISION');
  readonly diasPatronSeleccionados = signal<DiaSemana[]>(['SABADO', 'DOMINGO']);
  readonly formularioPolitica;
  readonly formularioConfiguracion;
  readonly formularioAprobar;
  readonly formularioContrapropuesta;
  readonly formularioRechazar;
  readonly formularioPatron;
  readonly diasSemana = DIAS_SEMANA;

  private suscripcionEmpleadoPatron?: Subscription;

  constructor(
    fb: FormBuilder,
    private readonly servicioVacaciones: ServicioVacaciones,
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly ruta: ActivatedRoute
  ) {
    this.formularioPolitica = fb.nonNullable.group({
      nombre: ['Política estándar', Validators.required],
      descripcion: [''],
      aniosMinimos: [0, Validators.required],
      aniosMaximos: [''],
      diasOtorgados: [14, [Validators.required, Validators.min(1)]]
    });

    this.formularioConfiguracion = fb.nonNullable.group({
      diasMinimosFraccion: [7, [Validators.required, Validators.min(7)]],
      anticipacionMinimaDias: [45, [Validators.required, Validators.min(0)]],
      permiteSolicitudSinSaldo: [false]
    });

    this.formularioAprobar = fb.nonNullable.group({
      fechaInicioAprobada: [''],
      fechaFinAprobada: [''],
      comentarios: ['']
    });

    this.formularioContrapropuesta = fb.nonNullable.group({
      fechaInicioContrapropuesta: ['', Validators.required],
      fechaFinContrapropuesta: ['', Validators.required],
      motivo: ['', Validators.required]
    });

    this.formularioRechazar = fb.nonNullable.group({
      motivoRechazo: ['', Validators.required]
    });

    this.formularioPatron = fb.nonNullable.group({
      empleadoId: [null as number | null, Validators.required],
      vigenciaDesde: ['', Validators.required],
      vigenciaHasta: ['']
    });
  }

  ngOnDestroy(): void {
    this.suscripcionEmpleadoPatron?.unsubscribe();
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
    this.recargar();

    this.ruta.queryParamMap.subscribe((params) => {
      const solicitudVacacionesId = params.get('solicitud');
      if (solicitudVacacionesId) {
        this.pestanaActiva.set('bandeja');
        this.abrirDetalle(Number(solicitudVacacionesId));
        return;
      }

      const workflowId = params.get('workflow');
      if (workflowId) {
        this.pestanaActiva.set('bandeja');
        this.abrirPorSolicitudWorkflow(Number(workflowId));
      }
    });

    this.suscripcionEmpleadoPatron = this.formularioPatron.controls.empleadoId.valueChanges.subscribe(() => {
      this.cargarPatronesEmpleado();
    });
  }

  necesitaCargaInicial(): boolean {
    const pestana = this.pestanaActiva();
    return pestana === 'bandeja' || pestana === 'saldos' || pestana === 'politicas';
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    const estado = this.filtroEstado() || undefined;
    forkJoin({
      solicitudes: this.servicioVacaciones.listarSolicitudes(estado).pipe(catchError(() => of([]))),
      politicas: this.servicioVacaciones.listarPoliticas().pipe(catchError(() => of([]))),
      saldos: this.servicioVacaciones.listarSaldos().pipe(catchError(() => of([])))
    }).pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: ({ solicitudes, politicas, saldos }) => {
        this.solicitudes.set(solicitudes);
        this.politicas.set(politicas);
        this.saldos.set(saldos);
      },
      error: () => this.error.set('Error al cargar datos de vacaciones')
    });
  }

  cambiarPestana(pestana: PestanaVacaciones): void {
    this.pestanaActiva.set(pestana);
    this.detalle.set(null);
    this.error.set(null);
    if (pestana === 'calendario') {
      this.cargarCalendario();
    }
    if (pestana === 'configuracion') {
      this.cargarConfiguracion();
    }
    if (pestana === 'patron-descanso') {
      this.cargarPatronesEmpleado();
    }
  }

  cambiarFiltroEstado(estado: string): void {
    this.filtroEstado.set(estado);
    this.servicioVacaciones.listarSolicitudes(estado || undefined).subscribe({
      next: (lista) => this.solicitudes.set(lista)
    });
  }

  abrirDetalle(solicitudVacacionesId: number): void {
    this.error.set(null);
    this.servicioVacaciones.obtenerDetalle(solicitudVacacionesId).subscribe({
      next: (d) => {
        this.detalle.set(d);
        const sol = d.solicitud;
        this.formularioAprobar.patchValue({
          fechaInicioAprobada: sol.fechaInicioAprobada ?? sol.fechaInicio,
          fechaFinAprobada: sol.fechaFinAprobada ?? sol.fechaFin,
          comentarios: ''
        });
        this.formularioContrapropuesta.patchValue({
          fechaInicioContrapropuesta: sol.fechaInicioContrapropuesta ?? sol.fechaInicio,
          fechaFinContrapropuesta: sol.fechaFinContrapropuesta ?? sol.fechaFin,
          motivo: sol.motivoContrapropuesta ?? ''
        });
      },
      error: (err: { error?: { mensaje?: string } }) =>
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar el detalle')
    });
  }

  cerrarDetalle(): void {
    this.detalle.set(null);
  }

  filaSeleccionada(solicitudVacacionesId: number): boolean {
    return this.detalle()?.solicitud?.solicitudVacacionesId === solicitudVacacionesId;
  }

  private abrirPorSolicitudWorkflow(solicitudId: number): void {
    this.servicioVacaciones.listarSolicitudes().subscribe({
      next: (lista) => {
        const encontrada = lista.find((s) => s.solicitudId === solicitudId);
        if (encontrada) {
          this.abrirDetalle(encontrada.solicitudVacacionesId);
          return;
        }
        this.error.set(
          'No hay registro de vacaciones para esta solicitud. Es un pedido incompleto: dalo de baja desde Solicitudes.'
        );
      },
      error: () => this.error.set('No se pudo buscar la solicitud de vacaciones')
    });
  }

  cargarCalendario(desde?: string, hasta?: string): void {
    const hoy = new Date();
    const fechaDesde = desde ?? this.formatearFecha(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    const fechaHasta = hasta ?? this.formatearFecha(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0));
    this.servicioVacaciones.obtenerCalendario(fechaDesde, fechaHasta).subscribe({
      next: (eventos) => this.eventosCalendario.set(eventos),
      error: () => this.error.set('No se pudo cargar el calendario')
    });
  }

  alCambiarMesCalendario(rango: { desde: string; hasta: string }): void {
    this.cargarCalendario(rango.desde, rango.hasta);
  }

  cargarConfiguracion(): void {
    this.servicioVacaciones.obtenerConfiguracion().subscribe({
      next: (config) => {
        this.configuracion.set(config);
        this.formularioConfiguracion.patchValue({
          diasMinimosFraccion: config.diasMinimosFraccion,
          anticipacionMinimaDias: config.anticipacionMinimaDias,
          permiteSolicitudSinSaldo: config.permiteSolicitudSinSaldo
        });
      },
      error: (err: { error?: { mensaje?: string } }) =>
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar la configuración')
    });
  }

  guardarConfiguracion(): void {
    if (this.formularioConfiguracion.invalid) {
      this.formularioConfiguracion.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.servicioVacaciones.actualizarConfiguracion(this.formularioConfiguracion.getRawValue()).subscribe({
      next: (config) => {
        this.configuracion.set(config);
        this.guardando.set(false);
      },
      error: (err: { error?: { mensaje?: string } }) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar la configuración');
      }
    });
  }

  cargarPatronesEmpleado(): void {
    const empleadoId = this.formularioPatron.controls.empleadoId.value;
    if (!empleadoId) {
      this.patrones.set([]);
      return;
    }
    this.servicioVacaciones.listarPatronesDescanso(empleadoId).subscribe({
      next: (lista) => this.patrones.set(lista),
      error: (err: { error?: { mensaje?: string } }) => {
        this.patrones.set([]);
        this.error.set(err.error?.mensaje ?? 'No se pudieron cargar los patrones de descanso');
      }
    });
  }

  alternarDiaPatron(dia: DiaSemana): void {
    this.diasPatronSeleccionados.update((actual) => {
      if (actual.includes(dia)) {
        return actual.filter((d) => d !== dia);
      }
      return [...actual, dia];
    });
  }

  diaPatronActivo(dia: DiaSemana): boolean {
    return this.diasPatronSeleccionados().includes(dia);
  }

  crearPatronDescanso(): void {
    if (this.formularioPatron.invalid || this.diasPatronSeleccionados().length === 0) {
      this.formularioPatron.markAllAsTouched();
      if (this.diasPatronSeleccionados().length === 0) {
        this.error.set('Seleccioná al menos un día de descanso.');
      }
      return;
    }
    const datos = this.formularioPatron.getRawValue();
    const empleadoId = datos.empleadoId;
    if (!empleadoId) {
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.servicioVacaciones.crearPatronDescanso(empleadoId, {
      diasDescanso: this.diasPatronSeleccionados(),
      vigenciaDesde: datos.vigenciaDesde,
      vigenciaHasta: datos.vigenciaHasta || null
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioPatron.patchValue({ vigenciaHasta: '' });
        this.cargarPatronesEmpleado();
      },
      error: (err: { error?: { mensaje?: string } }) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear el patrón de descanso');
      }
    });
  }

  etiquetaDiasPatron(dias: DiaSemana[]): string {
    return dias
      .map((dia) => this.diasSemana.find((item) => item.valor === dia)?.etiqueta ?? dia)
      .join(', ');
  }

  aprobarSolicitud(): void {
    const det = this.detalle();
    if (!det) {
      return;
    }
    const datos = this.formularioAprobar.getRawValue();
    this.guardando.set(true);
    this.servicioVacaciones.aprobar(det.solicitud.solicitudVacacionesId, {
      fechaInicioAprobada: datos.fechaInicioAprobada || null,
      fechaFinAprobada: datos.fechaFinAprobada || null,
      comentarios: datos.comentarios || null
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarDetalle();
        this.recargar();
      },
      error: (err: { status?: number; error?: ErrorValidacionVacaciones }) => this.manejarErrorAccion(err)
    });
  }

  contraproponerSolicitud(): void {
    const det = this.detalle();
    if (!det || this.formularioContrapropuesta.invalid) {
      this.formularioContrapropuesta.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.servicioVacaciones.contraproponer(
      det.solicitud.solicitudVacacionesId,
      this.formularioContrapropuesta.getRawValue()
    ).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarDetalle();
        this.recargar();
      },
      error: (err: { status?: number; error?: ErrorValidacionVacaciones }) => this.manejarErrorAccion(err)
    });
  }

  rechazarSolicitud(): void {
    const det = this.detalle();
    if (!det || this.formularioRechazar.invalid) {
      this.formularioRechazar.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.servicioVacaciones.rechazar(det.solicitud.solicitudVacacionesId, {
      motivoRechazo: this.formularioRechazar.controls.motivoRechazo.value
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarDetalle();
        this.recargar();
      },
      error: (err: { status?: number; error?: ErrorValidacionVacaciones }) => this.manejarErrorAccion(err)
    });
  }

  private manejarErrorAccion(err: { status?: number; error?: ErrorValidacionVacaciones }): void {
    this.guardando.set(false);
    const cuerpo = err.error;
    if (err.status === 422 && cuerpo?.errores?.length) {
      this.error.set(cuerpo.errores.join(' · '));
    } else {
      this.error.set(cuerpo?.mensaje ?? 'No se pudo completar la acción');
    }
  }

  private formatearFecha(fecha: Date): string {
    return fecha.toISOString().slice(0, 10);
  }

  crearPolitica(): void {
    if (this.formularioPolitica.invalid) {
      this.formularioPolitica.markAllAsTouched();
      return;
    }

    const datos = this.formularioPolitica.getRawValue();
    const aniosMax = datos.aniosMaximos ? Number(datos.aniosMaximos) : null;

    this.guardando.set(true);
    this.servicioVacaciones.crearPolitica({
      nombre: datos.nombre,
      descripcion: datos.descripcion || undefined,
      rangos: [{
        aniosMinimos: Number(datos.aniosMinimos),
        aniosMaximos: aniosMax,
        diasOtorgados: Number(datos.diasOtorgados)
      }]
    }).subscribe({
      next: (politica) => {
        this.servicioVacaciones.activarPolitica(politica.id).subscribe({
          next: () => {
            this.guardando.set(false);
            this.mostrarFormularioPolitica.set(false);
            this.recargar();
          },
          error: (err: { error?: { mensaje?: string } }) => {
            this.guardando.set(false);
            this.error.set(err.error?.mensaje ?? 'Política creada pero no se pudo activar');
            this.recargar();
          }
        });
      },
      error: (err: { error?: { mensaje?: string } }) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear la política');
      }
    });
  }

  activarPolitica(id: number): void {
    this.servicioVacaciones.activarPolitica(id).subscribe({
      next: () => this.recargar(),
      error: (err: { error?: { mensaje?: string } }) =>
        this.error.set(err.error?.mensaje ?? 'No se pudo activar la política')
    });
  }

  puedeGestionar(det: DetalleSolicitudVacaciones): boolean {
    return det.solicitud.estado === 'EN_REVISION';
  }
}
