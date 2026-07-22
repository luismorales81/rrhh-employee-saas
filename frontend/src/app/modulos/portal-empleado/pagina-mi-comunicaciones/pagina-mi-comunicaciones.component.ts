import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  DestinatarioComunicacionResumen,
  ServicioComunicaciones
} from '../../../nucleo/servicios/servicio-comunicaciones';

@Component({
  selector: 'app-pagina-mi-comunicaciones',
  standalone: true,
  imports: [DatePipe, EsqueletoCargaComponent],
  templateUrl: './pagina-mi-comunicaciones.component.html'
})
export class PaginaMiComunicacionesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly comunicaciones = signal<DestinatarioComunicacionResumen[]>([]);

  constructor(private readonly servicioComunicaciones: ServicioComunicaciones) {}

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioComunicaciones.listarMisComunicaciones().subscribe({
      next: (lista) => {
        this.comunicaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus comunicaciones');
        this.cargando.set(false);
      }
    });
  }

  confirmarLectura(id: number): void {
    this.servicioComunicaciones.confirmarLectura(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo confirmar la lectura')
    });
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
}
