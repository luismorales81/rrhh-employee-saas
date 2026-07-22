import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import { NotificacionResumen, ServicioNotificaciones } from '../../nucleo/servicios/servicio-notificaciones';
import { ServicioContadoresTabs } from '../../nucleo/servicios/servicio-contadores-tabs';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import {
  etiquetaTipoNotificacion,
  rutaDesdeNotificacion
} from '../../nucleo/utilidades/ruta-desde-notificacion';

interface GrupoNotificaciones {
  titulo: string;
  items: NotificacionResumen[];
}

@Component({
  selector: 'app-pagina-notificaciones',
  standalone: true,
  imports: [
    DatePipe,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Notificaciones</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Notificaciones"
        subtitulo="Tocá una para ir al detalle"
      />

      @if (cargando()) {
        <app-estado-carga modo="lista" />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (notificaciones().length === 0) {
        <app-estado-vacio
          icono="notifications-outline"
          titulo="Sin notificaciones"
          descripcion="Cuando haya novedades, van a aparecer acá."
        />
      } @else {
        @for (grupo of grupos(); track grupo.titulo) {
          <h3 class="grupo-lista-titulo">{{ grupo.titulo }}</h3>
          <div class="lista-tarjetas">
            @for (n of grupo.items; track n.id) {
              <app-tarjeta-fila
                [titulo]="n.titulo"
                [subtitulo]="n.mensaje"
                [meta]="(n.createdAt | date:'dd/MM/yyyy HH:mm') ?? ''"
                [etiqueta]="etiquetaTipo(n.tipo)"
                [alerta]="!n.leida"
                [acciones]="accionesPara(n)"
              />
            }
          </div>
        }
      }
    </ion-content>
  `
})
export class PaginaNotificacionesComponent implements OnInit {
  private readonly servicioNotificaciones = inject(ServicioNotificaciones);
  private readonly servicioContadores = inject(ServicioContadoresTabs);
  private readonly router = inject(Router);

  readonly notificaciones = signal<NotificacionResumen[]>([]);
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);

  readonly grupos = computed(() => this.agruparPorFecha(this.notificaciones()));

  ngOnInit(): void {
    this.cargar();
  }

  etiquetaTipo(tipo: string): string {
    return etiquetaTipoNotificacion(tipo);
  }

  abrir(notificacion: NotificacionResumen): void {
    const destino = rutaDesdeNotificacion(notificacion.tipo, notificacion.referenciaId);
    const ir = () => void this.router.navigateByUrl(destino);

    if (notificacion.leida) {
      ir();
      return;
    }

    this.servicioNotificaciones.marcarLeida(notificacion.id).subscribe({
      next: () => {
        this.servicioContadores.refrescar();
        ir();
      },
      error: () => {
        ir();
      }
    });
  }

  accionesPara(notificacion: NotificacionResumen): AccionTarjetaFila[] {
    return [{
      id: `abrir-${notificacion.id}`,
      etiqueta: notificacion.leida ? 'Abrir' : 'Ver',
      ejecutar: () => this.abrir(notificacion)
    }];
  }

  private cargar(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);
    this.servicioNotificaciones.listar().subscribe({
      next: (lista) => {
        this.notificaciones.set(lista);
        this.cargando.set(false);
        this.servicioContadores.refrescar();
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las notificaciones.');
        this.cargando.set(false);
      }
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
