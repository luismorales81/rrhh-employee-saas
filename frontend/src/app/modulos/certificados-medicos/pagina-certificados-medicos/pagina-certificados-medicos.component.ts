import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CertificadoCompleto,
  CertificadoResumen,
  ESTADOS_PENDIENTES_REVISION,
  ETIQUETAS_ESTADO_CERTIFICADO,
  ETIQUETAS_TIPO_EXAMEN,
  EstadoCertificado,
  OrigenCargaCertificado,
  ServicioCertificados,
  SolicitudCargarCertificado,
  TipoExamenCertificado
} from '../../../nucleo/servicios/servicio-certificados';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { BuscadorEmpleadoComponent } from '../../../compartido/componentes/buscador-empleado/buscador-empleado.component';
import { SelectorArchivoComponent } from '../../../compartido/componentes/selector-archivo/selector-archivo.component';
import { VisorDocumentoComponent } from '../../../compartido/componentes/visor-documento/visor-documento.component';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import { ArchivoSubidoResumen } from '../../../nucleo/servicios/servicio-archivos';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';

type VistaActiva = 'bandeja' | 'empleado' | 'reporte';

@Component({
  selector: 'app-pagina-certificados-medicos',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    DatePipe,
    RouterLink,
    BuscadorEmpleadoComponent,
    SelectorArchivoComponent,
    VisorDocumentoComponent,
    DirectivaTienePermiso
  ],
  templateUrl: './pagina-certificados-medicos.component.html'
})
export class PaginaCertificadosMedicosComponent implements OnInit {
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly vistaActiva = signal<VistaActiva>('bandeja');
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly empleadoSeleccionadoId = signal<number | null>(null);
  readonly certificados = signal<CertificadoResumen[]>([]);
  readonly bandejaCertificados = signal<CertificadoResumen[]>([]);
  readonly filtroTexto = signal('');
  readonly filtroTextoBandeja = signal('');
  readonly filtroEstado = signal<EstadoCertificado | ''>('');
  readonly soloPendientes = signal(true);
  readonly incluirRequiereAclaracionBandeja = signal(false);
  readonly mostrarFormularioCarga = signal(false);
  readonly certificadoDetalle = signal<CertificadoCompleto | CertificadoResumen | null>(null);
  readonly mostrarModalDetalle = signal(false);
  readonly mostrarModalValidar = signal(false);
  readonly mostrarModalRechazar = signal(false);
  readonly reporteCargando = signal(false);
  readonly reporte = signal<import('../../../nucleo/servicios/servicio-certificados').RespuestaReporteAusentismo | null>(null);

  readonly etiquetasEstado = ETIQUETAS_ESTADO_CERTIFICADO;
  readonly etiquetasTipo = ETIQUETAS_TIPO_EXAMEN;
  readonly estadosFiltro = Object.keys(ETIQUETAS_ESTADO_CERTIFICADO) as EstadoCertificado[];
  readonly tiposExamen = Object.keys(ETIQUETAS_TIPO_EXAMEN) as TipoExamenCertificado[];

  readonly certificadosFiltrados = computed(() => {
    let lista = this.certificados();
    if (this.soloPendientes()) {
      lista = lista.filter((c) => ESTADOS_PENDIENTES_REVISION.includes(c.estado));
    }
    const estado = this.filtroEstado();
    if (estado) {
      lista = lista.filter((c) => c.estado === estado);
    }
    const texto = this.filtroTexto().trim().toLowerCase();
    if (texto) {
      lista = lista.filter((c) => {
        const tipo = ETIQUETAS_TIPO_EXAMEN[c.tipoExamen].toLowerCase();
        const est = ETIQUETAS_ESTADO_CERTIFICADO[c.estado].toLowerCase();
        return tipo.includes(texto) || est.includes(texto) || String(c.id).includes(texto);
      });
    }
    return lista;
  });

  readonly bandejaFiltrada = computed(() => {
    const texto = this.filtroTextoBandeja().trim().toLowerCase();
    const empleados = this.empleados();
    const lista = this.bandejaCertificados();
    if (!texto) {
      return lista;
    }
    return lista.filter((c) => {
      const emp = empleados.find((e) => e.id === c.empleadoId);
      const nombreEmpleado = emp ? `${emp.nombre} ${emp.apellido}`.toLowerCase() : '';
      const tipo = ETIQUETAS_TIPO_EXAMEN[c.tipoExamen].toLowerCase();
      const est = ETIQUETAS_ESTADO_CERTIFICADO[c.estado].toLowerCase();
      return nombreEmpleado.includes(texto) || tipo.includes(texto) || est.includes(texto)
        || String(c.id).includes(texto);
    });
  });

  readonly formularioCarga;
  readonly formularioValidar;
  readonly formularioRechazar;
  readonly formularioReporte;

  private archivoCargadoId: number | null = null;

