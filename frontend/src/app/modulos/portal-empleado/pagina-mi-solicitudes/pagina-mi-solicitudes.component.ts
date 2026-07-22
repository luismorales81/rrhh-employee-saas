import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  ServicioSolicitudes,
  SolicitudResumen,
  TipoSolicitudResumen
} from '../../../nucleo/servicios/servicio-solicitudes';
import { ServicioVacaciones } from '../../../nucleo/servicios/servicio-vacaciones';
import { enriquecerSolicitudesConVacaciones } from '../../../nucleo/utilidades/enriquecer-solicitudes';
import { formatearEstadoSolicitud } from '../../../nucleo/utilidades/formatear-estado-solicitud';

@Component({
  selector: 'app-pagina-mi-solicitudes',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, EsqueletoCargaComponent],
  templateUrl: './pagina-mi-solicitudes.component.html'
})
export class PaginaMiSolicitudesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly solicitudes = signal<SolicitudResumen[]>([]);
  readonly tipos = signal<TipoSolicitudResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly formularioCrear;

  constructor(
    fb: FormBuilder,
    private readonly servicioSolicitudes: ServicioSolicitudes,
    private readonly servicioVacaciones: ServicioVacaciones
  ) {
    this.formularioCrear = fb.nonNullable.group({
      tipoSolicitudId: ['', Validators.required],
      motivo: ['']
    });
  }

  ngOnInit(): void {
    this.recargar();
    this.servicioSolicitudes.listarTipos().subscribe({
      next: (lista) => {
        this.tipos.set(lista.filter((tipo) => !tipo.nombre.toLowerCase().includes('vacacion')));
      },
      error: () => this.error.set('No se pudieron cargar los tipos de solicitud')
    });
  }

  readonly solicitudesVisibles = computed(() =>
    this.solicitudes().filter((solicitud) => solicitud.estado !== 'CANCELADO')
  );

  recargar(): void {
    this.cargando.set(true);
    forkJoin({
      solicitudes: this.servicioSolicitudes.listarPropias(),
      vacaciones: this.servicioVacaciones.listarPropias().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ solicitudes, vacaciones }) => {
        this.solicitudes.set(enriquecerSolicitudesConVacaciones(solicitudes, vacaciones));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus solicitudes');
        this.cargando.set(false);
      }
    });
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  alternarFormulario(): void {
    this.mostrarFormulario.update((v) => !v);
  }

  crearYEnviar(): void {
    if (this.formularioCrear.invalid) {
      this.formularioCrear.markAllAsTouched();
      return;
    }

    const datos = this.formularioCrear.getRawValue();
    this.guardando.set(true);
    this.error.set(null);

    this.servicioSolicitudes.crear({
      tipoSolicitudId: Number(datos.tipoSolicitudId),
      motivo: datos.motivo || undefined
    }).subscribe({
      next: (creada) => {
        if (creada.estado === 'BORRADOR' || creada.estado === 'CAMBIOS_SOLICITADOS') {
          this.servicioSolicitudes.enviar(creada.id).subscribe({
            next: () => {
              this.guardando.set(false);
              this.formularioCrear.reset();
              this.mostrarFormulario.set(false);
              this.recargar();
            },
            error: (err) => {
              this.guardando.set(false);
              this.error.set(err.error?.mensaje ?? 'Solicitud creada pero no se pudo enviar');
              this.recargar();
            }
          });
          return;
        }
        this.guardando.set(false);
        this.formularioCrear.reset();
        this.mostrarFormulario.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear la solicitud');
      }
    });
  }

  enviar(id: number): void {
    this.servicioSolicitudes.enviar(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo enviar la solicitud')
    });
  }

  cancelar(id: number): void {
    this.servicioSolicitudes.cancelar(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo cancelar la solicitud')
    });
  }

  puedeEnviar(estado: string): boolean {
    return estado === 'BORRADOR' || estado === 'CAMBIOS_SOLICITADOS';
  }

  puedeCancelar(estado: string): boolean {
    return estado === 'BORRADOR'
      || estado === 'EN_REVISION'
      || estado === 'CAMBIOS_SOLICITADOS';
  }
}
