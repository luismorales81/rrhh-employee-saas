import { Component, OnInit, signal } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';

import {

  ActivoResumen,

  ServicioActivos

} from '../../../nucleo/servicios/servicio-activos';

import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';



@Component({

  selector: 'app-pagina-activos',

  standalone: true,

  imports: [ReactiveFormsModule, DirectivaTienePermiso],

  templateUrl: './pagina-activos.component.html'

})

export class PaginaActivosComponent implements OnInit {

  readonly cargando = signal(true);

  readonly guardando = signal(false);

  readonly error = signal<string | null>(null);

  readonly activos = signal<ActivoResumen[]>([]);

  readonly empleados = signal<EmpleadoResumen[]>([]);

  readonly mostrarFormularioCrear = signal(false);

  readonly mostrarFormularioAsignar = signal(false);

  readonly mostrarFormularioDevolver = signal(false);

  readonly activoSeleccionado = signal<ActivoResumen | null>(null);

  readonly formularioCrear;

  readonly formularioAsignar;

  readonly formularioDevolver;



  constructor(

    fb: FormBuilder,

    private readonly servicioActivos: ServicioActivos,

    private readonly servicioEmpleados: ServicioEmpleados

  ) {

    this.formularioCrear = fb.nonNullable.group({

      nombre: ['', Validators.required],

      tipo: ['', Validators.required],

      numeroSerie: ['']

    });

    this.formularioAsignar = fb.nonNullable.group({

      empleadoId: ['', Validators.required],

      fechaEntrega: ['', Validators.required],

      notas: ['']

    });

    this.formularioDevolver = fb.nonNullable.group({

      fechaDevolucion: [''],

      notas: ['']

    });

  }



  ngOnInit(): void {

    this.servicioEmpleados.listar().subscribe({

      next: (lista) => this.empleados.set(lista.filter((e) => e.estado === 'ACTIVO'))

    });

    this.recargar();

  }



  recargar(): void {

    this.cargando.set(true);

    this.servicioActivos.listar().subscribe({

      next: (lista) => {

        this.activos.set(lista);

        this.cargando.set(false);

      },

      error: () => {

        this.error.set('No se pudieron cargar los activos');

        this.cargando.set(false);

      }

    });

  }



  alternarFormularioCrear(): void {

    this.mostrarFormularioCrear.update((v) => !v);

    this.mostrarFormularioAsignar.set(false);

    this.mostrarFormularioDevolver.set(false);

  }



  crearActivo(): void {

    if (this.formularioCrear.invalid) {

      this.formularioCrear.markAllAsTouched();

      return;

    }



    const datos = this.formularioCrear.getRawValue();

    this.guardando.set(true);

    this.servicioActivos.crear({

      nombre: datos.nombre,

      tipo: datos.tipo,

      numeroSerie: datos.numeroSerie || undefined

    }).subscribe({

      next: () => {

        this.guardando.set(false);

        this.formularioCrear.reset();

        this.mostrarFormularioCrear.set(false);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo crear el activo');

      }

    });

  }



  eliminarActivo(activo: ActivoResumen): void {

    if (!confirm(`¿Eliminar el activo "${activo.nombre}"?`)) return;

    this.guardando.set(true);

    this.servicioActivos.eliminar(activo.id).subscribe({

      next: () => {

        this.guardando.set(false);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo eliminar el activo');

      }

    });

  }



  abrirAsignar(activo: ActivoResumen): void {

    this.activoSeleccionado.set(activo);

    this.formularioAsignar.reset();

    this.mostrarFormularioAsignar.set(true);

    this.mostrarFormularioDevolver.set(false);

    this.mostrarFormularioCrear.set(false);

  }



  abrirDevolver(activo: ActivoResumen): void {

    this.activoSeleccionado.set(activo);

    this.formularioDevolver.reset();

    this.mostrarFormularioDevolver.set(true);

    this.mostrarFormularioAsignar.set(false);

    this.mostrarFormularioCrear.set(false);

  }



  cerrarFormulariosAccion(): void {

    this.mostrarFormularioAsignar.set(false);

    this.mostrarFormularioDevolver.set(false);

    this.activoSeleccionado.set(null);

  }



  asignarActivo(): void {

    const activo = this.activoSeleccionado();

    if (!activo || this.formularioAsignar.invalid) {

      this.formularioAsignar.markAllAsTouched();

      return;

    }



    const datos = this.formularioAsignar.getRawValue();

    this.guardando.set(true);

    this.servicioActivos.asignar(activo.id, {

      empleadoId: Number(datos.empleadoId),

      fechaEntrega: datos.fechaEntrega,

      notas: datos.notas || undefined

    }).subscribe({

      next: () => {

        this.guardando.set(false);

        this.cerrarFormulariosAccion();

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo asignar el activo');

      }

    });

  }



  devolverActivo(): void {

    const activo = this.activoSeleccionado();

    if (!activo) return;



    const datos = this.formularioDevolver.getRawValue();

    this.guardando.set(true);

    this.servicioActivos.devolver(activo.id, {

      fechaDevolucion: datos.fechaDevolucion || undefined,

      notas: datos.notas || undefined

    }).subscribe({

      next: () => {

        this.guardando.set(false);

        this.cerrarFormulariosAccion();

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo registrar la devolución');

      }

    });

  }

}


