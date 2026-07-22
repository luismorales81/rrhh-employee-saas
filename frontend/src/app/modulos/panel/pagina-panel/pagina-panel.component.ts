import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import { ServicioSolicitudes } from '../../../nucleo/servicios/servicio-solicitudes';
import { ServicioVacaciones } from '../../../nucleo/servicios/servicio-vacaciones';
import { ServicioDocumentos } from '../../../nucleo/servicios/servicio-documentos';
import { ServicioRecibos } from '../../../nucleo/servicios/servicio-recibos';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioPaletaComandos } from '../../../ui/paleta-comandos/servicio-paleta-comandos';
import { EstadoVacioComponent } from '../../../ui';

interface ItemAtencion {
  id: string;
  titulo: string;
  detalle: string;
  ruta: string;
  prioridad: number;
}

interface TarjetaIndicador {
  titulo: string;
  valor: number | string;
  ruta?: string;
  alerta?: boolean;
  permiso?: string;
}

@Component({
  selector: 'app-pagina-panel',
  standalone: true,
  imports: [RouterLink, EsqueletoCargaComponent, EstadoVacioComponent],
  templateUrl: './pagina-panel.component.html',
  styleUrl: './pagina-panel.component.scss'
})
export class PaginaPanelComponent implements OnInit {
  readonly cargando = signal(true);
  readonly tarjetas = signal<TarjetaIndicador[]>([]);
  readonly atencion = signal<ItemAtencion[]>([]);

  readonly saludo = computed(() => {
    const h = new Date().getHours();
    if (h < 12) return 'Buenos días';
    if (h < 19) return 'Buenas tardes';
    return 'Buenas noches';
  });

  readonly nombre = computed(() => {
    const email = this.servicioAutenticacion.sesionActual()?.email ?? '';
    return email.split('@')[0] || '';
  });

  readonly tarjetasVisibles = computed(() =>
    this.tarjetas().filter((t) => !t.permiso || this.servicioPermisos.tienePermiso(t.permiso))
  );

  constructor(
    private readonly servicioEmpleados: ServicioEmpleados,
    private readonly servicioSolicitudes: ServicioSolicitudes,
    private readonly servicioVacaciones: ServicioVacaciones,
    private readonly servicioDocumentos: ServicioDocumentos,
    private readonly servicioRecibos: ServicioRecibos,
    private readonly servicioPermisos: ServicioPermisos,
    private readonly servicioAutenticacion: ServicioAutenticacion,
    private readonly servicioPaleta: ServicioPaletaComandos
  ) {}

  ngOnInit(): void {
    forkJoin({
      empleados: this.servicioEmpleados.listar().pipe(catchError(() => of([]))),
      solicitudesPendientes: this.servicioSolicitudes.listarPendientes().pipe(catchError(() => of([]))),
      vacaciones: this.servicioVacaciones.listarSolicitudes().pipe(catchError(() => of([]))),
      documentos: this.servicioDocumentos.listarAsignaciones().pipe(catchError(() => of([]))),
      alertasRecibos: this.servicioRecibos.obtenerAlertasSeguimiento().pipe(catchError(() => of(null)))
    }).subscribe((datos) => {
      const aprobacionesPendientes = datos.solicitudesPendientes.length;
      const vacacionesPendientes = datos.vacaciones.filter(
        (v) => v.estado === 'EN_REVISION' || v.estado === 'PENDIENTE' || v.estado === 'CAMBIOS_SOLICITADOS'
      ).length;
      const documentosPendientes = datos.documentos.filter((d) => d.estado === 'PENDIENTE').length;
      const recibosAlerta =
        (datos.alertasRecibos?.totalAntiguosSinFirmar ?? 0) +
        (datos.alertasRecibos?.totalDisconformidades ?? 0);

      const items: ItemAtencion[] = [];
      if (aprobacionesPendientes > 0 && this.servicioPermisos.tienePermiso('solicitud.ver')) {
        items.push({
          id: 'sol',
          titulo: `${aprobacionesPendientes} aprobaciones pendientes`,
          detalle: 'Revisá pedidos del equipo',
          ruta: '/solicitudes',
          prioridad: 1
        });
      }
      if (vacacionesPendientes > 0 && this.servicioPermisos.tieneAlgunPermiso(['vacacion.ver', 'vacacion.politica.ver'])) {
        items.push({
          id: 'vac',
          titulo: `${vacacionesPendientes} vacaciones en revisión`,
          detalle: 'Validación LCT y respuesta',
          ruta: '/vacaciones',
          prioridad: 2
        });
      }
      if (documentosPendientes > 0 && this.servicioPermisos.tieneAlgunPermiso(['documento.ver', 'documento.gestionar'])) {
        items.push({
          id: 'doc',
          titulo: `${documentosPendientes} documentos sin firmar`,
          detalle: 'Seguimiento de firmas',
          ruta: '/documentos',
          prioridad: 3
        });
      }
      if (recibosAlerta > 0 && this.servicioPermisos.tienePermiso('recibo.ver')) {
        items.push({
          id: 'rec',
          titulo: `${recibosAlerta} alertas de recibos`,
          detalle: 'Firmas atrasadas o disconformidades',
          ruta: '/recibos',
          prioridad: 4
        });
      }

      this.atencion.set(items.sort((a, b) => a.prioridad - b.prioridad));

      this.tarjetas.set([
        {
          titulo: 'Aprobaciones pendientes',
          valor: aprobacionesPendientes,
          ruta: '/solicitudes',
          alerta: aprobacionesPendientes > 0,
          permiso: 'solicitud.ver'
        },
        {
          titulo: 'Personas activas',
          valor: datos.empleados.filter((e) => e.estado === 'ACTIVO').length,
          ruta: '/empleados',
          permiso: 'empleado.ver'
        },
        {
          titulo: 'Documentos sin firmar',
          valor: documentosPendientes,
          ruta: '/documentos',
          alerta: documentosPendientes > 0,
          permiso: 'documento.ver'
        },
        {
          titulo: 'Vacaciones pendientes',
          valor: vacacionesPendientes,
          ruta: '/vacaciones',
          alerta: vacacionesPendientes > 0,
          permiso: 'vacacion.ver'
        }
      ]);
      this.cargando.set(false);
    });
  }

  abrirPaleta(): void {
    this.servicioPaleta.abrir();
  }
}
