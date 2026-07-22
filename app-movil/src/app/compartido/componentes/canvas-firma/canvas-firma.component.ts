import { Component, ElementRef, EventEmitter, Output, ViewChild, AfterViewInit } from '@angular/core';
import { IonButton } from '@ionic/angular/standalone';

@Component({
  selector: 'app-canvas-firma',
  standalone: true,
  imports: [IonButton],
  template: `
    <div class="contenedor-firma">
      <p class="pista-firma">Firmá con el dedo dentro del recuadro</p>
      <canvas
        #lienzo
        width="800"
        height="400"
        (pointerdown)="iniciarTrazo($event)"
        (pointermove)="dibujar($event)"
        (pointerup)="finalizarTrazo($event)"
        (pointerleave)="finalizarTrazo($event)"
        (pointercancel)="finalizarTrazo($event)"
      ></canvas>
      <ion-button
        class="boton-limpiar"
        size="small"
        fill="outline"
        type="button"
        (click)="limpiar()"
      >
        Limpiar
      </ion-button>
    </div>
  `,
  styles: `
    .contenedor-firma {
      border: 2px dashed #94a3b8;
      border-radius: 10px;
      padding: 0.75rem;
      background: #fff;
    }

    .pista-firma {
      margin: 0 0 0.5rem;
      font-size: 0.875rem;
      color: var(--ion-color-medium, #64748b);
      text-align: center;
    }

    canvas {
      display: block;
      width: 100%;
      min-height: 220px;
      height: 220px;
      touch-action: none;
      cursor: crosshair;
      border-radius: 6px;
      background: #fafafa;
      border: 1px dashed #cbd5e1;
    }

    .boton-limpiar {
      margin-top: 0.5rem;
      min-height: 44px;
    }
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
      this.contexto.lineWidth = 2.5;
      this.contexto.lineCap = 'round';
      this.contexto.lineJoin = 'round';
    }
  }

  iniciarTrazo(evento: PointerEvent): void {
    if (!this.contexto) return;
    evento.preventDefault();
    this.lienzoRef.nativeElement.setPointerCapture(evento.pointerId);
    this.dibujando = true;
    const punto = this.obtenerPunto(evento);
    this.contexto.beginPath();
    this.contexto.moveTo(punto.x, punto.y);
  }

  dibujar(evento: PointerEvent): void {
    if (!this.dibujando || !this.contexto) return;
    evento.preventDefault();
    const punto = this.obtenerPunto(evento);
    this.contexto.lineTo(punto.x, punto.y);
    this.contexto.stroke();
    this.tieneTrazo = true;
  }

  finalizarTrazo(evento: PointerEvent): void {
    if (!this.dibujando) return;
    evento.preventDefault();
    this.dibujando = false;
    try {
      this.lienzoRef.nativeElement.releasePointerCapture(evento.pointerId);
    } catch {
      // El puntero ya fue liberado.
    }
  }

  limpiar(): void {
    const canvas = this.lienzoRef.nativeElement;
    this.contexto?.clearRect(0, 0, canvas.width, canvas.height);
    this.tieneTrazo = false;
  }

  emitirFirma(): boolean {
    if (!this.tieneTrazo) return false;
    this.firmaCapturada.emit(this.lienzoRef.nativeElement.toDataURL('image/png'));
    return true;
  }

  private obtenerPunto(evento: PointerEvent): { x: number; y: number } {
    const canvas = this.lienzoRef.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const escalaX = canvas.width / rect.width;
    const escalaY = canvas.height / rect.height;
    return {
      x: (evento.clientX - rect.left) * escalaX,
      y: (evento.clientY - rect.top) * escalaY
    };
  }
}
