import { Routes } from '@angular/router';
import { guardiaAutenticacion, guardiaInvitado } from './nucleo/guardias/guardia-autenticacion';
import { guardiaPortalEmpleado, guardiaPortalRrhh } from './nucleo/guardias/guardia-portal';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'tabs/inicio',
    pathMatch: 'full'
  },
  {
    path: 'iniciar-sesion',
    canActivate: [guardiaInvitado],
    loadComponent: () =>
      import('./paginas/pagina-login/pagina-login.component').then((m) => m.PaginaLoginComponent)
  },
  {
    path: 'bienvenida',
    canActivate: [guardiaInvitado],
    loadComponent: () =>
      import('./paginas/pagina-bienvenida/pagina-bienvenida.component').then(
        (m) => m.PaginaBienvenidaComponent
      )
  },
  {
    path: 'recuperar-acceso',
    canActivate: [guardiaInvitado],
    loadComponent: () =>
      import('./paginas/pagina-recuperar-acceso/pagina-recuperar-acceso.component').then(
        (m) => m.PaginaRecuperarAccesoComponent
      )
  },
  {
    path: 'tabs',
    canActivate: [guardiaAutenticacion, guardiaPortalEmpleado],
    loadComponent: () =>
      import('./layout/layout-tabs.component').then((m) => m.LayoutTabsComponent),
    children: [
      {
        path: 'inicio',
        loadComponent: () =>
          import('./paginas/pagina-inicio/pagina-inicio.component').then((m) => m.PaginaInicioComponent)
      },
      {
        path: 'mi-tiempo',
        loadComponent: () =>
          import('./paginas/pagina-mi-tiempo/pagina-mi-tiempo.component').then(
            (m) => m.PaginaMiTiempoComponent
          )
      },
      {
        path: 'mis-papeles',
        loadComponent: () =>
          import('./paginas/pagina-mis-papeles/pagina-mis-papeles.component').then(
            (m) => m.PaginaMisPapelesComponent
          )
      },
      {
        path: 'mis-activos',
        loadComponent: () =>
          import('./paginas/pagina-mis-activos/pagina-mis-activos.component').then(
            (m) => m.PaginaMisActivosComponent
          )
      },
      {
        path: 'onboarding',
        loadComponent: () =>
          import('./paginas/pagina-onboarding/pagina-onboarding.component').then(
            (m) => m.PaginaOnboardingComponent
          )
      },
      {
        path: 'mas',
        loadComponent: () =>
          import('./paginas/pagina-mas/pagina-mas.component').then((m) => m.PaginaMasComponent)
      },
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./paginas/pagina-solicitudes/pagina-solicitudes.component').then(
            (m) => m.PaginaSolicitudesComponent
          )
      },
      {
        path: 'vacaciones',
        loadComponent: () =>
          import('./paginas/pagina-vacaciones/pagina-vacaciones.component').then(
            (m) => m.PaginaVacacionesComponent
          )
      },
      {
        path: 'ausentismo',
        loadComponent: () =>
          import('./paginas/pagina-ausentismo/pagina-ausentismo.component').then(
            (m) => m.PaginaAusentismoComponent
          )
      },
      {
        path: 'documentos',
        loadComponent: () =>
          import('./paginas/pagina-documentos/pagina-documentos.component').then(
            (m) => m.PaginaDocumentosComponent
          )
      },
      {
        path: 'notificaciones',
        loadComponent: () =>
          import('./paginas/pagina-notificaciones/pagina-notificaciones.component').then(
            (m) => m.PaginaNotificacionesComponent
          )
      },
      {
        path: 'recibos',
        loadComponent: () =>
          import('./paginas/pagina-recibos/pagina-recibos.component').then(
            (m) => m.PaginaRecibosComponent
          )
      },
      {
        path: 'recibos/:id',
        loadComponent: () =>
          import('./paginas/pagina-detalle-recibo/pagina-detalle-recibo.component').then(
            (m) => m.PaginaDetalleReciboComponent
          )
      },
      {
        path: 'comunicaciones',
        loadComponent: () =>
          import('./paginas/pagina-comunicaciones/pagina-comunicaciones.component').then(
            (m) => m.PaginaComunicacionesComponent
          )
      },
      {
        path: 'fichaje',
        loadComponent: () =>
          import('./paginas/pagina-fichaje/pagina-fichaje.component').then(
            (m) => m.PaginaFichajeComponent
          )
      },
      {
        path: 'fichaje/historial',
        loadComponent: () =>
          import('./paginas/pagina-fichaje-historial/pagina-fichaje-historial.component').then(
            (m) => m.PaginaFichajeHistorialComponent
          )
      },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./paginas/pagina-perfil/pagina-perfil.component').then((m) => m.PaginaPerfilComponent)
      },
      {
        path: 'ayuda',
        loadComponent: () =>
          import('./paginas/pagina-ayuda/pagina-ayuda.component').then((m) => m.PaginaAyudaComponent)
      },
      {
        path: 'configuracion',
        loadComponent: () =>
          import('./paginas/pagina-configuracion/pagina-configuracion.component').then(
            (m) => m.PaginaConfiguracionComponent
          )
      },
      {
        path: '',
        redirectTo: 'inicio',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'tabs-rrhh',
    canActivate: [guardiaAutenticacion, guardiaPortalRrhh],
    loadComponent: () =>
      import('./layout/layout-tabs-rrhh.component').then((m) => m.LayoutTabsRrhhComponent),
    children: [
      {
        path: 'panel',
        loadComponent: () =>
          import('./paginas/pagina-panel-rrhh/pagina-panel-rrhh.component').then(
            (m) => m.PaginaPanelRrhhComponent
          )
      },
      {
        path: 'activos',
        loadComponent: () =>
          import('./paginas/pagina-activos-rrhh/pagina-activos-rrhh.component').then(
            (m) => m.PaginaActivosRrhhComponent
          )
      },
      {
        path: 'novedades',
        loadComponent: () =>
          import('./paginas/pagina-novedades-rrhh/pagina-novedades-rrhh.component').then(
            (m) => m.PaginaNovedadesRrhhComponent
          )
      },
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./paginas/pagina-solicitudes-rrhh/pagina-solicitudes-rrhh.component').then(
            (m) => m.PaginaSolicitudesRrhhComponent
          )
      },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./paginas/pagina-perfil-rrhh/pagina-perfil-rrhh.component').then(
            (m) => m.PaginaPerfilRrhhComponent
          )
      },
      {
        path: '',
        redirectTo: 'panel',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'tabs/inicio'
  }
];
