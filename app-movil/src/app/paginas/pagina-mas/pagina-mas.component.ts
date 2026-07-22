import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText
} from '@ionic/angular/standalone';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { ServicioNotificaciones } from '../../nucleo/servicios/servicio-notificaciones';
import { ServicioComunicaciones } from '../../nucleo/servicios/servicio-comunicaciones';
import { ServicioActivos } from '../../nucleo/servicios/servicio-activos';
import { ServicioVacaciones } from '../../nucleo/servicios/servicio-vacaciones';
import { ServicioFichajes } from '../../nucleo/servicios/servicio-fichajes';
import { TarjetaMetricaComponent } from '../../compartido/componentes/tarjeta-metrica/tarjeta-metrica.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { environment } from '../../../environments/environment';
import { addIcons } from 'ionicons';
import {
  calendarOutline,
  medkitOutline,
  megaphoneOutline,
  cashOutline,
  notificationsOutline,
  laptopOutline,
  checkmarkDoneOutline,
  timeOutline
} from 'ionicons/icons';

addIcons({
  calendarOutline,
  medkitOutline,
  megaphoneOutline,
  cashOutline,
  notificationsOutline,
  laptopOutline,
  checkmarkDoneOutline,
  timeOutline
});

@Component({
  selector: 'app-pagina-mas',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    TarjetaMetricaComponent,
    EstadoCargaComponent,
    EncabezadoPaginaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Más</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Más opciones"
        subtitulo="Todo lo que necesitás en un solo lugar"
      />

      @if (cargando()) {
        <app-estado-carga modo="esqueleto" />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else {
        <div class="grilla-metricas">
          <app-tarjeta-metrica
            titulo="Vacaciones"
            [valor]="diasVacaciones()"
            icono="calendar-outline"
            ruta="/tabs/vacaciones"
          />
          <app-tarjeta-metrica
            titulo="Ausencias"
            valor="Reportar"
            icono="medkit-outline"
            ruta="/tabs/ausentismo"
          />
          @if (servicioPermisos.tienePermiso('fichaje.registrar')) {
            <app-tarjeta-metrica
              titulo="Asistencia"
              [valor]="estadoFichaje()"
              icono="time-outline"
              ruta="/tabs/fichaje"
            />
          }
          <app-tarjeta-metrica
            titulo="Avisos"
            [valor]="comunicacionesNoLeidas()"
            [alerta]="comunicacionesNoLeidas() > 0"
            icono="megaphone-outline"
            ruta="/tabs/comunicaciones"
          />
          <app-tarjeta-metrica
            titulo="Recibos"
            valor="Ver"
            icono="cash-outline"
            ruta="/tabs/recibos"
          />
          <app-tarjeta-metrica
            titulo="Notificaciones"
            [valor]="notificacionesNoLeidas()"
            [alerta]="notificacionesNoLeidas() > 0"
            icono="notifications-outline"
            ruta="/tabs/notificaciones"
          />
          @if (servicioPermisos.tienePermiso('activo.ver')) {
            <app-tarjeta-metrica
              titulo="Mis activos"
              [valor]="activosAsignados()"
              icono="laptop-outline"
              ruta="/tabs/mis-activos"
            />
          }
          @if (onboardingActivo()) {
            <app-tarjeta-metrica
              titulo="Primeros pasos"
              valor="En curso"
              icono="checkmark-done-outline"
              [alerta]="true"
              ruta="/tabs/onboarding"
            />
          }
        </div>
      }
    </ion-content>
  `
})
export class PaginaMasComponent implements OnInit {
  readonly servicioPermisos = inject(ServicioPermisos);
  private readonly router = inject(Router);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioNotificaciones = inject(ServicioNotificaciones);
  private readonly servicioComunicaciones = inject(ServicioComunicaciones);
  private readonly servicioActivos = inject(ServicioActivos);
  private readonly servicioVacaciones = inject(ServicioVacaciones);
  private readonly servicioFichajes = inject(ServicioFichajes);

  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly notificacionesNoLeidas = signal(0);
  readonly comunicacionesNoLeidas = signal(0);
  readonly activosAsignados = signal(0);
  readonly diasVacaciones = signal<string>('—');
  readonly onboardingActivo = signal(false);
  readonly estadoFichaje = signal('Fichar');

  ngOnInit(): void {
    if (environment.navegacionV2) {
      void this.router.navigateByUrl('/tabs/perfil', { replaceUrl: true });
      return;
    }

    this.servicioEmpleados.obtenerPropio().pipe(
      switchMap((empleado) => forkJoin({
        notificaciones: this.servicioNotificaciones.contarNoLeidas().pipe(catchError(() => of({ cantidad: 0 }))),
        comunicaciones: this.servicioComunicaciones.listarMisComunicaciones().pipe(catchError(() => of([]))),
        legajo: this.servicioEmpleados.obtenerLegajo(empleado.id).pipe(catchError(() => of(null))),
        saldo: this.servicioVacaciones.saldoPropio().pipe(catchError(() => of(null))),
        activos: this.servicioPermisos.tienePermiso('activo.ver')
          ? this.servicioActivos.listarAsignacionesEmpleado(empleado.id).pipe(catchError(() => of([])))
          : of([])
      }))
    ).subscribe({
      next: ({ notificaciones, comunicaciones, legajo, saldo, activos }) => {
        this.notificacionesNoLeidas.set(notificaciones.cantidad);
        this.comunicacionesNoLeidas.set(comunicaciones.filter((c) => !c.leida).length);
        this.onboardingActivo.set(legajo?.estadoOnboarding === 'EN_PROGRESO');
        this.activosAsignados.set(activos.filter((a) => !a.fechaDevolucion).length);
        this.diasVacaciones.set(saldo ? `${saldo.diasDisponibles} días` : '—');
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar el menú.');
        this.cargando.set(false);
      }
    });
    if (this.servicioPermisos.tienePermiso('fichaje.registrar')) {
      this.servicioFichajes.obtenerEstadoDia().pipe(catchError(() => of(null))).subscribe({
        next: (e) => {
          if (!e?.fichajeHabilitado) {
            this.estadoFichaje.set('No habilitado');
          } else if (e.ultimaMarcaTipo === 'ENTRADA') {
            this.estadoFichaje.set('En jornada');
          } else {
            this.estadoFichaje.set('Fichar');
          }
        }
      });
    }
  }
}
