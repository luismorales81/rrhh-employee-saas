import { Component, ElementRef, EventEmitter, Output, ViewChild, AfterViewInit } from '@angular/core';

@Component({
  selector: 'app-canvas-firma',
  standalone: true,
  template: `
    <div class="contenedor-firma">
      <canvas #lienzo width="320" height="160" (pointerdown)="iniciarTrazo($event)" (pointermove)="dibujar($event)" (pointerup)="finalizarTrazo()" (pointerleave)="finalizarTrazo()"></canvas>
      <div class="acciones-firma">
        <button type="button" (click)="limpiar()">Limpiar</button>
      </div>
    </div>
  `,
  styles: `
    .contenedor-firma { border: 1px solid #ccc; border-radius: 6px; padding: 0.5rem; background: #fff; }
    canvas { display: block; width: 100%; touch-action: none; cursor: crosshair; }
    .acciones-firma { margin-top: 0.5rem; }
  `
})
export class CanvasFirmaComponent implements AfterViewInit {
  @ViewChild('lienzo') lienzoRef!: ElementRef<HTMLCanvasElement>;
  @Output() firmaCapturada = new EventEmitter<string>();

  private contexto?: CanvasRenderingContext2D;
  private dibujando = false;
  private tieneTrazo = false;

  ngAfterViewInit(): void {
    const canvas = this.lienzoRef.nativeElement;
    this.contexto = canvas.getContext('2d') ?? undefined;
    if (this.contexto) {
      this.contexto.strokeStyle = '#111';
      this.contexto.lineWidth = 2;
      this.contexto.lineCap = 'round';
    }
  }

  iniciarTrazo(evento: PointerEvent): void {
    if (!this.contexto) return;
    this.dibujando = true;
    this.contexto.beginPath();
    this.contexto.moveTo(evento.offsetX, evento.offsetY);
  }

  dibujar(evento: PointerEvent): void {
    if (!this.dibujando || !this.contexto) return;
    this.contexto.lineTo(evento.offsetX, evento.offsetY);
    this.contexto.stroke();
    this.tieneTrazo = true;
  }

  finalizarTrazo(): void {
    this.dibujando = false;
  }

  limpiar(): void {
    const canvas = this.lienzoRef.nativeElement;
    this.contexto?.clearRect(0, 0, canvas.width, canvas.height);
    this.tieneTrazo = false;
  }

  obtenerImagenBase64(): string | null {
    if (!this.tieneTrazo) return null;
    return this.lienzoRef.nativeElement.toDataURL('image/png');
  }

  emitirFirma(): boolean {
    const imagen = this.obtenerImagenBase64();
    if (!imagen) return false;
    this.firmaCapturada.emit(imagen);
    return true;
  }
}