  constructor(
    fb: FormBuilder,
    private readonly servicioCertificados: ServicioCertificados,
    private readonly servicioEmpleados: ServicioEmpleados,
    readonly servicioPermisos: ServicioPermisos
  ) {
    this.formularioCarga = fb.nonNullable.group({
      empleadoId: [null as number | null, Validators.required],
      tipoExamen: ['AUSENTISMO' as TipoExamenCertificado, Validators.required],
      origenCarga: ['MEDICO_LABORAL' as OrigenCargaCertificado, Validators.required]
    });
    this.formularioValidar = fb.nonNullable.group({
      diagnostico: [''],
      reposoDias: [null as number | null],
      medicoNombre: [''],
      medicoMatricula: [''],
      fechaEmision: [''],
      corregidoManualmente: [false],
      observaciones: ['']
    });
    this.formularioRechazar = fb.nonNullable.group({
      observaciones: ['', Validators.required]
    });
    const hoy = new Date();
    const hace30 = new Date(hoy);
    hace30.setDate(hoy.getDate() - 30);
    this.formularioReporte = fb.nonNullable.group({
      desde: [hace30.toISOString().slice(0, 10), Validators.required],
      hasta: [hoy.toISOString().slice(0, 10), Validators.required]
    });
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
    if (this.puedeVerBandeja()) {
      this.vistaActiva.set('bandeja');
      this.cargarBandeja();
    } else {
      this.vistaActiva.set('empleado');
    }
  }

  puedeVerBandeja(): boolean {
    return this.servicioPermisos.tieneAlgunPermiso([
      'certificado.validar',
      'certificado.ver_completo',
      'certificado.ver_resumen'
    ]);
  }

  cambiarVista(vista: VistaActiva): void {
    this.vistaActiva.set(vista);
    this.error.set(null);
    if (vista === 'bandeja') {
      this.cargarBandeja();
    } else if (vista === 'reporte' && !this.reporte()) {
      this.generarReporte();
    }
  }

  cargarBandeja(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioCertificados.listarBandejaRevision(this.incluirRequiereAclaracionBandeja()).subscribe({
      next: (lista) => {
        this.bandejaCertificados.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar la bandeja de revisión');
        this.cargando.set(false);
      }
    });
  }

  alCambiarIncluirAclaracion(incluir: boolean): void {
    this.incluirRequiereAclaracionBandeja.set(incluir);
    this.cargarBandeja();
  }

  alSeleccionarEmpleado(empleadoId: number | null): void {
    this.empleadoSeleccionadoId.set(empleadoId);
    if (empleadoId) {
      this.cargarCertificadosEmpleado(empleadoId);
    } else {
      this.certificados.set([]);
    }
  }

