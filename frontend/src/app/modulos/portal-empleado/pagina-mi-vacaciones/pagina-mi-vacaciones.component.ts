import { Component, OnInit, signal } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { DatePipe } from '@angular/common';

import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';

import { CalendarioVacacionesComponent } from '../../../compartido/componentes/calendario-vacaciones/calendario-vacaciones.component';

import {

  ErrorValidacionVacaciones,

  EventoCalendarioVacaciones,

  RespuestaSimularFechaVacaciones,

  SaldoVacacionesResumen,

  ServicioVacaciones,

  SolicitudVacacionesResumen

} from '../../../nucleo/servicios/servicio-vacaciones';
import { formatearEstadoSolicitud } from '../../../nucleo/utilidades/formatear-estado-solicitud';



@Component({

  selector: 'app-pagina-mi-vacaciones',

  standalone: true,

  imports: [ReactiveFormsModule, DatePipe, CalendarioVacacionesComponent, EsqueletoCargaComponent],

  templateUrl: './pagina-mi-vacaciones.component.html'

})

export class PaginaMiVacacionesComponent implements OnInit {

  readonly cargando = signal(true);

  readonly guardando = signal(false);

  readonly simulando = signal(false);

  readonly error = signal<string | null>(null);

  readonly erroresValidacion = signal<string[]>([]);

  readonly advertencias = signal<string[]>([]);

  readonly saldo = signal<SaldoVacacionesResumen | null>(null);

  readonly historialSaldo = signal<SaldoVacacionesResumen[]>([]);

  readonly solicitudes = signal<SolicitudVacacionesResumen[]>([]);

  readonly eventosCalendario = signal<EventoCalendarioVacaciones[]>([]);

  readonly mostrarFormulario = signal(false);

  readonly simulacionFecha = signal<RespuestaSimularFechaVacaciones | null>(null);

  readonly formularioSolicitar;



  private readonly diasMinimos = 7;



  constructor(

    fb: FormBuilder,

    private readonly servicioVacaciones: ServicioVacaciones

  ) {

    this.formularioSolicitar = fb.nonNullable.group({

      fechaInicio: ['', Validators.required],

      fechaFin: ['', Validators.required],

      motivo: ['']

    });



    this.formularioSolicitar.controls.fechaInicio.valueChanges.subscribe(() => {

      this.erroresValidacion.set([]);

      this.simularFechaInicio();

    });

    this.formularioSolicitar.controls.fechaFin.valueChanges.subscribe(() => {

      this.erroresValidacion.set([]);

      this.simularFechaInicio();

    });

  }



  ngOnInit(): void {

    this.recargar();

  }



