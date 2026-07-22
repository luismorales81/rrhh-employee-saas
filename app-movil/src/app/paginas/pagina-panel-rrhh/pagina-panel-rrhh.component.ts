import { Component, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import { ServicioAutenticacion } from '../../nucleo/servicios/servicio-autenticacion';
import { ServicioSolicitudes } from '../../nucleo/servicios/servicio-solicitudes';
import { ServicioNovedades } from '../../nucleo/servicios/servicio-novedades';
import { TarjetaMetricaComponent } from '../../compartido/componentes/tarjeta-metrica/tarjeta-metrica.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';

@Component({
  selector: 'app-pagina-panel-rrhh',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    TarjetaMetricaComponent,
    EstadoCargaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Inicio</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding contenido-panel">
      <div class="encabezado-saludo">
        <h1>Hola, equipo</h1>
        <p>{{ emailUsuario() }}</p>
      </div>

      @if (cargando()) {
        <app-estado-carga modo="esqueleto" />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else {
        <div class="grilla-metricas">
          <app-tarjeta-metrica
            titulo="Solicitudes pendientes"
            [valor]="solicitudesPendientes()"
            [alerta]="solicitudesPendientes() > 0"
            icono="document-text-outline"
            ruta="/tabs-rrhh/solicitudes"
          />
          <app-tarjeta-metrica
            titulo="Novedades en borrador"
            [valor]="novedadesBorrador()"
            [alerta]="novedadesBorrador() > 0"
            icono="stats-chart-outline"
            ruta="/tabs-rrhh/novedades"
          />
          <app-tarjeta-metrica
            titulo="Activos"
            valor="Gestionar"
            icono="laptop-outline"
            ruta="/tabs-rrhh/activos"
          />
        </div>
      }
    </ion-content>
  `
})
export class PaginaPanelRrhhComponent implements OnInit {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  private readonly servicioSolicitudes = inject(ServicioSolicitudes);
  private readonly servicioNovedades = inject(ServicioNovedades);

  readonly emailUsuario = signal(this.servicioAutenticacion.sesionActual()?.email ?? '');
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly solicitudesPendientes = signal(0);
  readonly novedadesBorrador = signal(0);

  ngOnInit(): void {
    const periodo = new Date().toISOString().slice(0, 7);
    forkJoin({
      solicitudes: this.servicioSolicitudes.listarPendientes(),
      novedades: this.servicioNovedades.listar()
    }).subscribe({
      next: ({ solicitudes, novedades }) => {
        this.solicitudesPendientes.set(solicitudes.length);
        this.novedadesBorrador.set(
          novedades.filter((n) => n.estado === 'BORRADOR' && n.periodo === periodo).length
        );
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar el inicio.');
        this.cargando.set(false);
      }
    });
  }
}
