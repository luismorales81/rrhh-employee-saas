import { Component, OnInit, signal } from '@angular/core';
import { NgFor } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  FlujoTrabajoResumen,
  ServicioWorkflow,
  TipoSolicitudResumen
} from '../../../nucleo/servicios/servicio-workflow';
import { RolResumen, ServicioRoles } from '../../../nucleo/servicios/servicio-roles';
import { ServicioUsuarios, UsuarioResumen } from '../../../nucleo/servicios/servicio-usuarios';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';

type PestanaWorkflow = 'flujos' | 'tipos';

const TIPOS_PASO = [
  'SUPERVISOR_DIRECTO',
  'RRHH',
  'GERENCIA',
  'ROL_ESPECIFICO',
  'USUARIO_ESPECIFICO'
] as const;

const ETIQUETAS_TIPO_PASO: Record<string, string> = {
  SUPERVISOR_DIRECTO: 'Supervisor directo',
  RRHH: 'Equipo',
  GERENCIA: 'Gerencia',
  ROL_ESPECIFICO: 'Rol específico',
  USUARIO_ESPECIFICO: 'Usuario específico'
};

@Component({
  selector: 'app-pagina-workflow',
  standalone: true,
  imports: [NgFor, ReactiveFormsModule],
  templateUrl: './pagina-workflow.component.html'
})
export class PaginaWorkflowComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly flujos = signal<FlujoTrabajoResumen[]>([]);
  readonly tipos = signal<TipoSolicitudResumen[]>([]);
  readonly roles = signal<RolResumen[]>([]);
  readonly usuarios = signal<UsuarioResumen[]>([]);
  readonly pestanaActiva = signal<PestanaWorkflow>('flujos');
  readonly mostrarFormulario = signal(false);
  readonly flujoExpandidoId = signal<number | null>(null);
  readonly flujoEditandoId = signal<number | null>(null);
  readonly tipoEditandoId = signal<number | null>(null);
  readonly tiposPaso = TIPOS_PASO;
  readonly formularioFlujo: FormGroup;
  readonly formularioTipo;

  constructor(
    private readonly fb: FormBuilder,
    private readonly servicioWorkflow: ServicioWorkflow,
    private readonly servicioRoles: ServicioRoles,
    private readonly servicioUsuarios: ServicioUsuarios,
    private readonly servicioPermisos: ServicioPermisos
  ) {
    this.formularioFlujo = fb.nonNullable.group({
      nombre: ['', Validators.required],
      descripcion: [''],
      pasos: fb.array([this.crearGrupoPaso(1)])
    });
    this.formularioTipo = fb.nonNullable.group({
      nombre: ['', Validators.required],
      descripcion: [''],
      flujoTrabajoId: [''],
      requiereAprobacion: [true],
      requiereAdjunto: [false]
    });
  }

  ngOnInit(): void {
    this.servicioRoles.listar().subscribe({
      next: (lista) => this.roles.set(lista)
    });
    this.servicioUsuarios.listar().subscribe({
      next: (lista) => this.usuarios.set(lista)
    });
    this.recargar();
  }

  get pasosFormulario(): FormArray {
    return this.formularioFlujo.get('pasos') as FormArray;
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.servicioWorkflow.listarFlujos().subscribe({
      next: (lista) => this.flujos.set(lista.map((flujo) => this.normalizarFlujo(flujo))),
      error: () => this.error.set('No se pudieron cargar los flujos de trabajo')
    });
    this.servicioWorkflow.listarTipos().subscribe({
      next: (lista) => {
        this.tipos.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los tipos de solicitud');
        this.cargando.set(false);
      }
    });
  }

  cambiarPestana(pestana: PestanaWorkflow): void {
    this.pestanaActiva.set(pestana);
    this.mostrarFormulario.set(false);
  }

  alternarFormulario(): void {
    if (this.mostrarFormulario()) {
      this.cancelarFormulario();
    } else {
      this.flujoEditandoId.set(null);
      this.tipoEditandoId.set(null);
      this.mostrarFormulario.set(true);
      if (this.pestanaActiva() === 'flujos') {
        this.reiniciarPasos();
      }
    }
  }

  cancelarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.flujoEditandoId.set(null);
    this.tipoEditandoId.set(null);
    this.formularioFlujo.reset({ nombre: '', descripcion: '' });
    this.formularioTipo.reset({ requiereAprobacion: true, requiereAdjunto: false });
    this.reiniciarPasos();
  }

  editarFlujo(flujo: FlujoTrabajoResumen): void {
    this.flujoEditandoId.set(flujo.id);
    this.mostrarFormulario.set(true);
    this.pestanaActiva.set('flujos');
    this.servicioWorkflow.obtenerFlujo(flujo.id).subscribe({
      next: (detalle) => {
        const flujoNormalizado = this.normalizarFlujo(detalle);
        this.formularioFlujo.patchValue({
          nombre: flujoNormalizado.nombre,
          descripcion: flujoNormalizado.descripcion ?? ''
        });
        this.pasosFormulario.clear();
        flujoNormalizado.pasos.forEach((paso) => {
          this.pasosFormulario.push(this.crearGrupoPasoDesdeDetalle(paso));
        });
        if (flujoNormalizado.pasos.length === 0) {
          this.reiniciarPasos();
        }
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudieron cargar los pasos del flujo')
    });
  }

  editarTipo(tipo: TipoSolicitudResumen): void {
    this.tipoEditandoId.set(tipo.id);
    this.mostrarFormulario.set(true);
    this.pestanaActiva.set('tipos');
    this.formularioTipo.patchValue({
      nombre: tipo.nombre,
      descripcion: tipo.descripcion ?? '',
      flujoTrabajoId: tipo.flujoTrabajoId ? String(tipo.flujoTrabajoId) : '',
      requiereAprobacion: tipo.requiereAprobacion,
      requiereAdjunto: tipo.requiereAdjunto
    });
  }

  desactivarFlujo(id: number): void {
    this.servicioWorkflow.alternarActivoFlujo(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo cambiar el estado del flujo')
    });
  }

  eliminarFlujo(id: number): void {
    if (!confirm('¿Eliminar este flujo de trabajo?')) return;
    this.servicioWorkflow.eliminarFlujo(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el flujo')
    });
  }

  eliminarTipo(id: number): void {
    if (!confirm('¿Eliminar este tipo de solicitud?')) return;
    this.servicioWorkflow.eliminarTipo(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el tipo')
    });
  }

  tituloFormularioFlujo(): string {
    return this.flujoEditandoId() ? 'Editar flujo de trabajo' : 'Nuevo flujo de trabajo';
  }

  tituloFormularioTipo(): string {
    return this.tipoEditandoId() ? 'Editar tipo de solicitud' : 'Nuevo tipo de solicitud';
  }

  alternarDetalleFlujo(id: number): void {
    this.flujoExpandidoId.update((actual) => actual === id ? null : id);
  }

  agregarPaso(): void {
    const orden = this.pasosFormulario.length + 1;
    this.pasosFormulario.push(this.crearGrupoPaso(orden));
  }

  private crearGrupoPaso(orden: number): FormGroup {
    return this.fb.nonNullable.group({
      orden: [orden, Validators.required],
      nombre: [`Paso ${orden}`, Validators.required],
      tipoPaso: ['RRHH', Validators.required],
      rolId: [''],
      usuarioId: ['']
    });
  }

  private crearGrupoPasoDesdeDetalle(paso: FlujoTrabajoResumen['pasos'][number]): FormGroup {
    return this.fb.nonNullable.group({
      orden: [paso.orden, Validators.required],
      nombre: [paso.nombre, Validators.required],
      tipoPaso: [paso.tipoPaso, Validators.required],
      rolId: [paso.rolId ? String(paso.rolId) : ''],
      usuarioId: [paso.usuarioId ? String(paso.usuarioId) : '']
    });
  }

  private normalizarFlujo(flujo: FlujoTrabajoResumen): FlujoTrabajoResumen {
    const pasos = [...(flujo.pasos ?? [])].sort((a, b) => a.orden - b.orden);
    return { ...flujo, pasos };
  }

  quitarPaso(indice: number): void {
    if (this.pasosFormulario.length <= 1) return;
    this.pasosFormulario.removeAt(indice);
    this.reordenarPasos();
  }

  private reordenarPasos(): void {
    this.pasosFormulario.controls.forEach((control, indice) => {
      control.patchValue({ orden: indice + 1 });
    });
  }

  private reiniciarPasos(): void {
    this.pasosFormulario.clear();
    this.pasosFormulario.push(this.crearGrupoPaso(1));
  }

  requiereRol(indice: number): boolean {
    return this.pasosFormulario.at(indice).get('tipoPaso')?.value === 'ROL_ESPECIFICO';
  }

  requiereUsuario(indice: number): boolean {
    return this.pasosFormulario.at(indice).get('tipoPaso')?.value === 'USUARIO_ESPECIFICO';
  }

  crearFlujo(): void {
    if (this.formularioFlujo.invalid) {
      this.formularioFlujo.markAllAsTouched();
      return;
    }

    const datos = this.formularioFlujo.getRawValue();
    const pasos = datos.pasos.map((paso: {
      orden: number;
      nombre: string;
      tipoPaso: string;
      rolId: string;
      usuarioId: string;
    }) => ({
      orden: paso.orden,
      nombre: paso.nombre,
      tipoPaso: paso.tipoPaso,
      rolId: paso.rolId ? Number(paso.rolId) : null,
      usuarioId: paso.usuarioId ? Number(paso.usuarioId) : null
    }));

    const solicitud = {
      nombre: datos.nombre,
      descripcion: datos.descripcion || undefined,
      pasos
    };

    this.guardando.set(true);
    const idEditando = this.flujoEditandoId();
    const operacion = idEditando
      ? this.servicioWorkflow.actualizarFlujo(idEditando, solicitud)
      : this.servicioWorkflow.crearFlujo(solicitud);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelarFormulario();
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el flujo de trabajo');
      }
    });
  }

  crearTipo(): void {
    if (this.formularioTipo.invalid) {
      this.formularioTipo.markAllAsTouched();
      return;
    }

    const datos = this.formularioTipo.getRawValue();
    const flujoId = datos.flujoTrabajoId ? Number(datos.flujoTrabajoId) : null;

    this.guardando.set(true);
    const idEditando = this.tipoEditandoId();
    const solicitud = {
      nombre: datos.nombre,
      descripcion: datos.descripcion || undefined,
      flujoTrabajoId: flujoId,
      requiereAprobacion: datos.requiereAprobacion,
      requiereAdjunto: datos.requiereAdjunto
    };
    const operacion = idEditando
      ? this.servicioWorkflow.actualizarTipo(idEditando, solicitud)
      : this.servicioWorkflow.crearTipo(solicitud);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelarFormulario();
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el tipo de solicitud');
      }
    });
  }

  nombreFlujo(flujoId: number | null): string {
    if (!flujoId) return '—';
    const flujo = this.flujos().find((f) => f.id === flujoId);
    return flujo?.nombre ?? String(flujoId);
  }

  nombreRol(rolId: number | null): string {
    if (!rolId) return '—';
    return this.roles().find((r) => r.id === rolId)?.nombre ?? String(rolId);
  }

  nombreUsuario(usuarioId: number | null): string {
    if (!usuarioId) return '—';
    return this.usuarios().find((u) => u.id === usuarioId)?.email ?? String(usuarioId);
  }

  puedeConfigurarFlujos(): boolean {
    return this.servicioPermisos.tienePermiso('workflow.configurar');
  }

  puedeGestionarTipos(): boolean {
    return this.servicioPermisos.tienePermiso('tipo_solicitud.gestionar');
  }

  pasosDelFlujo(flujo: FlujoTrabajoResumen) {
    return flujo.pasos ?? [];
  }

  cantidadPasos(flujo: FlujoTrabajoResumen): number {
    return this.pasosDelFlujo(flujo).length;
  }

  etiquetaTipoPaso(tipoPaso: string): string {
    return ETIQUETAS_TIPO_PASO[tipoPaso] ?? tipoPaso;
  }

  descripcionPaso(paso: FlujoTrabajoResumen['pasos'][number]): string {
    const detalleRol = paso.rolId ? ` · Rol: ${this.nombreRol(paso.rolId)}` : '';
    const detalleUsuario = paso.usuarioId ? ` · Usuario: ${this.nombreUsuario(paso.usuarioId)}` : '';
    return `${paso.orden}. ${paso.nombre} (${this.etiquetaTipoPaso(paso.tipoPaso)}${detalleRol}${detalleUsuario})`;
  }
}
