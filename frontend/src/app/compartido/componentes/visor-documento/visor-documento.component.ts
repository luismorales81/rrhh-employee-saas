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

/**
 * Visor integrado de PDF e imágenes: descarga con autenticación (blob) y muestra
 * el contenido dentro de la app, sin abrir otra pestaña.
 */
@Component({
  selector: 'app-visor-documento',
  standalone: true,
  templateUrl: './visor-documento.component.html',
  styleUrl: './visor-documento.component.scss'
})
export class VisorDocumentoComponent implements OnChanges, OnDestroy {
  private readonly sanitizer = inject(DomSanitizer);
  private readonly servicioArchivos = inject(ServicioArchivos);
  private suscripcion?: Subscription;
  private urlBlobActual: string | null = null;

  /** URL del API (absoluta o relativa) para descargar con el token de sesión. */
  @Input() urlFuente: string | null = null;
  /** Blob ya obtenido (p. ej. PDF de recibo). Tiene prioridad sobre urlFuente. */
  @Input() blobFuente: Blob | null = null;
  /** Muestra el esqueleto mientras el padre aún descarga el blob. */
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
