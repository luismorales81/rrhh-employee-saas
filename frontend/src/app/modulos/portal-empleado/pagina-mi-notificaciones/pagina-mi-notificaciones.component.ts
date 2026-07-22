import { Component, OnInit, computed, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { NotificacionResumen, ServicioNotificaciones } from '../../../nucleo/servicios/servicio-notificaciones';

interface GrupoNotificaciones {
  titulo: string;
  items: NotificacionResumen[];
}

@Component({
  selector: 'app-pagina-mi-notificaciones',
  standalone: true,
  imports: [DatePipe, EsqueletoCargaComponent],
  templateUrl: './pagina-mi-notificaciones.component.html'
})
export class PaginaMiNotificacionesComponent implements OnInit {
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly notificaciones = signal<NotificacionResumen[]>([]);

  readonly grupos = computed(() => this.agruparPorFecha(this.notificaciones()));

  constructor(private readonly servicioNotificaciones: ServicioNotificaciones) {}

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioNotificaciones.listar().subscribe({
      next: (lista) => {
        this.notificaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las notificaciones');
        this.cargando.set(false);
      }
    });
  }

  marcarLeida(notificacion: NotificacionResumen): void {
    if (notificacion.leida) {
      return;
    }
    this.servicioNotificaciones.marcarLeida(notificacion.id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo marcar como leída')
    });
  }

  private agruparPorFecha(lista: NotificacionResumen[]): GrupoNotificaciones[] {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const inicioSemana = new Date(hoy);
    inicioSemana.setDate(hoy.getDate() - 7);

    const hoyItems: NotificacionResumen[] = [];
    const semanaItems: NotificacionResumen[] = [];
    const anterioresItems: NotificacionResumen[] = [];

    for (const item of lista) {
      const fecha = new Date(item.createdAt);
      if (fecha >= hoy) {
        hoyItems.push(item);
      } else if (fecha >= inicioSemana) {
        semanaItems.push(item);
      } else {
        anterioresItems.push(item);
      }
    }

    const grupos: GrupoNotificaciones[] = [];
    if (hoyItems.length) grupos.push({ titulo: 'Hoy', items: hoyItems });
    if (semanaItems.length) grupos.push({ titulo: 'Esta semana', items: semanaItems });
    if (anterioresItems.length) grupos.push({ titulo: 'Anteriores', items: anterioresItems });
    return grupos;
  }
}
