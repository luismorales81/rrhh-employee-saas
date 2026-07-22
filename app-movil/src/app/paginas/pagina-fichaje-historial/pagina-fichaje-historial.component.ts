import { Component, computed, inject, OnInit, signal } from '@angular/core';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonText,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import {
  MarcaFichajeResumen,
  ServicioFichajes
} from '../../nucleo/servicios/servicio-fichajes';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { TarjetaFilaComponent } from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { etiquetaEstadoFichaje } from '../../nucleo/utilidades/etiquetas-estado';
import { mensajeDesdeErrorHttp } from '../../nucleo/utilidades/mensajes-error-producto';

@Component({
  selector: 'app-pagina-fichaje-historial',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButtons,
    IonBackButton,
    IonButton,
    IonText,
    EncabezadoPaginaComponent,
    EstadoCargaComponent,
    EstadoVacioComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/tabs/fichaje"></ion-back-button>
        </ion-buttons>
        <ion-title>Historial</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Historial"
        subtitulo="Tus marcas de entrada y salida"
      />

      <div class="filtro-mes">
        <ion-button fill="clear" type="button" aria-label="Mes anterior" (click)="mesAnterior()">
          ‹
        </ion-button>
        <strong>{{ etiquetaMes() }}</strong>
        <ion-button fill="clear" type="button" aria-label="Mes siguiente" (click)="mesSiguiente()">
          ›
        </ion-button>
      </div>

      @if (cargando()) {
        <app-estado-carga modo="lista" mensaje="Cargando historial..." />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
        <ion-button expand="block" fill="outline" type="button" (click)="cargar()">Reintentar</ion-button>
      } @else if (!marcasFiltradas().length) {
        <app-estado-vacio
          icono="time-outline"
          titulo="Sin marcas este mes"
          descripcion="Cuando fiches, van a aparecer acá."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (m of marcasFiltradas(); track m.id) {
            <app-tarjeta-fila
              [titulo]="m.tipo === 'ENTRADA' ? 'Entrada' : 'Salida'"
              [subtitulo]="formatearFecha(m.fechaHoraServidor)"
              [meta]="m.zonaFichajeNombre ?? 'Sin zona'"
              [etiqueta]="chipDe(m.estado).texto"
              [tonoEtiqueta]="chipDe(m.estado).tono"
              [alerta]="m.estado === 'PENDIENTE_REVISION'"
            />
          }
        </div>
      }
    </ion-content>
  `,
  styles: `
    .filtro-mes {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: var(--espacio-4);
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-lg);
      padding: 0.15rem 0.25rem;
    }

    .filtro-mes strong {
      font-size: 0.95rem;
      text-transform: capitalize;
      color: var(--color-texto);
    }

    .filtro-mes ion-button {
      min-width: 44px;
      min-height: 44px;
      font-size: 1.4rem;
      font-weight: 700;
      margin: 0;
    }
  `
})
export class PaginaFichajeHistorialComponent implements OnInit {
  private readonly servicioFichajes = inject(ServicioFichajes);

  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly marcas = signal<MarcaFichajeResumen[]>([]);
  readonly mesReferencia = signal(this.inicioDeMes(new Date()));

  readonly marcasFiltradas = computed(() => {
    const inicio = this.mesReferencia();
    const fin = new Date(inicio.getFullYear(), inicio.getMonth() + 1, 1);
    return this.marcas().filter((m) => {
      const fecha = new Date(m.fechaHoraServidor);
      return fecha >= inicio && fecha < fin;
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  etiquetaMes(): string {
    return this.mesReferencia().toLocaleDateString('es-AR', {
      month: 'long',
      year: 'numeric'
    });
  }

  mesAnterior(): void {
    const actual = this.mesReferencia();
    this.mesReferencia.set(new Date(actual.getFullYear(), actual.getMonth() - 1, 1));
  }

  mesSiguiente(): void {
    const actual = this.mesReferencia();
    this.mesReferencia.set(new Date(actual.getFullYear(), actual.getMonth() + 1, 1));
  }

  chipDe(estado: string) {
    return etiquetaEstadoFichaje(estado);
  }

  formatearFecha(iso: string): string {
    return new Date(iso).toLocaleString('es-AR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);
    this.servicioFichajes.listarMisMarcas().subscribe({
      next: (lista) => {
        this.marcas.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(mensajeDesdeErrorHttp(err, 'cargar el historial'));
        this.cargando.set(false);
      }
    });
  }

  private inicioDeMes(fecha: Date): Date {
    return new Date(fecha.getFullYear(), fecha.getMonth(), 1);
  }
}
