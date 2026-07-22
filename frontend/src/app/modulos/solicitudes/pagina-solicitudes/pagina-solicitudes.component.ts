import { Component, OnInit, computed, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import {
  ServicioSolicitudes,
  SolicitudResumen,
  TipoSolicitudResumen
} from '../../../nucleo/servicios/servicio-solicitudes';
import { formatearEstadoSolicitud } from '../../../nucleo/utilidades/formatear-estado-solicitud';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import {
  BotonComponent,
  CajonComponent,
  EstadoVacioComponent,
  InsigniaComponent,
  MigasComponent
} from '../../../ui';
import { varianteEstado } from '../../../ui/utilidades/etiquetas-estado';

type PestanaSolicitudes = 'pendientes' | 'todas';

@Component({
  selector: 'app-pagina-solicitudes',
  standalone: true,
  imports: [
    DatePipe,
    DirectivaTienePermiso,
    RouterLink,
    FormsModule,
    BotonComponent,
    CajonComponent,
    EstadoVacioComponent,
    InsigniaComponent,
    MigasComponent
  ],
  templateUrl: './pagina-solicitudes.component.html'
})
export class PaginaSolicitudesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly pestanaActiva = signal<PestanaSolicitudes>('pendientes');
  readonly pendientes = signal<SolicitudResumen[]>([]);
  readonly todas = signal<SolicitudResumen[]>([]);
  readonly tipos = signal<TipoSolicitudResumen[]>([]);
  readonly filtroTipoId = signal('');
  readonly filtroEstado = signal('');
  readonly filtroTexto = signal('');
  readonly seleccionada = signal<SolicitudResumen | null>(null);
  readonly cajonAbierto = signal(false);
  readonly migas = [
    { etiqueta: 'Tiempo', ruta: '/solicitudes' },
    { etiqueta: 'Solicitudes' }
  ];

  readonly solicitudesFiltradas = computed(() => {
    const lista = this.pestanaActiva() === 'pendientes' ? this.pendientes() : this.todas();
    const tipoId = this.filtroTipoId();
    const estado = this.filtroEstado();
    const texto = this.filtroTexto().trim().toLowerCase();

    return lista.filter((s) => {
      if (tipoId && String(s.tipoSolicitudId) !== tipoId) {
        return false;
      }
      if (estado && s.estado !== estado) {
        return false;
      }
      if (texto) {
        const nombre = (s.empleadoNombre ?? '').toLowerCase();
        const tipo = s.tipoSolicitudNombre.toLowerCase();
        const motivo = (s.motivo ?? '').toLowerCase();
        if (!nombre.includes(texto) && !tipo.includes(texto) && !motivo.includes(texto)) {
          return false;
        }
      }
      return true;
    });
  });

  readonly estadosDisponibles = computed(() => {
    const lista = this.pestanaActiva() === 'pendientes' ? this.pendientes() : this.todas();
    return [...new Set(lista.map((s) => s.estado))].sort();
  });

  constructor(
    private readonly servicioSolicitudes: ServicioSolicitudes,
    private readonly servicioPermisos: ServicioPermisos
  ) {}

  ngOnInit(): void {
    this.servicioSolicitudes.listarTipos().subscribe({
      next: (lista) => this.tipos.set(lista)
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.servicioSolicitudes.listarPendientes().subscribe({
      next: (lista) => this.pendientes.set(lista),
      error: () => this.error.set('No se pudieron cargar las solicitudes pendientes')
    });

    this.servicioSolicitudes.listar().subscribe({
      next: (lista) => {
        this.todas.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las solicitudes');
        this.cargando.set(false);
      }
    });
  }

  cambiarPestana(pestana: PestanaSolicitudes): void {
    this.pestanaActiva.set(pestana);
    this.filtroTipoId.set('');
    this.filtroEstado.set('');
    this.filtroTexto.set('');
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

  cancelar(id: number): void {
    this.servicioSolicitudes.cancelar(id).subscribe({
      next: () => {
        this.cerrarCajon();
        this.recargar();
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo dar de baja la solicitud')
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

  variante(estado: string) {
    return varianteEstado(estado);
  }

  puedeAprobar(estado: string): boolean {
    return estado === 'EN_REVISION';
  }

  detalleSolicitud(solicitud: SolicitudResumen): string {
    if (solicitud.fechaInicioVacaciones && solicitud.fechaFinVacaciones) {
      return `Vacaciones: ${solicitud.fechaInicioVacaciones} → ${solicitud.fechaFinVacaciones}`;
    }
    if (this.esVacacionesIncompleta(solicitud)) {
      return 'Pedido incompleto (sin fechas ni registro en Vacaciones)';
    }
    return solicitud.motivo ?? '—';
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  nombreEmpleado(solicitud: SolicitudResumen): string {
    return solicitud.empleadoNombre ?? 'Empleado';
  }

  esVacaciones(solicitud: SolicitudResumen): boolean {
    const nombre = solicitud.tipoSolicitudNombre.toLowerCase();
    return nombre.includes('vacacion');
  }

  tieneDetalleVacaciones(solicitud: SolicitudResumen): boolean {
    return solicitud.solicitudVacacionesId != null;
  }

  esVacacionesIncompleta(solicitud: SolicitudResumen): boolean {
    return this.esVacaciones(solicitud) && !this.tieneDetalleVacaciones(solicitud);
  }

  puedeCancelar(estado: string): boolean {
    return estado === 'BORRADOR'
      || estado === 'EN_REVISION'
      || estado === 'CAMBIOS_SOLICITADOS';
  }

  puedeDarDeBajaSolicitud(solicitud: SolicitudResumen): boolean {
    return this.esVacacionesIncompleta(solicitud)
      && this.puedeCancelar(solicitud.estado)
      && this.servicioPermisos.tieneAlgunPermiso(['solicitud.cancelar', 'solicitud.aprobar']);
  }

  puedeVerConfiguracionFlujos(): boolean {
    return this.servicioPermisos.tieneAlgunPermiso(['workflow.configurar', 'workflow.ver']);
  }
}
