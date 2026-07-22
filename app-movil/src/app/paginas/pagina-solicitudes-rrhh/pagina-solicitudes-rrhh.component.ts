import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import { ServicioSolicitudes, SolicitudResumen } from '../../nucleo/servicios/servicio-solicitudes';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { formatearEstadoSolicitud } from '../../nucleo/utilidades/formatear-estado-solicitud';

@Component({
  selector: 'app-pagina-solicitudes-rrhh',
  standalone: true,
  imports: [
    DatePipe,
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
      <ion-toolbar color="primary">
        <ion-title>Solicitudes</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Aprobaciones"
        subtitulo="Solicitudes pendientes de tu equipo"
      />

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (solicitudes().length === 0) {
        <app-estado-vacio
          icono="document-text-outline"
          titulo="Sin solicitudes pendientes"
          descripcion="No hay solicitudes esperando aprobación."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (s of solicitudes(); track s.id) {
            <app-tarjeta-fila
              [titulo]="s.tipoSolicitudNombre + ' — ' + (s.empleadoNombre || 'Empleado')"
              [meta]="(s.createdAt | date:'dd/MM/yyyy') ?? ''"
              [etiqueta]="etiquetaEstado(s.estado)"
              [alerta]="true"
              [acciones]="accionesPara(s)"
            />
          }
        </div>
      }
    </ion-content>
  `,
})
export class PaginaSolicitudesRrhhComponent implements OnInit {
  private readonly servicioSolicitudes = inject(ServicioSolicitudes);
  private readonly servicioMensajes = inject(ServicioMensajes);

  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly solicitudes = signal<SolicitudResumen[]>([]);

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioSolicitudes.listarPendientes().subscribe({
      next: (lista) => {
        this.solicitudes.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las solicitudes.');
        this.cargando.set(false);
      }
    });
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  accionesPara(solicitud: SolicitudResumen): AccionTarjetaFila[] {
    return [
      {
        id: `aprobar-${solicitud.id}`,
        etiqueta: 'Aprobar',
        color: 'success',
        ejecutar: () => this.aprobar(solicitud.id)
      },
      {
        id: `rechazar-${solicitud.id}`,
        etiqueta: 'Rechazar',
        color: 'danger',
        ejecutar: () => this.rechazar(solicitud.id)
      }
    ];
  }

  aprobar(id: number): void {
    this.servicioSolicitudes.aprobar(id).subscribe({
      next: () => {
        void this.servicioMensajes.exito('Solicitud aprobada.');
        this.recargar();
      },
      error: () => {
        this.mensajeError.set('No se pudo aprobar la solicitud.');
        void this.servicioMensajes.error('No se pudo aprobar la solicitud.');
      }
    });
  }

  rechazar(id: number): void {
    this.servicioSolicitudes.rechazar(id).subscribe({
      next: () => {
        void this.servicioMensajes.exito('Solicitud rechazada.');
        this.recargar();
      },
      error: () => {
        this.mensajeError.set('No se pudo rechazar la solicitud.');
        void this.servicioMensajes.error('No se pudo rechazar la solicitud.');
      }
    });
  }
}
