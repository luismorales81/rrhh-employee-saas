import { Component, computed, inject, OnInit, signal, ViewChild } from '@angular/core';
import { ViewWillEnter } from '@ionic/angular';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonButton,
  IonText,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle
} from '@ionic/angular/standalone';
import {
  AsignacionDocumentoResumen,
  ServicioDocumentos
} from '../../nucleo/servicios/servicio-documentos';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { CanvasFirmaComponent } from '../../compartido/componentes/canvas-firma/canvas-firma.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { VisorDocumentoComponent } from '../../compartido/componentes/visor-documento/visor-documento.component';
import { PanelExitoComponent } from '../../compartido/componentes/panel-exito/panel-exito.component';
import { etiquetaEstadoDocumento } from '../../nucleo/utilidades/etiquetas-estado';

@Component({
  selector: 'app-pagina-documentos',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButton,
    IonText,
    IonCard,
    IonCardContent,
    IonCardHeader,
    IonCardTitle,
    CanvasFirmaComponent,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent,
    VisorDocumentoComponent,
    PanelExitoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Documentos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Tus documentos"
        subtitulo="Leé y firmá lo que te asignen"
      />

      @if (exitoFirma()) {
        <app-panel-exito
          titulo="Documento firmado"
          mensaje="Tu firma quedó registrada correctamente."
          textoAccion="Seguir con documentos"
          (accion)="cerrarExitoFirma()"
        />
      }

      @if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      }

      @if (documentoEnVista(); as documento) {
        <ion-card class="tarjeta-visor">
          <ion-card-header>
            <ion-card-title>{{ documento.tituloDocumento }}</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <app-visor-documento
              [urlFuente]="documento.urlArchivo!"
              [titulo]="documento.tituloDocumento"
              etiquetaCarga="Cargando documento"
              (errorCarga)="mensajeError.set($event)"
            />
            <ion-button expand="block" fill="clear" type="button" (click)="cerrarDocumento()">
              Cerrar
            </ion-button>
          </ion-card-content>
        </ion-card>
      }

      @if (asignacionFirmandoId()) {
        <ion-card class="tarjeta-firma">
          <ion-card-header>
            <ion-card-title>Firmar documento</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <app-canvas-firma (firmaCapturada)="enviarFirma($event)" />
            <ion-button expand="block" type="button" (click)="confirmarFirma()">Confirmar firma</ion-button>
            <ion-button expand="block" fill="clear" type="button" (click)="cancelarFirma()">Cancelar</ion-button>
          </ion-card-content>
        </ion-card>
      }

      @if (cargando() && asignaciones().length === 0) {
        <app-estado-carga mensaje="Cargando documentos..." />
      } @else if (asignaciones().length === 0) {
        <app-estado-vacio
          icono="folder-outline"
          titulo="Sin documentos"
          descripcion="Cuando te asignen documentos, aparecerán acá para leer o firmar."
        />
      } @else if (!exitoFirma()) {
        <div class="lista-tarjetas">
          @for (asignacion of asignacionesOrdenadas(); track asignacion.id) {
            <app-tarjeta-fila
              [titulo]="asignacion.tituloDocumento"
              [subtitulo]="subtituloHumano(asignacion)"
              [etiqueta]="chipDe(asignacion).texto"
              [tonoEtiqueta]="chipDe(asignacion).tono"
              [alerta]="asignacion.requiereFirma && !asignacion.firmado"
              [acciones]="accionesPara(asignacion)"
            />
          }
        </div>
      }
    </ion-content>
  `,
  styles: `
    .tarjeta-firma, .tarjeta-visor { margin-bottom: 1rem; border-radius: 14px; }
  `
})
export class PaginaDocumentosComponent implements OnInit, ViewWillEnter {
  private readonly servicioDocumentos = inject(ServicioDocumentos);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private toastLecturaMostrado = false;

  readonly asignaciones = signal<AsignacionDocumentoResumen[]>([]);
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly asignacionFirmandoId = signal<number | null>(null);
  readonly documentoEnVista = signal<AsignacionDocumentoResumen | null>(null);
  readonly firmando = signal(false);
  readonly exitoFirma = signal(false);

  readonly asignacionesOrdenadas = computed(() =>
    [...this.asignaciones()].sort((a, b) => this.pesoPendiente(a) - this.pesoPendiente(b))
  );

  @ViewChild(CanvasFirmaComponent) canvasFirma?: CanvasFirmaComponent;

  ngOnInit(): void {
    this.cargarAsignaciones();
  }

  ionViewWillEnter(): void {
    if (!this.asignacionFirmandoId() && !this.documentoEnVista() && !this.exitoFirma()) {
      this.cargarAsignaciones(true);
    }
  }

  chipDe(asignacion: AsignacionDocumentoResumen) {
    return etiquetaEstadoDocumento(asignacion.estado);
  }

  subtituloHumano(asignacion: AsignacionDocumentoResumen): string {
    if (asignacion.firmado) return 'Ya firmaste este documento';
    if (asignacion.requiereFirma && !asignacion.firmado) return 'Requiere tu firma';
    if (asignacion.estado === 'LEIDO') return 'Ya lo revisaste';
    return 'Pendiente de lectura';
  }

  accionesPara(asignacion: AsignacionDocumentoResumen): AccionTarjetaFila[] {
    const acciones: AccionTarjetaFila[] = [];

    if (asignacion.requiereFirma && !asignacion.firmado) {
      acciones.push({
        id: `firmar-${asignacion.id}`,
        etiqueta: 'Firmar',
        primaria: true,
        ejecutar: () => this.iniciarFirma(asignacion)
      });
    }

    if (asignacion.urlArchivo) {
      acciones.push({
        id: `ver-${asignacion.id}`,
        etiqueta: 'Ver PDF',
        primaria: !(asignacion.requiereFirma && !asignacion.firmado),
        enMenu: !!(asignacion.requiereFirma && !asignacion.firmado),
        relleno: 'outline',
        ejecutar: () => this.abrirDocumento(asignacion)
      });
    }

    return acciones;
  }

  abrirDocumento(asignacion: AsignacionDocumentoResumen): void {
    this.mensajeError.set(null);
    this.documentoEnVista.set(asignacion);
    this.marcarLeidoSiCorresponde(asignacion);
  }

  cerrarDocumento(): void {
    this.documentoEnVista.set(null);
  }

  iniciarFirma(asignacion: AsignacionDocumentoResumen): void {
    this.mensajeError.set(null);
    this.asignacionFirmandoId.set(asignacion.id);
    if (asignacion.urlArchivo) {
      this.documentoEnVista.set(asignacion);
      this.marcarLeidoSiCorresponde(asignacion);
    }
  }

  cancelarFirma(): void {
    this.asignacionFirmandoId.set(null);
  }

  confirmarFirma(): void {
    if (!this.canvasFirma?.emitirFirma()) {
      const mensaje = 'Dibujá tu firma antes de confirmar.';
      this.mensajeError.set(mensaje);
      void this.servicioMensajes.error(mensaje);
    }
  }

  enviarFirma(imagenBase64: string): void {
    const id = this.asignacionFirmandoId();
    if (!id || this.firmando()) return;

    this.firmando.set(true);
    this.servicioDocumentos.firmar(id, {
      imagenFirmaBase64: imagenBase64,
      dispositivo: navigator.userAgent
    }).subscribe({
      next: () => {
        this.firmando.set(false);
        this.asignacionFirmandoId.set(null);
        this.documentoEnVista.set(null);
        this.exitoFirma.set(true);
        this.servicioAnalitica.documentoFirmado(id);
        this.cargarAsignaciones(true);
      },
      error: (err) => {
        this.firmando.set(false);
        const mensaje = err.error?.mensaje ?? 'No se pudo firmar el documento.';
        this.mensajeError.set(mensaje);
        void this.servicioMensajes.error(mensaje);
      }
    });
  }

  cerrarExitoFirma(): void {
    this.exitoFirma.set(false);
  }

  private marcarLeidoSiCorresponde(asignacion: AsignacionDocumentoResumen): void {
    if (asignacion.estado === 'LEIDO' || asignacion.estado === 'FIRMADO') {
      return;
    }
    this.servicioDocumentos.confirmarLectura(asignacion.id).subscribe({
      next: () => {
        if (!this.toastLecturaMostrado) {
          this.toastLecturaMostrado = true;
          void this.servicioMensajes.info('Documento marcado como leído');
        }
        this.cargarAsignaciones(true);
      },
      error: () => {
        // Lectura silenciosa: no molestar si falla.
      }
    });
  }

  private pesoPendiente(asignacion: AsignacionDocumentoResumen): number {
    if (asignacion.requiereFirma && !asignacion.firmado) return 0;
    if (asignacion.estado !== 'LEIDO' && asignacion.estado !== 'FIRMADO') return 1;
    return 2;
  }

  private cargarAsignaciones(silencioso = false): void {
    if (!silencioso) {
      this.cargando.set(true);
    }
    this.mensajeError.set(null);

    this.servicioDocumentos.listarMisAsignaciones().subscribe({
      next: (lista) => {
        this.asignaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar los documentos.');
        this.cargando.set(false);
      }
    });
  }
}
