import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import {
  ComunicacionResumen,
  DestinatarioComunicacionResumen,
  ServicioComunicaciones
} from '../../../nucleo/servicios/servicio-comunicaciones';
import { BuscadorEmpleadoComponent } from '../../../compartido/componentes/buscador-empleado/buscador-empleado.component';
import {
  BuscadorOpcionComponent,
  OpcionBuscador
} from '../../../compartido/componentes/buscador-opcion/buscador-opcion.component';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import { ServicioOrganizacion } from '../../../nucleo/servicios/servicio-organizacion';
import {
  BotonComponent,
  CajonComponent,
  EstadoVacioComponent,
  InsigniaComponent
} from '../../../ui';

const TIPOS_DESTINATARIO = [
  { valor: 'EMPRESA', etiqueta: 'Toda la empresa' },
  { valor: 'AREA', etiqueta: 'Área' },
  { valor: 'SECTOR', etiqueta: 'Sector' },
  { valor: 'PUESTO', etiqueta: 'Puesto' },
  { valor: 'EMPLEADO', etiqueta: 'Empleado específico' }
] as const;

@Component({
  selector: 'app-pagina-comunicaciones',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    BuscadorEmpleadoComponent,
    BuscadorOpcionComponent,
    BotonComponent,
    CajonComponent,
    EstadoVacioComponent,
    InsigniaComponent
  ],
  templateUrl: './pagina-comunicaciones.component.html'
})
export class PaginaComunicacionesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly comunicaciones = signal<ComunicacionResumen[]>([]);
  readonly programadas = signal<ComunicacionResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly tiposDestinatario = TIPOS_DESTINATARIO;
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly opcionesAreas = signal<OpcionBuscador[]>([]);
  readonly opcionesSectores = signal<OpcionBuscador[]>([]);
  readonly opcionesPuestos = signal<OpcionBuscador[]>([]);
  readonly formularioPublicar;

  readonly tipoDestinatarioActual = signal<string>('EMPRESA');
  readonly cajonLecturasAbierto = signal(false);
  readonly comunicacionSeleccionada = signal<ComunicacionResumen | null>(null);
  readonly destinatarios = signal<DestinatarioComunicacionResumen[]>([]);
  readonly cargandoDestinatarios = signal(false);
  readonly errorDestinatarios = signal<string | null>(null);
  readonly filtroLecturas = signal<'todos' | 'leidas' | 'pendientes'>('todos');

  readonly destinatariosFiltrados = computed(() => {
    const filtro = this.filtroLecturas();
    const lista = this.destinatarios();
    if (filtro === 'leidas') {
      return lista.filter((d) => d.leida);
    }
    if (filtro === 'pendientes') {
      return lista.filter((d) => !d.leida);
    }
    return lista;
  });

  readonly totalLeidasDetalle = computed(() => this.destinatarios().filter((d) => d.leida).length);
  readonly totalPendientesDetalle = computed(() => this.destinatarios().filter((d) => !d.leida).length);

  readonly etiquetaReferencia = computed(() => {
    const mapa: Record<string, string> = {
      AREA: 'Área destino',
      SECTOR: 'Sector destino',
      PUESTO: 'Puesto destino',
      EMPLEADO: 'Empleado destino'
    };
    return mapa[this.tipoDestinatarioActual()] ?? 'Destinatario';
  });

  constructor(
    fb: FormBuilder,
    private readonly servicioComunicaciones: ServicioComunicaciones,
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly servicioOrganizacion: ServicioOrganizacion
  ) {
    this.formularioPublicar = fb.nonNullable.group({
      titulo: ['', Validators.required],
      mensaje: ['', Validators.required],
      tipoDestinatario: ['EMPRESA', Validators.required],
      referenciaDestinatarioId: [null as number | null],
      requiereConfirmacion: [true],
      modoPublicacion: ['ahora' as 'ahora' | 'programar'],
      fechaProgramada: ['']
    });
  }

  ngOnInit(): void {
    this.cargarCatalogos();
    this.recargar();

    this.formularioPublicar.get('tipoDestinatario')?.valueChanges.subscribe((tipo) => {
      this.tipoDestinatarioActual.set(tipo);
      this.formularioPublicar.patchValue({ referenciaDestinatarioId: null });
      this.actualizarValidacionReferencia(tipo);
    });
  }

  cargarCatalogos(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista),
      error: () => this.error.set('No se pudieron cargar los empleados')
    });

    forkJoin({
      areas: this.servicioOrganizacion.listarAreas(),
      sectores: this.servicioOrganizacion.listarSectores(),
      puestos: this.servicioOrganizacion.listarPuestos()
    }).subscribe({
      next: ({ areas, sectores, puestos }) => {
        const mapaAreas = new Map(areas.map((a) => [a.id, a.nombre]));
        this.opcionesAreas.set(
          areas.map((a) => ({
            id: a.id,
            etiqueta: a.nombre,
            detalle: a.gerenciaNombre
          }))
        );
        this.opcionesSectores.set(
          sectores.map((s) => ({
            id: s.id,
            etiqueta: s.nombre,
            detalle: s.areaId ? mapaAreas.get(s.areaId) ?? null : null
          }))
        );
        this.opcionesPuestos.set(
          puestos.map((p) => ({
            id: p.id,
            etiqueta: p.nombre,
            detalle: p.nivel ?? null
          }))
        );
      }
    });
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioComunicaciones.listar().subscribe({
      next: (lista) => this.comunicaciones.set(lista)
    });
    this.servicioComunicaciones.listarProgramadas().subscribe({
      next: (lista) => {
        this.programadas.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las comunicaciones');
        this.cargando.set(false);
      }
    });
  }

  alternarFormulario(): void {
    this.mostrarFormulario.update((v) => !v);
    if (!this.mostrarFormulario()) {
      this.formularioPublicar.reset({
        tipoDestinatario: 'EMPRESA',
        requiereConfirmacion: true,
        modoPublicacion: 'ahora',
        referenciaDestinatarioId: null
      });
      this.tipoDestinatarioActual.set('EMPRESA');
    }
  }

  publicar(): void {
    this.actualizarValidacionReferencia(this.formularioPublicar.get('tipoDestinatario')?.value ?? 'EMPRESA');

    if (this.formularioPublicar.invalid) {
      this.formularioPublicar.markAllAsTouched();
      if (this.requiereReferencia() && !this.formularioPublicar.get('referenciaDestinatarioId')?.value) {
        this.error.set(`Seleccioná ${this.etiquetaReferencia().toLowerCase()}`);
      }
      return;
    }

    const datos = this.formularioPublicar.getRawValue();

    if (datos.modoPublicacion === 'programar' && !datos.fechaProgramada) {
      this.error.set('Indicá fecha y hora de publicación');
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    this.servicioComunicaciones.publicar({
      titulo: datos.titulo,
      mensaje: datos.mensaje,
      tipoDestinatario: datos.tipoDestinatario,
      referenciaDestinatarioId: datos.tipoDestinatario === 'EMPRESA' ? null : datos.referenciaDestinatarioId,
      requiereConfirmacion: datos.requiereConfirmacion,
      fechaPublicacionProgramada: datos.modoPublicacion === 'programar'
        ? datos.fechaProgramada
        : null
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioPublicar.reset({
          tipoDestinatario: 'EMPRESA',
          requiereConfirmacion: true,
          modoPublicacion: 'ahora',
          referenciaDestinatarioId: null
        });
        this.tipoDestinatarioActual.set('EMPRESA');
        this.mostrarFormulario.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo publicar la comunicación');
      }
    });
  }

  requiereReferencia(): boolean {
    return this.tipoDestinatarioActual() !== 'EMPRESA';
  }

  esEmpleado(): boolean {
    return this.tipoDestinatarioActual() === 'EMPLEADO';
  }

  esArea(): boolean {
    return this.tipoDestinatarioActual() === 'AREA';
  }

  esSector(): boolean {
    return this.tipoDestinatarioActual() === 'SECTOR';
  }

  esPuesto(): boolean {
    return this.tipoDestinatarioActual() === 'PUESTO';
  }

  esProgramada(): boolean {
    return this.formularioPublicar.get('modoPublicacion')?.value === 'programar';
  }

  etiquetaTipoDestinatario(tipo: string): string {
    return TIPOS_DESTINATARIO.find((t) => t.valor === tipo)?.etiqueta ?? tipo;
  }

  cancelarProgramada(id: number): void {
    if (!confirm('¿Cancelar esta comunicación programada?')) return;
    this.guardando.set(true);
    this.servicioComunicaciones.cancelar(id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo cancelar la comunicación');
      }
    });
  }

  verLecturas(comunicacion: ComunicacionResumen): void {
    this.comunicacionSeleccionada.set(comunicacion);
    this.filtroLecturas.set('todos');
    this.errorDestinatarios.set(null);
    this.destinatarios.set([]);
    this.cajonLecturasAbierto.set(true);
    this.cargandoDestinatarios.set(true);

    this.servicioComunicaciones.listarDestinatarios(comunicacion.id).subscribe({
      next: (lista) => {
        const ordenada = [...lista].sort((a, b) => {
          if (a.leida !== b.leida) {
            return a.leida ? -1 : 1;
          }
          return (a.nombreEmpleado ?? '').localeCompare(b.nombreEmpleado ?? '', 'es');
        });
        this.destinatarios.set(ordenada);
        this.cargandoDestinatarios.set(false);
      },
      error: (err) => {
        this.cargandoDestinatarios.set(false);
        this.errorDestinatarios.set(
          err.error?.mensaje ?? 'No se pudo cargar quiénes vieron este aviso'
        );
      }
    });
  }

  cerrarLecturas(): void {
    this.cajonLecturasAbierto.set(false);
    this.comunicacionSeleccionada.set(null);
    this.destinatarios.set([]);
    this.errorDestinatarios.set(null);
  }

  private actualizarValidacionReferencia(tipo: string): void {
    const control = this.formularioPublicar.get('referenciaDestinatarioId');
    if (!control) {
      return;
    }
    if (tipo === 'EMPRESA') {
      control.clearValidators();
    } else {
      control.setValidators(Validators.required);
    }
    control.updateValueAndValidity();
  }
}
