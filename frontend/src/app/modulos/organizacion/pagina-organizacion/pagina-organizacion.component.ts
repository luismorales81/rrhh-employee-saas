import { Component, OnInit, signal } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import {

  AreaResumen,

  GerenciaResumen,

  PuestoResumen,

  ServicioOrganizacion

} from '../../../nucleo/servicios/servicio-organizacion';



type PestanaOrganizacion = 'gerencias' | 'areas' | 'puestos';



@Component({

  selector: 'app-pagina-organizacion',

  standalone: true,

  imports: [ReactiveFormsModule],

  templateUrl: './pagina-organizacion.component.html'

})

export class PaginaOrganizacionComponent implements OnInit {

  readonly cargando = signal(true);

  readonly guardando = signal(false);

  readonly error = signal<string | null>(null);

  readonly gerencias = signal<GerenciaResumen[]>([]);

  readonly areas = signal<AreaResumen[]>([]);

  readonly puestos = signal<PuestoResumen[]>([]);

  readonly pestanaActiva = signal<PestanaOrganizacion>('gerencias');

  readonly mostrarFormulario = signal(false);

  readonly editandoGerenciaId = signal<number | null>(null);

  readonly editandoAreaId = signal<number | null>(null);

  readonly editandoPuestoId = signal<number | null>(null);

  readonly formularioGerencia;

  readonly formularioArea;

  readonly formularioPuesto;



  constructor(

    fb: FormBuilder,

    private readonly servicioOrganizacion: ServicioOrganizacion

  ) {

    this.formularioGerencia = fb.nonNullable.group({

      nombre: ['', Validators.required],

      descripcion: ['']

    });

    this.formularioArea = fb.nonNullable.group({

      nombre: ['', Validators.required],

      descripcion: [''],

      gerenciaId: [0, Validators.required]

    });

    this.formularioPuesto = fb.nonNullable.group({

      nombre: ['', Validators.required],

      descripcion: [''],

      nivel: ['']

    });

  }



  ngOnInit(): void {

    this.recargar();

  }



  recargar(): void {

    this.cargando.set(true);

    this.error.set(null);



    this.servicioOrganizacion.listarGerencias().subscribe({

      next: (lista) => this.gerencias.set(lista),

      error: () => this.error.set('No se pudieron cargar las gerencias')

    });

    this.servicioOrganizacion.listarAreas().subscribe({

      next: (lista) => this.areas.set(lista),

      error: () => this.error.set('No se pudieron cargar las áreas')

    });

    this.servicioOrganizacion.listarPuestos().subscribe({

      next: (lista) => {

        this.puestos.set(lista);

        this.cargando.set(false);

      },

      error: () => {

        this.error.set('No se pudieron cargar los puestos');

        this.cargando.set(false);

      }

    });

  }



  cambiarPestana(pestana: PestanaOrganizacion): void {

    this.pestanaActiva.set(pestana);

    this.cancelarFormulario();

  }



  alternarFormulario(): void {

    if (this.mostrarFormulario()) {

      this.cancelarFormulario();

    } else {

      this.limpiarEdicion();

      this.resetearFormularioActivo();

      this.mostrarFormulario.set(true);

    }

  }



  cancelarFormulario(): void {

    this.mostrarFormulario.set(false);

    this.limpiarEdicion();

  }



  editarGerencia(gerencia: GerenciaResumen): void {

    this.pestanaActiva.set('gerencias');

    this.editandoGerenciaId.set(gerencia.id);

    this.editandoAreaId.set(null);

    this.editandoPuestoId.set(null);

    this.formularioGerencia.patchValue({

      nombre: gerencia.nombre,

      descripcion: gerencia.descripcion ?? ''

    });

    this.mostrarFormulario.set(true);

  }



  editarArea(area: AreaResumen): void {

    this.pestanaActiva.set('areas');

    this.editandoAreaId.set(area.id);

    this.editandoGerenciaId.set(null);

    this.editandoPuestoId.set(null);

    this.formularioArea.patchValue({

      nombre: area.nombre,

      descripcion: area.descripcion ?? '',

      gerenciaId: area.gerenciaId ?? 0

    });

    this.mostrarFormulario.set(true);

  }



  editarPuesto(puesto: PuestoResumen): void {

    this.pestanaActiva.set('puestos');

    this.editandoPuestoId.set(puesto.id);

    this.editandoGerenciaId.set(null);

    this.editandoAreaId.set(null);

    this.formularioPuesto.patchValue({

      nombre: puesto.nombre,

      descripcion: puesto.descripcion ?? '',

      nivel: puesto.nivel ?? ''

    });

    this.mostrarFormulario.set(true);

  }



