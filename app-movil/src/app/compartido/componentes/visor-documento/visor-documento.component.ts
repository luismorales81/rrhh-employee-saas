import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  inject,
  output,
  signal
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { ServicioArchivos } from '../../../nucleo/servicios/servicio-archivos';

@Component({
  selector: 'app-visor-documento',
  standalone: true,
  template: `
    @if (cargando() || cargandoExterno) {
      <div class="esqueleto-pdf" role="status" [attr.aria-label]="etiquetaCarga">Cargando...</div>
    } @else if (esImagen() && urlImagenSegura()) {
      <img class="visor-documento-imagen" [src]="urlImagenSegura()!" [alt]="titulo" />
    } @else if (urlPdfSegura()) {
      <iframe class="visor-pdf" [src]="urlPdfSegura()!" [title]="titulo"></iframe>
    }
  `,
  styles: `
    .visor-pdf {
      width: 100%;
      height: min(70vh, 520px);
      border: 1px solid var(--ion-color-light-shade, #ddd);
      border-radius: 12px;
      margin: 0.75rem 0;
      background: #fff;
    }
    .visor-documento-imagen {
      display: block;
      width: 100%;
      max-height: min(70vh, 520px);
      object-fit: contain;
      border: 1px solid var(--ion-color-light-shade, #ddd);
      border-radius: 12px;
      margin: 0.75rem 0;
      background: #fff;
    }
    .esqueleto-pdf {
      width: 100%;
      height: min(70vh, 520px);
      border-radius: 12px;
      margin: 0.75rem 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--ion-color-light, #f4f5f8);
      color: var(--ion-color-medium, #92949c);
      font-size: 0.9rem;
    }
  `
})
export class VisorDocumentoComponent implements OnChanges, OnDestroy {
  private readonly sanitizer = inject(DomSanitizer);
  private readonly servicioArchivos = inject(ServicioArchivos);
  private suscripcion?: Subscription;
  private urlBlobActual: string | null = null;

  @Input() urlFuente: string | null = null;
  @Input() blobFuente: Blob | null = null;
  @Input() cargandoExterno = false;
  @Input() titulo = 'Documento';
  @Input() etiquetaCarga = 'Cargando documento';

  readonly errorCarga = output<string>();

  readonly cargando = signal(false);
  readonly esImagen = signal(false);
  readonly urlImagenSegura = signal<SafeUrl | null>(null);
  readonly urlPdfSegura = signal<SafeResourceUrl | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['blobFuente'] || changes['urlFuente']) {
      this.cargar();
    }
  }

  ngOnDestroy(): void {
    this.limpiar();
  }

  private cargar(): void {
    this.limpiar();

    if (this.blobFuente) {
      this.mostrarBlob(this.blobFuente);
      return;
    }

    const url = this.urlFuente?.trim();
    if (!url) {
      return;
    }

    this.cargando.set(true);
    this.suscripcion = this.servicioArchivos.descargarPorUrl(url).subscribe({
      next: (contenido) => {
        this.cargando.set(false);
        this.mostrarBlob(contenido);
      },
      error: () => {
        this.cargando.set(false);
        this.errorCarga.emit('No se pudo cargar el documento');
      }
    });
  }

  private mostrarBlob(blob: Blob): void {
    const url = URL.createObjectURL(blob);
    this.urlBlobActual = url;
    const esImagen = blob.type.startsWith('image/');
    this.esImagen.set(esImagen);
    if (esImagen) {
      this.urlImagenSegura.set(this.sanitizer.bypassSecurityTrustUrl(url));
    } else {
      this.urlPdfSegura.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
    }
  }

  private limpiar(): void {
    this.suscripcion?.unsubscribe();
    this.suscripcion = undefined;
    if (this.urlBlobActual) {
      URL.revokeObjectURL(this.urlBlobActual);
      this.urlBlobActual = null;
    }
    this.urlImagenSegura.set(null);
    this.urlPdfSegura.set(null);
    this.esImagen.set(false);
    this.cargando.set(false);
  }
}
