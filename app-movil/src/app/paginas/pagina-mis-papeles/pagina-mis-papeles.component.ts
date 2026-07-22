import { Component, inject, OnInit, signal } from '@angular/core';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular/standalone';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { FilaAccesoComponent } from '../../compartido/componentes/fila-acceso/fila-acceso.component';
import { ServicioDocumentos } from '../../nucleo/servicios/servicio-documentos';
import { ServicioRecibos } from '../../nucleo/servicios/servicio-recibos';
import { ServicioSolicitudes } from '../../nucleo/servicios/servicio-solicitudes';
import { catchError, forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-pagina-mis-papeles',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    EncabezadoPaginaComponent,
    FilaAccesoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Mis papeles</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Mis papeles"
        subtitulo="Recibos, documentos y solicitudes"
      />

      <div class="lista-accesos">
        <app-fila-acceso
          titulo="Recibos"
          subtitulo="Firmá y consultá tus recibos"
          icono="cash-outline"
          ruta="/tabs/recibos"
          [badge]="recibosPendientes()"
        />
        <app-fila-acceso
          titulo="Documentos"
          subtitulo="Leé y firmá lo asignado"
          icono="folder-outline"
          ruta="/tabs/documentos"
          [badge]="documentosPendientes()"
        />
        <app-fila-acceso
          titulo="Solicitudes"
          subtitulo="Licencias y trámites"
          icono="document-text-outline"
          ruta="/tabs/solicitudes"
          [badge]="solicitudesPendientes()"
        />
      </div>
    </ion-content>
  `,
  styles: `
    .lista-accesos {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }
  `
})
export class PaginaMisPapelesComponent implements OnInit {
  private readonly servicioDocumentos = inject(ServicioDocumentos);
  private readonly servicioRecibos = inject(ServicioRecibos);
  private readonly servicioSolicitudes = inject(ServicioSolicitudes);

  readonly recibosPendientes = signal(0);
  readonly documentosPendientes = signal(0);
  readonly solicitudesPendientes = signal(0);

  ngOnInit(): void {
    forkJoin({
      documentos: this.servicioDocumentos.listarMisAsignaciones().pipe(catchError(() => of([]))),
      recibos: this.servicioRecibos.listarPropios().pipe(catchError(() => of([]))),
      solicitudes: this.servicioSolicitudes.listarPropias().pipe(catchError(() => of([])))
    }).subscribe(({ documentos, recibos, solicitudes }) => {
      this.documentosPendientes.set(
        documentos.filter((d) => d.estado !== 'LEIDO' && (!d.requiereFirma || !d.firmado)).length
      );
      this.recibosPendientes.set(
        recibos.filter((r) => r.estado === 'PENDIENTE' || r.estado === 'VISTO').length
      );
      this.solicitudesPendientes.set(
        solicitudes.filter((s) =>
          ['BORRADOR', 'ENVIADA', 'EN_APROBACION', 'EN_REVISION', 'CAMBIOS_SOLICITADOS'].includes(s.estado)
        ).length
      );
    });
  }
}
