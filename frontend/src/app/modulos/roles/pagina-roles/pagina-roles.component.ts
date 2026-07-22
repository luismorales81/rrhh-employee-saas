import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import {
  RolResumen,
  ServicioRoles
} from '../../../nucleo/servicios/servicio-roles';
import {
  PermisoResumen,
  ServicioPermisosApi
} from '../../../nucleo/servicios/servicio-permisos-api';

@Component({
  selector: 'app-pagina-roles',
  standalone: true,
  imports: [ReactiveFormsModule, DirectivaTienePermiso],
  templateUrl: './pagina-roles.component.html'
})
export class PaginaRolesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly roles = signal<RolResumen[]>([]);
  readonly permisos = signal<PermisoResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly rolEditandoId = signal<number | null>(null);
  readonly permisosSeleccionados = signal<Set<number>>(new Set());
  readonly formularioRol;

  constructor(
    fb: FormBuilder,
    private readonly servicioRoles: ServicioRoles,
    private readonly servicioPermisosApi: ServicioPermisosApi
  ) {
    this.formularioRol = fb.nonNullable.group({
      nombre: ['', Validators.required],
      descripcion: ['']
    });
  }

  ngOnInit(): void {
    this.servicioPermisosApi.listar().subscribe({
      next: (lista) => this.permisos.set(lista),
      error: () => this.error.set('No se pudieron cargar los permisos')
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioRoles.listar().subscribe({
      next: (lista) => {
        this.roles.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los roles');
        this.cargando.set(false);
      }
    });
  }

  alternarFormulario(): void {
    if (this.mostrarFormulario()) {
      this.cancelarFormulario();
    } else {
      this.rolEditandoId.set(null);
      this.permisosSeleccionados.set(new Set());
      this.formularioRol.reset();
      this.mostrarFormulario.set(true);
    }
  }

  editarRol(rol: RolResumen): void {
    this.rolEditandoId.set(rol.id);
    this.formularioRol.patchValue({
      nombre: rol.nombre,
      descripcion: rol.descripcion ?? ''
    });
    const ids = this.permisos()
      .filter((p) => rol.permisos.includes(p.codigo))
      .map((p) => p.id);
    this.permisosSeleccionados.set(new Set(ids));
    this.mostrarFormulario.set(true);
  }

  cancelarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.rolEditandoId.set(null);
    this.permisosSeleccionados.set(new Set());
    this.formularioRol.reset();
  }

  alternarPermiso(permisoId: number): void {
    this.permisosSeleccionados.update((set) => {
      const copia = new Set(set);
      if (copia.has(permisoId)) {
        copia.delete(permisoId);
      } else {
        copia.add(permisoId);
      }
      return copia;
    });
  }

  permisoMarcado(permisoId: number): boolean {
    return this.permisosSeleccionados().has(permisoId);
  }

  guardarRol(): void {
    if (this.formularioRol.invalid) {
      this.formularioRol.markAllAsTouched();
      return;
    }
    if (this.permisosSeleccionados().size === 0) {
      this.error.set('Seleccioná al menos un permiso');
      return;
    }

    this.guardando.set(true);
    const datos = this.formularioRol.getRawValue();
    const solicitud = {
      nombre: datos.nombre,
      descripcion: datos.descripcion || undefined,
      permisosIds: Array.from(this.permisosSeleccionados())
    };

    const idEditando = this.rolEditandoId();
    const operacion = idEditando
      ? this.servicioRoles.actualizar(idEditando, solicitud)
      : this.servicioRoles.crear(solicitud);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelarFormulario();
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el rol');
      }
    });
  }

  eliminarRol(rol: RolResumen): void {
    if (!confirm(`¿Eliminar el rol "${rol.nombre}"?`)) {
      return;
    }
    this.guardando.set(true);
    this.servicioRoles.eliminar(rol.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el rol');
      }
    });
  }

  tituloFormulario(): string {
    return this.rolEditandoId() ? 'Editar rol' : 'Nuevo rol';
  }
}
