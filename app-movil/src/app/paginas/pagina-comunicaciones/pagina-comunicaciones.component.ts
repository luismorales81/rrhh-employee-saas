import { Component, inject, OnInit, signal } from '@angular/core';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import {
  DestinatarioComunicacionResumen,
  ServicioComunicaciones
} from '../../nucleo/servicios/servicio-comunicaciones';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';

@Component({
  selector: 'app-pagina-comunicaciones',
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
        <ion-title>Avisos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Avisos"
        subtitulo="Novedades y mensajes de tu empresa"
      />

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (comunicaciones().length === 0) {
        <app-estado-vacio
          icono="megaphone-outline"
          titulo="Sin avisos"
          descripcion="Cuando haya un aviso nuevo, lo vas a ver acá."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (c of comunicaciones(); track c.id) {
            <app-tarjeta-fila
              [titulo]="c.tituloComunicacion"
              [subtitulo]="c.mensaje"
              [meta]="etiquetaEstado(c)"
              [etiqueta]="c.leida ? 'Leída' : 'Nueva'"
              [alerta]="!c.leida"
              [acciones]="accionesPara(c)"
            />
          }
        </div>
      }
    </ion-content>
  `,
})
export class PaginaComunicacionesComponent implements OnInit {
  private readonly servicioComunicaciones = inject(ServicioComunicaciones);
  private readonly servicioMensajes = inject(ServicioMensajes);

  readonly comunicaciones = signal<DestinatarioComunicacionResumen[]>([]);
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  confirmarLectura(comunicacion: DestinatarioComunicacionResumen): void {
    this.servicioComunicaciones.confirmarLectura(comunicacion.id).subscribe({
      next: () => {
        void this.servicioMensajes.exito('Comunicación confirmada.');
        this.cargar();
      },
      error: () => {
        this.mensajeError.set('No se pudo confirmar la lectura.');
        void this.servicioMensajes.error('No se pudo confirmar la lectura.');
      }
    });
  }

  accionesPara(comunicacion: DestinatarioComunicacionResumen): AccionTarjetaFila[] {
    if (!this.puedeConfirmar(comunicacion)) return [];
    return [{
      id: `confirmar-${comunicacion.id}`,
      etiqueta: 'Confirmar',
      ejecutar: () => this.confirmarLectura(comunicacion)
    }];
  }

  puedeConfirmar(comunicacion: DestinatarioComunicacionResumen): boolean {
    if (comunicacion.requiereConfirmacion) {
      return !comunicacion.confirmada;
    }
    return !comunicacion.leida;
  }

  etiquetaEstado(comunicacion: DestinatarioComunicacionResumen): string {
    if (comunicacion.requiereConfirmacion) {
      return comunicacion.confirmada ? 'Confirmada' : 'Pendiente de confirmación';
    }
    return comunicacion.leida ? 'Leída' : 'Sin leer';
  }

  private cargar(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);
    this.servicioComunicaciones.listarMisComunicaciones().subscribe({
      next: (lista) => {
        this.comunicaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar los avisos.');
        this.cargando.set(false);
      }
    });
  }
}
