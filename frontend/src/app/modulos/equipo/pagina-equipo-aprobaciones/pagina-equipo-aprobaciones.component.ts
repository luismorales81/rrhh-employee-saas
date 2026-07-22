import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import { ServicioSolicitudes, SolicitudResumen } from '../../../nucleo/servicios/servicio-solicitudes';
import { formatearEstadoSolicitud } from '../../../nucleo/utilidades/formatear-estado-solicitud';
import { CajonComponent, EstadoVacioComponent, InsigniaComponent, MigasComponent, BotonComponent } from '../../../ui';
import { varianteEstado } from '../../../ui/utilidades/etiquetas-estado';

@Component({
  selector: 'app-pagina-equipo-aprobaciones',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    FormsModule,
    DirectivaTienePermiso,
    CajonComponent,
    EstadoVacioComponent,
    InsigniaComponent,
    MigasComponent,
    BotonComponent
  ],
  templateUrl: './pagina-equipo-aprobaciones.component.html'
})
export class PaginaEquipoAprobacionesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly pendientes = signal<SolicitudResumen[]>([]);
  readonly seleccionada = signal<SolicitudResumen | null>(null);
  readonly cajonAbierto = signal(false);

  readonly migas = [
    { etiqueta: 'Mi equipo', ruta: '/equipo' },
    { etiqueta: 'Aprobaciones' }
  ];

  constructor(private readonly servicioSolicitudes: ServicioSolicitudes) {}

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioSolicitudes.listarPendientes().subscribe({
      next: (lista) => {
        this.pendientes.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las aprobaciones');
        this.cargando.set(false);
      }
    });
  }

  abrirDetalle(s: SolicitudResumen): void {
    this.seleccionada.set(s);
    this.cajonAbierto.set(true);
  }

  cerrarCajon(): void {
    this.cajonAbierto.set(false);
    this.seleccionada.set(null);
  }

  aprobar(id: number): void {
    this.servicioSolicitudes.aprobar(id).subscribe({
      next: () => {
        this.cerrarCajon();
        this.recargar();
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo aprobar')
    });
  }

  rechazar(id: number): void {
    this.servicioSolicitudes.rechazar(id).subscribe({
      next: () => {
        this.cerrarCajon();
        this.recargar();
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo rechazar')
    });
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  variante(estado: string) {
    return varianteEstado(estado);
  }

  esVacaciones(s: SolicitudResumen): boolean {
    return s.tipoSolicitudNombre.toLowerCase().includes('vacacion');
  }
}
