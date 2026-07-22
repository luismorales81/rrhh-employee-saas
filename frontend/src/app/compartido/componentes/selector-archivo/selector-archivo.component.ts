import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { ArchivoSubidoResumen, ServicioArchivos } from '../../../nucleo/servicios/servicio-archivos';

@Component({
  selector: 'app-selector-archivo',
  standalone: true,
  template: `
    <div class="selector-archivo">
      <input type="file" (change)="alSeleccionar($event)" [accept]="aceptar" />
      @if (subiendo()) {
        <span>Subiendo archivo...</span>
      }
      @if (nombreArchivo()) {
        <span class="archivo-seleccionado">{{ nombreArchivo() }}</span>
      }
      @if (error()) {
        <span class="error-archivo">{{ error() }}</span>
      }
    </div>
  `,
  styles: `
    .selector-archivo { display: flex; flex-direction: column; gap: 0.25rem; }
    .archivo-seleccionado { font-size: 0.85rem; color: #2e7d32; }
    .error-archivo { font-size: 0.85rem; color: #c62828; }
  `
})
export class SelectorArchivoComponent {
  @Input() aceptar = '.pdf,image/*';
  @Output() urlArchivo = new EventEmitter<string>();
  @Output() archivoCargado = new EventEmitter<ArchivoSubidoResumen>();

  readonly subiendo = signal(false);
  readonly nombreArchivo = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  constructor(private readonly servicioArchivos: ServicioArchivos) {}

  alSeleccionar(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;

    this.subiendo.set(true);
    this.error.set(null);
    this.servicioArchivos.subir(archivo).subscribe({
      next: (respuesta) => {
        this.subiendo.set(false);
        this.nombreArchivo.set(respuesta.nombreOriginal);
        this.urlArchivo.emit(this.servicioArchivos.urlContenido(respuesta.id));
        this.archivoCargado.emit(respuesta);
      },
      error: (err) => {
        this.subiendo.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo subir el archivo');
      }
    });
  }
}
