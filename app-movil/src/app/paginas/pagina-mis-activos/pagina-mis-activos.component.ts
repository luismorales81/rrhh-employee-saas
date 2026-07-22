import { Component, inject, OnInit, signal } from '@angular/core';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import { ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { ServicioActivos, AsignacionActivoResumen } from '../../nucleo/servicios/servicio-activos';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { TarjetaFilaComponent } from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';

@Component({
  selector: 'app-pagina-mis-activos',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Mis equipos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Mis equipos"
        subtitulo="Lo que te asignaron"
      />

      @if (cargando()) {
        <app-estado-carga mensaje="Cargando activos asignados..." />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (asignaciones().length === 0) {
        <app-estado-vacio
          icono="laptop-outline"
          titulo="Sin activos"
          descripcion="No tenés activos asignados actualmente."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (a of asignaciones(); track a.id) {
            <app-tarjeta-fila
              [titulo]="a.nombreActivo"
              [subtitulo]="'Entrega: ' + a.fechaEntrega"
              [etiqueta]="a.fechaDevolucion ? 'Devuelto' : 'En uso'"
              [alerta]="!a.fechaDevolucion"
            />
          }
        </div>
      }
    </ion-content>
  `
})
export class PaginaMisActivosComponent implements OnInit {
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioActivos = inject(ServicioActivos);

  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly asignaciones = signal<AsignacionActivoResumen[]>([]);

  ngOnInit(): void {
    this.servicioEmpleados.obtenerPropio().subscribe({
      next: (empleado) => {
        this.servicioActivos.listarAsignacionesEmpleado(empleado.id).subscribe({
          next: (lista) => {
            this.asignaciones.set(lista.filter((a) => !a.fechaDevolucion));
            this.cargando.set(false);
          },
          error: () => {
            this.mensajeError.set('No se pudieron cargar tus activos.');
            this.cargando.set(false);
          }
        });
      },
      error: () => {
        this.mensajeError.set('No se pudo identificar tu empleado.');
        this.cargando.set(false);
      }
    });
  }
}