  guardarGerencia(): void {

    if (this.formularioGerencia.invalid) {

      this.formularioGerencia.markAllAsTouched();

      return;

    }



    const datos = this.formularioGerencia.getRawValue();

    const id = this.editandoGerenciaId();

    this.guardando.set(true);

    this.error.set(null);



    const peticion = id

      ? this.servicioOrganizacion.actualizarGerencia(id, {

          nombre: datos.nombre,

          descripcion: datos.descripcion || undefined

        })

      : this.servicioOrganizacion.crearGerencia({

          nombre: datos.nombre,

          descripcion: datos.descripcion || undefined

        });



    peticion.subscribe({

      next: () => {

        this.guardando.set(false);

        this.cancelarFormulario();

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo guardar la gerencia');

      }

    });

  }



  guardarArea(): void {

    if (this.formularioArea.invalid || this.formularioArea.getRawValue().gerenciaId === 0) {

      this.formularioArea.markAllAsTouched();

      return;

    }



    const datos = this.formularioArea.getRawValue();

    const id = this.editandoAreaId();

    this.guardando.set(true);

    this.error.set(null);



    const cuerpo = {

      nombre: datos.nombre,

      descripcion: datos.descripcion || undefined,

      gerenciaId: Number(datos.gerenciaId)

    };



    const peticion = id

      ? this.servicioOrganizacion.actualizarArea(id, cuerpo)

      : this.servicioOrganizacion.crearArea(cuerpo);



    peticion.subscribe({

      next: () => {

        this.guardando.set(false);

        this.cancelarFormulario();

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el área');

      }

    });

  }



  guardarPuesto(): void {

    if (this.formularioPuesto.invalid) {

      this.formularioPuesto.markAllAsTouched();

      return;

    }



    const datos = this.formularioPuesto.getRawValue();

    const id = this.editandoPuestoId();

    this.guardando.set(true);

    this.error.set(null);



    const cuerpo = {

      nombre: datos.nombre,

      descripcion: datos.descripcion || undefined,

      nivel: datos.nivel || undefined

    };



    const peticion = id

      ? this.servicioOrganizacion.actualizarPuesto(id, cuerpo)

      : this.servicioOrganizacion.crearPuesto(cuerpo);



    peticion.subscribe({

      next: () => {

        this.guardando.set(false);

        this.cancelarFormulario();

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el puesto');

      }

    });

  }



  eliminarGerencia(gerencia: GerenciaResumen): void {

    if (!confirm(`¿Eliminar la gerencia "${gerencia.nombre}"?`)) return;

    this.servicioOrganizacion.eliminarGerencia(gerencia.id).subscribe({

      next: () => this.recargar(),

      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar la gerencia')

    });

  }



  eliminarArea(area: AreaResumen): void {

    if (!confirm(`¿Eliminar el área "${area.nombre}"?`)) return;

    this.servicioOrganizacion.eliminarArea(area.id).subscribe({

      next: () => this.recargar(),

      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el área')

    });

  }



  eliminarPuesto(puesto: PuestoResumen): void {

    if (!confirm(`¿Eliminar el puesto "${puesto.nombre}"?`)) return;

    this.servicioOrganizacion.eliminarPuesto(puesto.id).subscribe({

      next: () => this.recargar(),

      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el puesto')

    });

  }



  tituloFormulario(): string {

    if (this.pestanaActiva() === 'gerencias') {

      return this.editandoGerenciaId() ? 'Editar gerencia' : 'Nueva gerencia';

    }

    if (this.pestanaActiva() === 'areas') {

      return this.editandoAreaId() ? 'Editar área' : 'Nueva área';

    }

    return this.editandoPuestoId() ? 'Editar puesto' : 'Nuevo puesto';

  }



  private limpiarEdicion(): void {

    this.editandoGerenciaId.set(null);

    this.editandoAreaId.set(null);

    this.editandoPuestoId.set(null);

  }



  private resetearFormularioActivo(): void {

    if (this.pestanaActiva() === 'gerencias') {

      this.formularioGerencia.reset();

    } else if (this.pestanaActiva() === 'areas') {

      this.formularioArea.reset({ gerenciaId: 0 });

    } else {

      this.formularioPuesto.reset();

    }

  }

}

