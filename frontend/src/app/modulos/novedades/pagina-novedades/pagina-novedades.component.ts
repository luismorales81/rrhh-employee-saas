import { Component, OnInit, computed, signal } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { DatePipe } from '@angular/common';

import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';

import {

  NovedadLiquidacionResumen,

  ServicioNovedades,

  TipoNovedadLiquidacion

} from '../../../nucleo/servicios/servicio-novedades';

import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';



const TIPOS_NOVEDAD: TipoNovedadLiquidacion[] = [

  'HORAS_EXTRAS', 'BONIFICACION', 'COMISION', 'AUSENCIA', 'LICENCIA', 'OTRO'

];



const ETIQUETAS_TIPO: Record<TipoNovedadLiquidacion, string> = {

  HORAS_EXTRAS: 'Horas extras',

  BONIFICACION: 'Bonificación',

  COMISION: 'Comisión',

  AUSENCIA: 'Ausencia',

  LICENCIA: 'Licencia',

  OTRO: 'Otro'

};



@Component({

  selector: 'app-pagina-novedades',

  standalone: true,

  imports: [ReactiveFormsModule, DatePipe, DirectivaTienePermiso],

  templateUrl: './pagina-novedades.component.html'

})

export class PaginaNovedadesComponent implements OnInit {

  readonly cargando = signal(true);

  readonly guardando = signal(false);

  readonly error = signal<string | null>(null);

  readonly novedades = signal<NovedadLiquidacionResumen[]>([]);

  readonly empleados = signal<EmpleadoResumen[]>([]);

  readonly periodoFiltro = signal('');

  readonly loteConfirmado = signal(false);

  readonly mostrarFormulario = signal(false);

  readonly novedadEditando = signal<NovedadLiquidacionResumen | null>(null);

  readonly tiposNovedad = TIPOS_NOVEDAD;

  readonly etiquetasTipo = ETIQUETAS_TIPO;

  readonly formularioNovedad;



  readonly novedadesFiltradas = computed(() => {

    const periodo = this.periodoFiltro();

    const lista = this.novedades();

    if (!periodo) return lista;

    return lista.filter((n) => n.periodo === periodo);

  });



  constructor(

    fb: FormBuilder,

    private readonly servicioNovedades: ServicioNovedades,

    private readonly servicioEmpleados: ServicioEmpleados

  ) {

    const ahora = new Date();

    const periodoActual = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}`;

    this.periodoFiltro.set(periodoActual);



    this.formularioNovedad = fb.nonNullable.group({

      empleadoId: ['', Validators.required],

      periodo: [periodoActual, [Validators.required, Validators.pattern(/^\d{4}-\d{2}$/)]],

      tipo: ['HORAS_EXTRAS' as TipoNovedadLiquidacion, Validators.required],

      descripcion: [''],

      cantidad: [''],

      monto: [''],

      fecha: ['', Validators.required]

    });

  }



  ngOnInit(): void {

    this.servicioEmpleados.listar().subscribe({

      next: (lista) => this.empleados.set(lista)

    });

    this.recargar();

  }



  recargar(): void {

    this.cargando.set(true);

    this.loteConfirmado.set(false);

    this.servicioNovedades.listar().subscribe({

      next: (lista) => {

        this.novedades.set(lista);

        this.cargando.set(false);

      },

      error: () => {

        this.error.set('No se pudieron cargar las novedades');

        this.cargando.set(false);

      }

    });

  }



  establecerPeriodoFiltro(valor: string): void {

    this.periodoFiltro.set(valor);

    this.loteConfirmado.set(false);

  }



  alternarFormulario(): void {

    this.mostrarFormulario.update((v) => !v);

    this.novedadEditando.set(null);

    if (this.mostrarFormulario()) {

      this.formularioNovedad.reset({

        periodo: this.periodoFiltro(),

        tipo: 'HORAS_EXTRAS'

      });

    }

  }



  editarNovedad(novedad: NovedadLiquidacionResumen): void {

    this.novedadEditando.set(novedad);

    this.mostrarFormulario.set(true);

    this.formularioNovedad.patchValue({

      empleadoId: String(novedad.empleadoId),

      periodo: novedad.periodo,

      tipo: novedad.tipo,

      descripcion: novedad.descripcion ?? '',

      cantidad: novedad.cantidad != null ? String(novedad.cantidad) : '',

      monto: novedad.monto != null ? String(novedad.monto) : '',

      fecha: novedad.fecha

    });

  }



  guardarNovedad(): void {

    if (this.formularioNovedad.invalid) {

      this.formularioNovedad.markAllAsTouched();

      return;

    }



    const datos = this.formularioNovedad.getRawValue();

    const cantidad = datos.cantidad ? Number(datos.cantidad) : undefined;

    const monto = datos.monto ? Number(datos.monto) : undefined;

    const editando = this.novedadEditando();



    this.guardando.set(true);

    this.error.set(null);



    const operacion = editando

      ? this.servicioNovedades.actualizar(editando.id, {

          tipo: datos.tipo,

          descripcion: datos.descripcion || undefined,

          cantidad,

          monto,

          fecha: datos.fecha

        })

      : this.servicioNovedades.crear({

          empleadoId: Number(datos.empleadoId),

          periodo: datos.periodo,

          tipo: datos.tipo,

          descripcion: datos.descripcion || undefined,

          cantidad,

          monto,

          fecha: datos.fecha

        });



    operacion.subscribe({

      next: () => {

        this.guardando.set(false);

        this.mostrarFormulario.set(false);

        this.novedadEditando.set(null);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo guardar la novedad');

      }

    });

  }



  eliminarNovedad(novedad: NovedadLiquidacionResumen): void {

    if (!confirm(`¿Eliminar la novedad de ${novedad.nombreEmpleado}?`)) return;

    this.guardando.set(true);

    this.servicioNovedades.eliminar(novedad.id).subscribe({

      next: () => {

        this.guardando.set(false);

        this.recargar();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'No se pudo eliminar la novedad');

      }

    });

  }



  confirmarLote(): void {
    const periodo = this.periodoFiltro();
    if (!periodo) {
      this.error.set('Seleccioná un período');
      return;
    }
    const borradores = this.novedadesFiltradas().filter((n) => n.estado === 'BORRADOR');
    if (borradores.length === 0) {
      this.error.set('No hay novedades en borrador para confirmar en este período');
      return;
    }
    if (!confirm(`¿Confirmar ${borradores.length} novedad(es) en borrador del período ${periodo}?`)) {
      return;
    }
    this.guardando.set(true);
    this.servicioNovedades.confirmarLote(periodo).subscribe({
      next: (respuesta) => {
        this.guardando.set(false);
        this.loteConfirmado.set(true);
        this.error.set(null);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo confirmar el lote');
      }
    });
  }



  exportarCsv(): void {

    const periodo = this.periodoFiltro();

    if (!periodo) {

      this.error.set('Seleccioná un período para exportar');

      return;

    }

    this.guardando.set(true);

    this.servicioNovedades.exportarCsv(periodo).subscribe({

      next: (blob) => {

        const url = window.URL.createObjectURL(blob);

        const enlace = document.createElement('a');

        enlace.href = url;

        enlace.download = `novedades_${periodo}.csv`;

        enlace.click();

        window.URL.revokeObjectURL(url);

        this.guardando.set(false);

      },

      error: () => {

        this.guardando.set(false);

        this.error.set('No se pudo exportar el archivo CSV');

      }

    });

  }



  etiquetaTipo(tipo: TipoNovedadLiquidacion): string {

    return this.etiquetasTipo[tipo] ?? tipo;

  }

}


