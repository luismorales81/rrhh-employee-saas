import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AusenciaResumen,
  ServicioAusentismo,
  TipoAusenciaResumen
} from '../../../nucleo/servicios/servicio-ausentismo';
import { SelectorArchivoComponent } from '../../../compartido/componentes/selector-archivo/selector-archivo.component';
import { BuscadorEmpleadoComponent } from '../../../compartido/componentes/buscador-empleado/buscador-empleado.component';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';

@Component({
  selector: 'app-pagina-ausentismo',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, DatePipe, SelectorArchivoComponent, BuscadorEmpleadoComponent],
  templateUrl: './pagina-ausentismo.component.html'
})
export class PaginaAusentismoComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly ausencias = signal<AusenciaResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly tipos = signal<TipoAusenciaResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly mostrarModalJustificar = signal(false);
  readonly ausenciaJustificando = signal<AusenciaResumen | null>(null);
  readonly filtroListado = signal('');
  readonly formularioRegistrar;
  readonly formularioJustificar;

  readonly ausenciasFiltradas = computed(() => {
    const texto = this.filtroListado().trim().toLowerCase();
    if (!texto) {
      return this.ausencias();
    }
    return this.ausencias().filter((a) => {
      const nombre = a.empleadoNombre.toLowerCase();
      const tipo = a.tipoAusenciaNombre.toLowerCase();
      const motivo = (a.motivo ?? '').toLowerCase();
      return nombre.includes(texto) || tipo.includes(texto) || motivo.includes(texto);
    });
  });
  constructor(
    fb: FormBuilder,
    private readonly servicioAusentismo: ServicioAusentismo,
    private readonly servicioEmpleados: ServicioEmpleados
  ) {
    this.formularioRegistrar = fb.nonNullable.group({
      empleadoId: [null as number | null, Validators.required],
      tipoAusenciaId: ['', Validators.required],
      fecha: ['', Validators.required],
      motivo: [''],
      justificada: [false],
      urlCertificado: ['']
    });
    this.formularioJustificar = fb.nonNullable.group({
      motivo: [''],
      justificada: [true],
      urlCertificado: ['']
    });
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
    this.servicioAusentismo.listarTipos().subscribe({
      next: (lista) => this.tipos.set(lista)
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioAusentismo.listarAusencias().subscribe({
      next: (lista) => {
        this.ausencias.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las ausencias');
        this.cargando.set(false);
      }
    });
  }

  alternarFormulario(): void {
    this.mostrarFormulario.update((v) => !v);
    if (!this.mostrarFormulario()) {
      this.formularioRegistrar.reset({ justificada: false, empleadoId: null });
    }
  }

  establecerUrlCertificado(url: string): void {
    this.formularioRegistrar.patchValue({ urlCertificado: url });
  }

  establecerUrlCertificadoJustificar(url: string): void {
    this.formularioJustificar.patchValue({ urlCertificado: url });
  }

  abrirJustificar(ausencia: AusenciaResumen): void {
    this.ausenciaJustificando.set(ausencia);
    this.formularioJustificar.patchValue({
      motivo: ausencia.motivo ?? '',
      justificada: true,
      urlCertificado: ''
    });
    this.mostrarModalJustificar.set(true);
  }

  cerrarJustificar(): void {
    this.mostrarModalJustificar.set(false);
    this.ausenciaJustificando.set(null);
    this.formularioJustificar.reset({ justificada: true });
  }

  guardarJustificacion(): void {
    const ausencia = this.ausenciaJustificando();
    if (!ausencia) return;

    const datos = this.formularioJustificar.getRawValue();
    this.guardando.set(true);
    this.servicioAusentismo.actualizar(ausencia.id, {
      motivo: datos.motivo || undefined,
      justificada: datos.justificada,
      urlCertificado: datos.urlCertificado || undefined
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarJustificar();
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo actualizar la ausencia');
      }
    });
  }

  registrarAusencia(): void {
    if (this.formularioRegistrar.invalid) {
      this.formularioRegistrar.markAllAsTouched();
      return;
    }

    const datos = this.formularioRegistrar.getRawValue();
    if (datos.empleadoId == null) {
      this.formularioRegistrar.get('empleadoId')?.markAsTouched();
      return;
    }

    this.guardando.set(true);
    this.servicioAusentismo.registrar({
      empleadoId: datos.empleadoId,
      tipoAusenciaId: Number(datos.tipoAusenciaId),
      fecha: datos.fecha,
      motivo: datos.motivo || undefined,
      justificada: datos.justificada,
      urlCertificado: datos.urlCertificado || undefined
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioRegistrar.reset({ justificada: false, empleadoId: null });
        this.mostrarFormulario.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo registrar la ausencia');
      }
    });
  }
}
