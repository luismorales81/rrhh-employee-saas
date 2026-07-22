import { Component, inject, OnInit, signal } from '@angular/core';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular/standalone';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { FilaAccesoComponent } from '../../compartido/componentes/fila-acceso/fila-acceso.component';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { ServicioFichajes } from '../../nucleo/servicios/servicio-fichajes';
import { ServicioVacaciones } from '../../nucleo/servicios/servicio-vacaciones';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-pagina-mi-tiempo',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    EncabezadoPaginaComponent,
    FilaAccesoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Mi tiempo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Mi tiempo"
        subtitulo="Fichaje, vacaciones y ausencias"
      />

      <div class="lista-accesos">
        @if (servicioPermisos.tienePermiso('fichaje.registrar')) {
          <app-fila-acceso
            titulo="Fichar"
            [subtitulo]="estadoFichaje()"
            icono="time-outline"
            ruta="/tabs/fichaje"
          />
        }
        <app-fila-acceso
          titulo="Vacaciones"
          [subtitulo]="saldoVacaciones()"
          icono="calendar-outline"
          ruta="/tabs/vacaciones"
        />
        <app-fila-acceso
          titulo="Ausencias"
          subtitulo="Avisá una falta o subí un certificado"
          icono="medkit-outline"
          ruta="/tabs/ausentismo"
        />
        @if (servicioPermisos.tienePermiso('fichaje.registrar')) {
          <app-fila-acceso
            titulo="Historial de fichajes"
            subtitulo="Entradas y salidas"
            icono="time-outline"
            ruta="/tabs/fichaje/historial"
          />
        }
      </div>
    </ion-content>
  `,
  styles: `
    .lista-accesos {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }
  `
})
export class PaginaMiTiempoComponent implements OnInit {
  readonly servicioPermisos = inject(ServicioPermisos);
  private readonly servicioFichajes = inject(ServicioFichajes);
  private readonly servicioVacaciones = inject(ServicioVacaciones);

  readonly estadoFichaje = signal('Marcá tu jornada');
  readonly saldoVacaciones = signal('Consultá tu saldo y pedí días');

  ngOnInit(): void {
    this.servicioVacaciones.saldoPropio().pipe(catchError(() => of(null))).subscribe((saldo) => {
      if (saldo) {
        this.saldoVacaciones.set(`${saldo.diasDisponibles} días disponibles`);
      }
    });

    if (!this.servicioPermisos.tienePermiso('fichaje.registrar')) {
      return;
    }
    this.servicioFichajes.obtenerEstadoDia().pipe(catchError(() => of(null))).subscribe((estado) => {
      if (!estado?.fichajeHabilitado) {
        this.estadoFichaje.set('No habilitado en este momento');
      } else if (estado.ultimaMarcaTipo === 'ENTRADA') {
        this.estadoFichaje.set('En jornada — tocá para salir');
      } else {
        this.estadoFichaje.set('Listo para marcar entrada');
      }
    });
  }
}
