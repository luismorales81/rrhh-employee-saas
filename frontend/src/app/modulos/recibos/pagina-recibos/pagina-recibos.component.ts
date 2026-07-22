import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {
  EventoFirmaReciboResumen,
  FilaAnalisisCargaMasiva,
  ReciboResumen,
  RespuestaAlertasRecibos,
  RespuestaAnalisisCargaMasiva,
  RespuestaCargaMasivaRecibos,
  ServicioRecibos
} from '../../../nucleo/servicios/servicio-recibos';
import { ArchivoSubidoResumen } from '../../../nucleo/servicios/servicio-archivos';
import { SelectorArchivoComponent } from '../../../compartido/componentes/selector-archivo/selector-archivo.component';
import { WidgetAlertasRecibosComponent } from '../../../compartido/componentes/widget-alertas-recibos/widget-alertas-recibos.component';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import { BotonComponent, CajonComponent, InsigniaComponent, MigasComponent, etiquetaEstado as etiquetaEstadoUi, varianteEstado } from '../../../ui';

type ModoVista = 'listado' | 'masiva';

@Component({
  selector: 'app-pagina-recibos',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    SelectorArchivoComponent,
    WidgetAlertasRecibosComponent,
    BotonComponent,
    CajonComponent,
    InsigniaComponent,
    MigasComponent
  ],
  templateUrl: './pagina-recibos.component.html'
})
export class PaginaRecibosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly recibos = signal<ReciboResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly modoVista = signal<ModoVista>('listado');
  readonly cajonCargaAbierto = signal(false);
  readonly reciboEventos = signal<ReciboResumen | null>(null);
  readonly eventosFirma = signal<EventoFirmaReciboResumen[]>([]);
  readonly cargandoEventos = signal(false);
  readonly hashArchivoSubido = signal<string | null>(null);
  readonly archivoZipSeleccionado = signal<File | null>(null);
  readonly analisisMasivo = signal<RespuestaAnalisisCargaMasiva | null>(null);
  readonly resultadoMasivo = signal<RespuestaCargaMasivaRecibos | null>(null);
  readonly pasoMasivo = signal<1 | 2 | 3>(1);
  readonly alertas = signal<RespuestaAlertasRecibos | null>(null);
  readonly filtroEstado = signal<string>('');
  readonly filtroPeriodo = signal<string>('');
  readonly filtroSoloAntiguos = signal(false);
  readonly reciboRepublicar = signal<ReciboResumen | null>(null);
  readonly archivoRepublicarId = signal(0);
  readonly motivoRepublicacion = signal('');
  readonly formularioCargar;
  readonly formularioMasivo;
  readonly migas = [
    { etiqueta: 'Documentos', ruta: '/recibos' },
    { etiqueta: 'Recibos' }
  ];

  constructor(
    fb: FormBuilder,
    private readonly servicioRecibos: ServicioRecibos,
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly ruta: ActivatedRoute
  ) {
    this.formularioCargar = fb.nonNullable.group({
      empleadoId: [0, Validators.required],
      periodo: ['', Validators.required],
      titulo: ['', Validators.required],
      archivoId: [0, Validators.required],
      montoNeto: [''],
      montoBruto: ['']
    });

    this.formularioMasivo = fb.nonNullable.group({
      periodo: ['', Validators.required],
      tituloBase: ['Recibo de sueldo', Validators.required],
      montoNeto: [''],
      montoBruto: ['']
    });
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista),
      error: () => this.error.set('No se pudieron cargar los empleados')
    });
    this.recargar();
    this.cargarAlertas();
    this.ruta.queryParamMap.subscribe((params) => {
      const estado = params.get('estado');
      if (estado) {
        this.filtroEstado.set(estado);
      }
      const periodo = params.get('periodo');
      if (periodo) {
        this.filtroPeriodo.set(periodo);
      }
      if (params.get('antiguos') === '1') {
        this.filtroSoloAntiguos.set(true);
      }
    });
  }

  cargarAlertas(): void {
    this.servicioRecibos.obtenerAlertasSeguimiento().subscribe({
      next: (datos) => this.alertas.set(datos),
      error: () => this.alertas.set(null)
    });
  }

  recibosFiltrados(): ReciboResumen[] {
    const estado = this.filtroEstado();
    const periodo = this.filtroPeriodo();
    const soloAntiguos = this.filtroSoloAntiguos();
    const dias = this.alertas()?.diasUmbralAntiguedad ?? 7;
    const umbral = Date.now() - dias * 24 * 60 * 60 * 1000;
    return this.recibos().filter((r) => {
      if (estado) {
        if (estado === 'PENDIENTE') {
          if (r.estado !== 'PENDIENTE' && r.estado !== 'VISTO') {
            return false;
          }
        } else if (r.estado !== estado) {
          return false;
        }
      }
      if (periodo && r.periodo !== periodo) {
        return false;
      }
      if (soloAntiguos) {
        const pendiente = r.estado === 'PENDIENTE' || r.estado === 'VISTO';
        const publicacion = new Date(r.fechaPublicacion).getTime();
        if (!pendiente || publicacion > umbral) {
          return false;
        }
      }
      return true;
    });
  }

  puedeRepublicar(recibo: ReciboResumen): boolean {
    return recibo.vigente && (
      recibo.estado === 'PENDIENTE' ||
      recibo.estado === 'VISTO' ||
      recibo.estado === 'FIRMADO' ||
      recibo.estado === 'DISCONFORMIDAD'
    );
  }

  abrirRepublicar(recibo: ReciboResumen): void {
    this.reciboRepublicar.set(recibo);
    this.archivoRepublicarId.set(0);
    this.motivoRepublicacion.set('');
    this.error.set(null);
  }

  cerrarRepublicar(): void {
    this.reciboRepublicar.set(null);
  }

  establecerArchivoRepublicar(archivo: ArchivoSubidoResumen): void {
    this.archivoRepublicarId.set(archivo.id);
  }

  confirmarRepublicar(): void {
    const recibo = this.reciboRepublicar();
    if (!recibo || this.archivoRepublicarId() === 0) {
      this.error.set('Seleccioná el PDF corregido.');
      return;
    }
    if (this.motivoRepublicacion().trim().length < 10) {
      this.error.set('El motivo debe tener al menos 10 caracteres.');
      return;
    }
    this.guardando.set(true);
    this.servicioRecibos.republicar(recibo.id, {
      archivoId: this.archivoRepublicarId(),
      motivo: this.motivoRepublicacion().trim()
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarRepublicar();
        this.recargar();
        this.cargarAlertas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo republicar el recibo');
      }
    });
  }

  reenviarNotificacion(recibo: ReciboResumen): void {
    this.servicioRecibos.reenviarNotificacion(recibo.id).subscribe({
      next: () => this.recargar(),
      error: () => this.error.set('No se pudo reenviar la notificación')
    });
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioRecibos.listar().subscribe({
      next: (lista) => {
        this.recibos.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los recibos');
        this.cargando.set(false);
      }
    });
  }

  mostrarCargaIndividual(): void {
    this.cajonCargaAbierto.set(true);
    this.error.set(null);
  }

  cerrarCajonCarga(): void {
    this.cajonCargaAbierto.set(false);
    this.formularioCargar.reset({ empleadoId: 0, archivoId: 0 });
    this.hashArchivoSubido.set(null);
  }

  mostrarCargaMasiva(): void {
    this.modoVista.set('masiva');
    this.pasoMasivo.set(1);
    this.analisisMasivo.set(null);
    this.resultadoMasivo.set(null);
    this.archivoZipSeleccionado.set(null);
    this.error.set(null);
  }

  volverAlListado(): void {
    this.modoVista.set('listado');
    this.error.set(null);
  }

  establecerArchivo(archivo: ArchivoSubidoResumen): void {
    this.formularioCargar.patchValue({ archivoId: archivo.id });
    this.hashArchivoSubido.set(archivo.hashSha256);
  }

  seleccionarZip(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;
    this.archivoZipSeleccionado.set(archivo);
    this.analisisMasivo.set(null);
    this.pasoMasivo.set(1);
  }

  analizarCargaMasiva(): void {
    if (this.formularioMasivo.invalid || !this.archivoZipSeleccionado()) {
      this.formularioMasivo.markAllAsTouched();
      return;
    }
    const periodo = this.formularioMasivo.getRawValue().periodo;
    this.guardando.set(true);
    this.error.set(null);
    this.servicioRecibos.analizarCargaMasiva(this.archivoZipSeleccionado()!, periodo).subscribe({
      next: (analisis) => {
        this.analisisMasivo.set(analisis);
        this.pasoMasivo.set(2);
        this.guardando.set(false);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo analizar el ZIP');
      }
    });
  }

  confirmarCargaMasiva(): void {
    if (!this.archivoZipSeleccionado() || this.formularioMasivo.invalid) {
      return;
    }
    const datos = this.formularioMasivo.getRawValue();
    this.guardando.set(true);
    this.error.set(null);
    this.servicioRecibos.ejecutarCargaMasiva(
      this.archivoZipSeleccionado()!,
      datos.periodo,
      datos.tituloBase,
      datos.montoNeto ? Number(datos.montoNeto) : null,
      datos.montoBruto ? Number(datos.montoBruto) : null
    ).subscribe({
      next: (resultado) => {
        this.resultadoMasivo.set(resultado);
        this.pasoMasivo.set(3);
        this.guardando.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo ejecutar la carga masiva');
      }
    });
  }

  cargarRecibo(): void {
    if (this.formularioCargar.invalid || this.formularioCargar.getRawValue().empleadoId === 0) {
      this.formularioCargar.markAllAsTouched();
      return;
    }

    const datos = this.formularioCargar.getRawValue();
    this.guardando.set(true);
    this.error.set(null);

    this.servicioRecibos.cargar({
      empleadoId: Number(datos.empleadoId),
      periodo: datos.periodo,
      titulo: datos.titulo,
      archivoId: Number(datos.archivoId),
      montoNeto: datos.montoNeto ? Number(datos.montoNeto) : null,
      montoBruto: datos.montoBruto ? Number(datos.montoBruto) : null
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioCargar.reset({ empleadoId: 0, archivoId: 0 });
        this.hashArchivoSubido.set(null);
        this.cajonCargaAbierto.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar el recibo');
      }
    });
  }

  exportarExpediente(recibo: ReciboResumen): void {
    this.servicioRecibos.exportarExpediente(recibo.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `expediente-${recibo.periodo}-${recibo.nombreEmpleado.replace(/\s+/g, '-')}.zip`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.error.set('No se pudo exportar el expediente')
    });
  }

  verEventos(recibo: ReciboResumen): void {
    this.reciboEventos.set(recibo);
    this.cargandoEventos.set(true);
    this.servicioRecibos.listarEventosFirma(recibo.id).subscribe({
      next: (eventos) => {
        this.eventosFirma.set(eventos);
        this.cargandoEventos.set(false);
      },
      error: () => {
        this.eventosFirma.set([]);
        this.cargandoEventos.set(false);
      }
    });
  }

  cerrarEventos(): void {
    this.reciboEventos.set(null);
    this.eventosFirma.set([]);
  }

  etiquetaEstadoAnalisis(fila: FilaAnalisisCargaMasiva): string {
    if (fila.estado === 'LISTO') {
      return 'Listo';
    }
    if (fila.estado === 'OMITIDO') {
      return 'Omitido';
    }
    return 'Error';
  }

  etiquetaEstado(estado: string): string {
    return etiquetaEstadoUi(estado);
  }

  varianteEstadoRecibo(estado: string) {
    return varianteEstado(estado);
  }

  claseEstadoRecibo(estado: string): string {
    const mapa: Record<string, string> = {
      PENDIENTE: 'pendiente',
      VISTO: 'visto',
      FIRMADO: 'firmado',
      DISCONFORMIDAD: 'disconformidad'
    };
    return mapa[estado] ?? '';
  }
}
