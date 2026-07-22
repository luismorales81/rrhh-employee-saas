import { Injectable, inject, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ServicioDocumentos } from './servicio-documentos';
import { ServicioRecibos } from './servicio-recibos';
import { ServicioNotificaciones } from './servicio-notificaciones';
import { ServicioEmpleados } from './servicio-empleados';
import { ServicioPermisos } from './servicio-permisos';

export interface ContadoresTabs {
  papeles: number;
  notificaciones: number;
}

@Injectable({ providedIn: 'root' })
export class ServicioContadoresTabs {
  private readonly servicioDocumentos = inject(ServicioDocumentos);
  private readonly servicioRecibos = inject(ServicioRecibos);
  private readonly servicioNotificaciones = inject(ServicioNotificaciones);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioPermisos = inject(ServicioPermisos);

  readonly contadores = signal<ContadoresTabs>({ papeles: 0, notificaciones: 0 });

  refrescar(): void {
    if (!this.servicioPermisos.esPortalEmpleado()) {
      return;
    }

    this.servicioEmpleados.obtenerPropio().pipe(
      switchMap(() =>
        forkJoin({
          documentos: this.servicioDocumentos.listarMisAsignaciones().pipe(catchError(() => of([]))),
          recibos: this.servicioRecibos.listarPropios().pipe(catchError(() => of([]))),
          notificaciones: this.servicioNotificaciones.contarNoLeidas().pipe(
            catchError(() => of({ cantidad: 0 }))
          )
        })
      ),
      catchError(() => of(null))
    ).subscribe((datos) => {
      if (!datos) {
        return;
      }
      const docsPendientes = datos.documentos.filter(
        (d) => d.estado !== 'LEIDO' && (!d.requiereFirma || !d.firmado)
      ).length;
      const recibosPendientes = datos.recibos.filter(
        (r) => r.estado === 'PENDIENTE' || r.estado === 'VISTO'
      ).length;
      this.contadores.set({
        papeles: docsPendientes + recibosPendientes,
        notificaciones: datos.notificaciones.cantidad
      });
    });
  }
}
