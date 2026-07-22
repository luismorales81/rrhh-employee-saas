import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import {
  EmpleadoResumen,
  ServicioEmpleados
} from '../../../nucleo/servicios/servicio-empleados';
import {
  AreaResumen,
  GerenciaResumen,
  PuestoResumen,
  SectorResumen,
  ServicioOrganizacion
} from '../../../nucleo/servicios/servicio-organizacion';
import {
  BotonComponent,
  CajonComponent,
  EstadoErrorComponent,
  EstadoVacioComponent,
  InsigniaComponent,
  MigasComponent,
  etiquetaEstado,
  varianteEstado
} from '../../../ui';

@Component({
  selector: 'app-pagina-empleados',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DirectivaTienePermiso,
    BotonComponent,
    CajonComponent,
    EstadoErrorComponent,
    EstadoVacioComponent,
    InsigniaComponent,
    MigasComponent
  ],
  templateUrl: './pagina-empleados.component.html'
})
export class PaginaEmpleadosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly gerencias = signal<GerenciaResumen[]>([]);
  readonly areas = signal<AreaResumen[]>([]);
  readonly sectores = signal<SectorResumen[]>([]);
  readonly puestos = signal<PuestoResumen[]>([]);
  readonly filtro = signal('');
  readonly mostrarFormulario = signal(false);
  readonly empleadoEditandoId = signal<number | null>(null);
  readonly formularioEmpleado;

  constructor(
    fb: FormBuilder,
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly servicioOrganizacion: ServicioOrganizacion
  ) {
    this.formularioEmpleado = fb.nonNullable.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      dni: [''],
      numeroEmpleado: [''],
      fechaIngreso: [''],
      emailLaboral: ['', Validators.email],
      gerenciaId: [''],
      areaId: [''],
      sectorId: [''],
      puestoId: [''],
      supervisorId: ['']
    });
  }

  ngOnInit(): void {
    this.servicioOrganizacion.listarGerencias().subscribe({
      next: (lista) => this.gerencias.set(lista)
    });
    this.servicioOrganizacion.listarAreas().subscribe({
      next: (lista) => this.areas.set(lista)
    });
    this.servicioOrganizacion.listarSectores().subscribe({
      next: (lista) => this.sectores.set(lista)
    });
    this.servicioOrganizacion.listarPuestos().subscribe({
      next: (lista) => this.puestos.set(lista)
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => {
        this.empleados.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los empleados');
        this.cargando.set(false);
      }
    });
  }

  areasFiltradas(): AreaResumen[] {
    const gerenciaId = this.formularioEmpleado.get('gerenciaId')?.value;
    if (!gerenciaId) return this.areas();
    return this.areas().filter((a) => String(a.gerenciaId) === String(gerenciaId));
  }

  sectoresFiltrados(): SectorResumen[] {
    const areaId = this.formularioEmpleado.get('areaId')?.value;
    if (!areaId) return this.sectores();
    return this.sectores().filter((s) => s.areaId != null && String(s.areaId) === String(areaId));
  }

  supervisoresDisponibles(): EmpleadoResumen[] {
    const idEditando = this.empleadoEditandoId();
    return this.empleados().filter((e) => e.estado === 'ACTIVO' && e.id !== idEditando);
  }

  empleadosFiltrados(): EmpleadoResumen[] {
    const texto = this.filtro().toLowerCase();
    if (!texto) return this.empleados();
    return this.empleados().filter(
      (e) =>
        e.nombre.toLowerCase().includes(texto) ||
        e.apellido.toLowerCase().includes(texto) ||
        (e.numeroEmpleado?.toLowerCase().includes(texto) ?? false) ||
        (e.dni?.toLowerCase().includes(texto) ?? false)
    );
  }

  actualizarFiltro(evento: Event): void {
    this.filtro.set((evento.target as HTMLInputElement).value);
  }

  alternarFormulario(): void {
    if (this.mostrarFormulario()) {
      this.cancelarFormulario();
    } else {
      this.empleadoEditandoId.set(null);
      this.formularioEmpleado.reset();
      this.mostrarFormulario.set(true);
    }
  }

  editarEmpleado(empleado: EmpleadoResumen): void {
    this.empleadoEditandoId.set(empleado.id);
    this.mostrarFormulario.set(true);
    this.servicioEmpleados.obtener(empleado.id).subscribe({
      next: (detalle) => {
        this.formularioEmpleado.patchValue({
          nombre: detalle.nombre,
          apellido: detalle.apellido,
          dni: detalle.dni ?? '',
          numeroEmpleado: detalle.numeroEmpleado ?? '',
          fechaIngreso: detalle.fechaIngreso ?? '',
          emailLaboral: detalle.emailLaboral ?? '',
          gerenciaId: detalle.gerenciaId != null ? String(detalle.gerenciaId) : '',
          areaId: detalle.areaId != null ? String(detalle.areaId) : '',
          sectorId: detalle.sectorId != null ? String(detalle.sectorId) : '',
          puestoId: detalle.puestoId != null ? String(detalle.puestoId) : '',
          supervisorId: detalle.supervisorId != null ? String(detalle.supervisorId) : ''
        });
      },
      error: () => {
        this.error.set('No se pudo cargar el empleado para editar');
        this.cancelarFormulario();
      }
    });
  }

  cancelarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.empleadoEditandoId.set(null);
    this.formularioEmpleado.reset();
  }

  guardarEmpleado(): void {
    if (this.formularioEmpleado.invalid) {
      this.formularioEmpleado.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const datos = this.formularioEmpleado.getRawValue();
    const idEditando = this.empleadoEditandoId();
    const solicitud = {
      nombre: datos.nombre,
      apellido: datos.apellido,
      dni: datos.dni || undefined,
      numeroEmpleado: datos.numeroEmpleado || undefined,
      fechaIngreso: datos.fechaIngreso || undefined,
      emailLaboral: datos.emailLaboral || undefined,
      gerenciaId: datos.gerenciaId ? Number(datos.gerenciaId) : undefined,
      areaId: datos.areaId ? Number(datos.areaId) : undefined,
      sectorId: datos.sectorId ? Number(datos.sectorId) : undefined,
      puestoId: datos.puestoId ? Number(datos.puestoId) : undefined,
      supervisorId: datos.supervisorId ? Number(datos.supervisorId) : undefined
    };

    const operacion = idEditando
      ? this.servicioEmpleados.actualizar(idEditando, solicitud)
      : this.servicioEmpleados.crear(solicitud);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelarFormulario();
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el empleado');
      }
    });
  }

  desactivarEmpleado(empleado: EmpleadoResumen): void {
    if (!confirm(`¿Desactivar a ${empleado.nombre} ${empleado.apellido}?`)) {
      return;
    }
    this.guardando.set(true);
    this.servicioEmpleados.desactivar(empleado.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo desactivar el empleado');
      }
    });
  }

  tituloFormulario(): string {
    return this.empleadoEditandoId() ? 'Editar empleado' : 'Alta de empleado';
  }

  etiquetaEstadoEmpleado(estado: string): string {
    return etiquetaEstado(estado);
  }

  varianteEstadoEmpleado(estado: string) {
    return varianteEstado(estado);
  }
}
