import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ViewWillEnter } from '@ionic/angular';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText,
  IonButton,
  IonSegment,
  IonSegmentButton,
  IonLabel
} from '@ionic/angular/standalone';
import { ReciboResumen, ServicioRecibos, tieneIntegridadVerificable } from '../../nucleo/servicios/servicio-recibos';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { etiquetaEstadoRecibo } from '../../nucleo/utilidades/etiquetas-estado';
import { mensajeErrorGenerico, mensajeDesdeErrorHttp } from '../../nucleo/utilidades/mensajes-error-producto';

type SegmentoRecibos = 'pendientes' | 'firmados';

@Component({
  selector: 'app-pagina-recibos',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    IonButton,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Recibos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Recibos"
        subtitulo="Consultá y firmá tus recibos de sueldo"
      />

      @if (cargando()) {
        <app-estado-carga modo="lista" mensaje="Cargando recibos..." />
      } @else if (mensajeError()) {
        <div class="bloque-error">
          <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
          <ion-button expand="block" fill="outline" type="button" (click)="cargarRecibos()">
            Reintentar
          </ion-button>
        </div>
      } @else {
        <ion-segment [value]="segmento()" (ionChange)="alCambiarSegmento($event)">
          <ion-segment-button value="pendientes">
            <ion-label>Pendientes</ion-label>
          </ion-segment-button>
          <ion-segment-button value="firmados">
            <ion-label>Firmados</ion-label>
          </ion-segment-button>
        </ion-segment>

        @if (recibosFiltrados().length === 0) {
          @if (segmento() === 'pendientes') {
            <app-estado-vacio
              icono="checkmark-done-outline"
              titulo="Nada pendiente"
              descripcion="No tenés recibos para firmar. Cuando publiquen uno nuevo, va a aparecer acá."
            />
          } @else {
            <app-estado-vacio
              icono="cash-outline"
              titulo="Sin firmas todavía"
              descripcion="Los recibos que confirmes o marques con observaciones van a listarse acá."
            />
          }
        } @else {
          <div class="lista-tarjetas">
            @for (recibo of recibosFiltrados(); track recibo.id) {
              <app-tarjeta-fila
                [titulo]="recibo.titulo"
                [subtitulo]="'Período: ' + recibo.periodo"
                [etiqueta]="chipDe(recibo.estado).texto"
                [tonoEtiqueta]="chipDe(recibo.estado).tono"
                [alerta]="esPendienteFirma(recibo.estado)"
                [acciones]="accionesPara(recibo)"
              />
            }
          </div>
        }
      }
    </ion-content>
  `,
  styles: `
    .bloque-error {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
      padding: var(--espacio-2) 0;
    }

    .bloque-error p {
      margin: 0;
    }

    ion-segment {
      margin-bottom: var(--espacio-4, 1rem);
    }
  `
})
export class PaginaRecibosComponent implements OnInit, ViewWillEnter {
  private readonly servicioRecibos = inject(ServicioRecibos);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly router = inject(Router);

  readonly recibos = signal<ReciboResumen[]>([]);
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly segmento = signal<SegmentoRecibos>('pendientes');

  readonly recibosFiltrados = computed(() => {
    const lista = this.recibos();
    if (this.segmento() === 'pendientes') {
      return lista.filter((r) => r.estado === 'PENDIENTE' || r.estado === 'VISTO');
    }
    return lista.filter((r) => r.estado === 'FIRMADO' || r.estado === 'DISCONFORMIDAD');
  });

  ngOnInit(): void {
    this.cargarRecibos();
  }

  ionViewWillEnter(): void {
    if (!this.cargando()) {
      this.cargarRecibos();
    }
  }

  alCambiarSegmento(evento: CustomEvent): void {
    const valor = evento.detail.value as SegmentoRecibos;
    if (valor === 'pendientes' || valor === 'firmados') {
      this.segmento.set(valor);
    }
  }

  chipDe(estado: string) {
    return etiquetaEstadoRecibo(estado);
  }

  esPendienteFirma(estado: string): boolean {
    return estado === 'PENDIENTE' || estado === 'VISTO';
  }

  accionesPara(recibo: ReciboResumen): AccionTarjetaFila[] {
    const pendienteFirma = this.esPendienteFirma(recibo.estado);
    const acciones: AccionTarjetaFila[] = [{
      id: `ver-${recibo.id}`,
      etiqueta: !pendienteFirma
        ? 'Ver'
        : (tieneIntegridadVerificable(recibo)
          ? 'Ver y firmar'
          : 'Ver detalle'),
      primaria: true,
      ejecutar: () => void this.router.navigate(['/tabs/recibos', recibo.id])
    }];

    if (recibo.estado === 'FIRMADO' || recibo.estado === 'DISCONFORMIDAD') {
      acciones.push({
        id: `evidencia-${recibo.id}`,
        etiqueta: 'Descargar evidencia',
        enMenu: true,
        ejecutar: () => this.descargarComprobante(recibo.id, recibo.periodo)
      });
    }

    return acciones;
  }

  cargarRecibos(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);
    this.servicioRecibos.listarPropios().subscribe({
      next: (lista) => {
        this.recibos.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(mensajeDesdeErrorHttp(err, 'cargar tus recibos'));
        this.cargando.set(false);
      }
    });
  }

  private descargarComprobante(id: number, periodo: string): void {
    this.servicioRecibos.descargarComprobante(id).subscribe({
      next: (blob) => {
        const esPdf = blob.type.includes('pdf');
        const extension = esPdf ? 'pdf' : 'json';
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `evidencia-${periodo}.${extension}`;
        enlace.click();
        URL.revokeObjectURL(url);
        if (!esPdf) {
          void this.servicioMensajes.info(
            'Descargaste la evidencia digital del recibo. Tu equipo puede pedirte este archivo.'
          );
        }
      },
      error: () => this.mensajeError.set(mensajeErrorGenerico('descargar la evidencia'))
    });
  }
}