  recargar(): void {

    this.cargando.set(true);

    this.error.set(null);



    this.servicioVacaciones.saldoPropio().subscribe({

      next: (saldo) => this.saldo.set(saldo),

      error: () => this.error.set('No se pudo cargar tu saldo de vacaciones')

    });



    this.servicioVacaciones.historialSaldoPropio().subscribe({

      next: (lista) => this.historialSaldo.set(lista)

    });



    this.servicioVacaciones.listarPropias().subscribe({

      next: (lista) => {

        this.solicitudes.set(lista);

        this.cargando.set(false);

      },

      error: () => {

        this.error.set('No se pudieron cargar tus solicitudes de vacaciones');

        this.cargando.set(false);

      }

    });



    const hoy = new Date();

    this.cargarCalendario(

      this.formatearFecha(new Date(hoy.getFullYear(), hoy.getMonth(), 1)),

      this.formatearFecha(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0))

    );

  }



  cargarCalendario(desde: string, hasta: string): void {

    this.servicioVacaciones.obtenerCalendario(desde, hasta).subscribe({

      next: (eventos) => this.eventosCalendario.set(eventos)

    });

  }



  alCambiarMesCalendario(rango: { desde: string; hasta: string }): void {

    this.cargarCalendario(rango.desde, rango.hasta);

  }



  diasSolicitados(): number {

    const inicio = this.formularioSolicitar.controls.fechaInicio.value;

    const fin = this.formularioSolicitar.controls.fechaFin.value;

    if (!inicio || !fin) {

      return 0;

    }

    const fechaInicio = new Date(inicio + 'T00:00:00');

    const fechaFin = new Date(fin + 'T00:00:00');

    const diff = Math.round((fechaFin.getTime() - fechaInicio.getTime()) / 86_400_000) + 1;

    return diff > 0 ? diff : 0;

  }



  simularFechaInicio(): void {

    const inicio = this.formularioSolicitar.controls.fechaInicio.value;

    const dias = this.diasSolicitados();

    if (!inicio || dias < 1) {

      this.simulacionFecha.set(null);

      return;

    }



    this.simulando.set(true);

    this.servicioVacaciones.simularFecha({ fechaInicioDeseada: inicio, diasDeseados: dias }).subscribe({

      next: (resultado) => {

        this.simulacionFecha.set(resultado);

        this.simulando.set(false);

      },

      error: () => this.simulando.set(false)

    });

  }



  aplicarFechaSugerida(): void {

    const sim = this.simulacionFecha();

    if (!sim?.fechaSugerida) {

      return;

    }

    this.formularioSolicitar.patchValue({ fechaInicio: sim.fechaSugerida });

    const dias = this.diasSolicitados();

    if (dias > 0) {

      const fin = new Date(sim.fechaSugerida + 'T00:00:00');

      fin.setDate(fin.getDate() + dias - 1);

      this.formularioSolicitar.patchValue({ fechaFin: this.formatearFecha(fin) });

    }

  }



  private formatearFecha(fecha: Date): string {

    const anio = fecha.getFullYear();

    const mes = String(fecha.getMonth() + 1).padStart(2, '0');

    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;

  }



  alternarFormulario(): void {

    this.mostrarFormulario.update((v) => !v);

    if (!this.mostrarFormulario()) {

      this.erroresValidacion.set([]);

      this.advertencias.set([]);

      this.simulacionFecha.set(null);

    }

  }



  solicitar(): void {

    if (this.formularioSolicitar.invalid) {

      this.formularioSolicitar.markAllAsTouched();

      return;

    }



    const dias = this.diasSolicitados();

    if (dias < this.diasMinimos) {

      this.erroresValidacion.set([`La fracción mínima es de ${this.diasMinimos} días corridos (Ley 27.802).`]);

      return;

    }



    const sim = this.simulacionFecha();

    if (sim && !sim.esValida) {

      this.erroresValidacion.set([

        sim.motivo ?? 'La fecha de inicio no es válida según tu patrón de descanso (Art. 151 LCT).'

      ]);

      return;

    }



    const datos = this.formularioSolicitar.getRawValue();

    this.guardando.set(true);

    this.error.set(null);

    this.erroresValidacion.set([]);

    this.advertencias.set([]);



    this.servicioVacaciones.solicitar({

      fechaInicio: datos.fechaInicio,

      fechaFin: datos.fechaFin,

      motivo: datos.motivo || undefined

    }).subscribe({

      next: (respuesta) => {

        this.guardando.set(false);

        if (respuesta.advertencias?.length) {

          this.advertencias.set(respuesta.advertencias);

        }

        this.formularioSolicitar.reset();

        this.mostrarFormulario.set(false);

        this.simulacionFecha.set(null);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        const cuerpo = err.error as ErrorValidacionVacaciones | undefined;

        if (err.status === 422 && cuerpo?.errores?.length) {

          this.erroresValidacion.set(cuerpo.errores);

          if (cuerpo.advertencias?.length) {

            this.advertencias.set(cuerpo.advertencias);

          }

        } else {

          this.error.set(cuerpo?.mensaje ?? 'No se pudo registrar la solicitud');

        }

      }

    });

  }



  tieneContrapropuesta(sol: SolicitudVacacionesResumen): boolean {

    return sol.estado === 'CAMBIOS_SOLICITADOS' && !!sol.fechaInicioContrapropuesta;

  }



  aceptarContrapropuesta(solicitudVacacionesId: number): void {

    this.guardando.set(true);

    this.servicioVacaciones.aceptarContrapropuesta(solicitudVacacionesId).subscribe({

      next: () => {

        this.guardando.set(false);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo aceptar la contrapropuesta');

      }

    });

  }



  cancelarSolicitud(solicitudVacacionesId: number): void {

    this.guardando.set(true);

    this.servicioVacaciones.cancelar(solicitudVacacionesId).subscribe({

      next: () => {

        this.guardando.set(false);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo cancelar la solicitud');

      }

    });

  }



  puedeCancelar(estado: string): boolean {

    return estado === 'EN_REVISION' || estado === 'BORRADOR';

  }

  etiquetaEstado(estado: string): string {

    return formatearEstadoSolicitud(estado);

  }

}


