import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ServicioUsuarios, UsuarioResumen } from '../../../nucleo/servicios/servicio-usuarios';
import { RolResumen, ServicioRoles } from '../../../nucleo/servicios/servicio-roles';
import { ServicioContextoEmpresa } from '../../../nucleo/servicios/servicio-contexto-empresa';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';

@Component({
  selector: 'app-pagina-usuarios',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './pagina-usuarios.component.html'
})
export class PaginaUsuariosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly usuarios = signal<UsuarioResumen[]>([]);
  readonly roles = signal<RolResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly rolesSeleccionados = signal<Set<number>>(new Set());
  readonly formularioCrear;

  constructor(
    fb: FormBuilder,
    private readonly servicioUsuarios: ServicioUsuarios,
    private readonly servicioRoles: ServicioRoles,
    private readonly servicioContextoEmpresa: ServicioContextoEmpresa,
    private readonly servicioAutenticacion: ServicioAutenticacion,
    private readonly servicioPermisos: ServicioPermisos
  ) {
    this.formularioCrear = fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
    this.servicioRoles.listar().subscribe({
      next: (lista) => this.roles.set(lista),
      error: () => this.error.set('No se pudieron cargar los roles')
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioUsuarios.listar().subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los usuarios');
        this.cargando.set(false);
      }
    });
  }

  alternarFormulario(): void {
    this.mostrarFormulario.update((v) => !v);
  }

  alternarRol(rolId: number): void {
    this.rolesSeleccionados.update((set) => {
      const copia = new Set(set);
      if (copia.has(rolId)) {
        copia.delete(rolId);
      } else {
        copia.add(rolId);
      }
      return copia;
    });
  }

  rolMarcado(rolId: number): boolean {
    return this.rolesSeleccionados().has(rolId);
  }

  crearUsuario(): void {
    if (this.formularioCrear.invalid) {
      this.formularioCrear.markAllAsTouched();
      return;
    }

    const empresaId = this.servicioContextoEmpresa.obtenerEmpresaIdActiva(
      this.servicioAutenticacion.sesionActual()
    );
    if (!empresaId) {
      this.error.set('Debe seleccionar una empresa');
      return;
    }

    if (this.rolesSeleccionados().size === 0) {
      this.error.set('Seleccioná al menos un rol');
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    const datos = this.formularioCrear.getRawValue();

    this.servicioUsuarios.crear({
      empresaId,
      email: datos.email,
      password: datos.password,
      rolesIds: Array.from(this.rolesSeleccionados())
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioCrear.reset();
        this.rolesSeleccionados.set(new Set());
        this.mostrarFormulario.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear el usuario');
      }
    });
  }

  esVistaPlataforma(): boolean {
    return this.servicioPermisos.esSuperAdmin()
        && !this.servicioContextoEmpresa.modoSoporte();
  }
}