  cargarCertificadosEmpleado(empleadoId: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioCertificados.listarPorEmpleado(empleadoId).subscribe({
      next: (lista) => {
        this.certificados.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'No se pudieron cargar los certificados');
        this.cargando.set(false);
      }
    });
  }

  alternarFormularioCarga(): void {
    this.mostrarFormularioCarga.update((v) => !v);
    if (!this.mostrarFormularioCarga()) {
      this.archivoCargadoId = null;
      this.formularioCarga.reset({
        tipoExamen: 'AUSENTISMO',
        origenCarga: 'MEDICO_LABORAL',
        empleadoId: this.empleadoSeleccionadoId()
      });
    }
  }

  alSubirArchivo(archivo: ArchivoSubidoResumen): void {
    this.archivoCargadoId = archivo.id;
  }

  enviarCarga(): void {
    if (this.formularioCarga.invalid || !this.archivoCargadoId) {
      this.error.set('Seleccioná empleado, tipo de examen y archivo.');
      return;
    }
    const valores = this.formularioCarga.getRawValue();
    const solicitud: SolicitudCargarCertificado = {
      archivoId: this.archivoCargadoId,
      tipoExamen: valores.tipoExamen,
      origenCarga: valores.origenCarga,
      empleadoId: valores.empleadoId ?? undefined
    };
    this.guardando.set(true);
    this.error.set(null);
    this.servicioCertificados.cargar(solicitud).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mostrarFormularioCarga.set(false);
        this.archivoCargadoId = null;
        if (valores.empleadoId) {
          this.empleadoSeleccionadoId.set(valores.empleadoId);
          this.cargarCertificadosEmpleado(valores.empleadoId);
        }
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar el certificado');
      }
    });
  }

  abrirDetalle(certificado: CertificadoResumen): void {
    this.servicioCertificados.obtener(certificado.id).subscribe({
      next: (detalle) => {
        this.certificadoDetalle.set(detalle);
        this.mostrarModalDetalle.set(true);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar el detalle');
      }
    });
  }

  cerrarDetalle(): void {
    this.mostrarModalDetalle.set(false);
    this.certificadoDetalle.set(null);
  }

  abrirValidar(): void {
    const detalle = this.certificadoDetalle();
    if (!detalle || !this.servicioCertificados.esCompleto(detalle)) {
      return;
    }
    this.formularioValidar.patchValue({
      diagnostico: detalle.diagnostico ?? '',
      reposoDias: detalle.reposoDias,
      medicoNombre: detalle.medicoNombre ?? '',
      medicoMatricula: detalle.medicoMatricula ?? '',
      fechaEmision: detalle.fechaEmision ?? '',
      corregidoManualmente: false,
      observaciones: ''
    });
    this.mostrarModalValidar.set(true);
  }

  cerrarValidar(): void {
    this.mostrarModalValidar.set(false);
  }

  abrirRechazar(): void {
    this.formularioRechazar.reset({ observaciones: '' });
    this.mostrarModalRechazar.set(true);
  }

  cerrarRechazar(): void {
    this.mostrarModalRechazar.set(false);
  }

  confirmarValidar(): void {
    const id = this.certificadoDetalle()?.id;
    if (!id) return;
    const valores = this.formularioValidar.getRawValue();
    this.guardando.set(true);
    this.servicioCertificados.validar(id, {
      diagnostico: valores.diagnostico || undefined,
      reposoDias: valores.reposoDias ?? undefined,
      medicoNombre: valores.medicoNombre || undefined,
      medicoMatricula: valores.medicoMatricula || undefined,
      fechaEmision: valores.fechaEmision || undefined,
      corregidoManualmente: valores.corregidoManualmente,
      observaciones: valores.observaciones || undefined
    }).subscribe({
      next: (actualizado) => {
        this.guardando.set(false);
        this.mostrarModalValidar.set(false);
        this.certificadoDetalle.set(actualizado);
        this.recargarLista();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo validar el certificado');
      }
    });
  }

  confirmarRechazar(): void {
    const id = this.certificadoDetalle()?.id;
    if (!id || this.formularioRechazar.invalid) return;
    this.guardando.set(true);
    this.servicioCertificados.rechazar(id, this.formularioRechazar.getRawValue()).subscribe({
      next: (actualizado) => {
        this.guardando.set(false);
        this.mostrarModalRechazar.set(false);
        this.certificadoDetalle.set(actualizado);
        this.recargarLista();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo rechazar el certificado');
      }
    });
  }

  reprocesar(): void {
    const id = this.certificadoDetalle()?.id;
    if (!id) return;
    this.guardando.set(true);
    this.servicioCertificados.reprocesar(id).subscribe({
      next: (actualizado) => {
        this.guardando.set(false);
        this.certificadoDetalle.set(actualizado);
        this.recargarLista();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo reprocesar el certificado');
      }
    });
  }

  generarReporte(): void {
    if (this.formularioReporte.invalid) return;
    const { desde, hasta } = this.formularioReporte.getRawValue();
    this.reporteCargando.set(true);
    this.servicioCertificados.reporteAusentismo(desde, hasta).subscribe({
      next: (respuesta) => {
        this.reporte.set(respuesta);
        this.reporteCargando.set(false);
      },
      error: (err) => {
        this.reporteCargando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo generar el reporte');
      }
    });
  }

  puedeValidar(certificado: CertificadoResumen | CertificadoCompleto): boolean {
    return this.servicioPermisos.tienePermiso('certificado.validar')
      && (certificado.estado === 'EN_REVISION' || certificado.estado === 'REQUIERE_ACLARACION');
  }

  puedeReprocesar(): boolean {
    return this.servicioPermisos.tieneAlgunPermiso(['certificado.validar', 'certificado.configurar']);
  }

  puedeVerDiagnostico(certificado: CertificadoResumen | CertificadoCompleto): boolean {
    return this.servicioCertificados.esCompleto(certificado);
  }

  comoCompleto(certificado: CertificadoResumen | CertificadoCompleto): CertificadoCompleto | null {
    return this.servicioCertificados.esCompleto(certificado) ? certificado : null;
  }

  nombreEmpleado(empleadoId: number): string {
    const empleado = this.empleados().find((e) => e.id === empleadoId);
    if (!empleado) {
      return `Empleado #${empleadoId}`;
    }
    return `${empleado.nombre} ${empleado.apellido}`;
  }

  claseEstado(estado: EstadoCertificado): string {
    if (estado === 'VALIDADO') return 'firmado';
    if (estado === 'RECHAZADO' || estado === 'REQUIERE_ACLARACION') return 'disconformidad';
    if (estado === 'EN_REVISION' || estado === 'EN_PROCESAMIENTO') return 'pendiente';
    return 'visto';
  }

  private recargarLista(): void {
    if (this.vistaActiva() === 'bandeja') {
      this.cargarBandeja();
      return;
    }
    const empleadoId = this.empleadoSeleccionadoId();
    if (empleadoId) {
      this.cargarCertificadosEmpleado(empleadoId);
    }
  }
}
