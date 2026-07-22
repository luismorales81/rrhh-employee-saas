import { Routes } from '@angular/router';

import {

  guardiaAutenticacion,

  guardiaContextoEmpresaRuta,

  guardiaEmpresaActiva,

  guardiaInvitado

} from './nucleo/guardias/guardia-autenticacion';

import {

  guardiaPermiso,

  guardiaPlataforma,

  guardiaPortalEmpleado,

  guardiaPortalRrhh

} from './nucleo/guardias/guardia-permiso';

import { LayoutPrincipalComponent } from './compartido/componentes/layout-principal/layout-principal.component';

import { LayoutPlataformaComponent } from './compartido/componentes/layout-plataforma/layout-plataforma.component';

import { LayoutEmpleadoComponent } from './compartido/componentes/layout-empleado/layout-empleado.component';



export const routes: Routes = [

  {

    path: 'iniciar-sesion',

    loadComponent: () =>

      import('./modulos/autenticacion/pagina-login/pagina-login.component').then(

        (m) => m.PaginaLoginComponent

      ),

    canActivate: [guardiaInvitado]

  },

  {

    path: 'plataforma',

    component: LayoutPlataformaComponent,

    canActivate: [guardiaAutenticacion, guardiaPlataforma],

    children: [

      { path: '', redirectTo: 'inicio', pathMatch: 'full' },

      {

        path: 'inicio',

        loadComponent: () =>

          import('./modulos/plataforma/pagina-inicio-plataforma/pagina-inicio-plataforma.component').then(

            (m) => m.PaginaInicioPlataformaComponent

          )

      },

      {

        path: 'empresas',

        loadComponent: () =>

          import('./modulos/empresas/pagina-empresas/pagina-empresas.component').then(

            (m) => m.PaginaEmpresasComponent

          )

      },

      {

        path: 'empresas/:id',

        canActivate: [guardiaPermiso],

        data: { permiso: 'empresa.ver' },

        loadComponent: () =>

          import('./modulos/empresas/pagina-detalle-empresa/pagina-detalle-empresa.component').then(

            (m) => m.PaginaDetalleEmpresaComponent

          )

      },

      {

        path: 'empresas/:id/usuarios',

        canActivate: [guardiaContextoEmpresaRuta, guardiaPermiso],

        data: { permiso: 'usuario.ver' },

        loadComponent: () =>

          import('./modulos/usuarios/pagina-usuarios/pagina-usuarios.component').then(

            (m) => m.PaginaUsuariosComponent

          )

      }

    ]

  },

  {

    path: '',

    component: LayoutPrincipalComponent,

    canActivate: [guardiaAutenticacion, guardiaPortalRrhh],

    children: [

      { path: '', redirectTo: 'panel', pathMatch: 'full' },

      {

        path: 'panel',

        canActivate: [guardiaEmpresaActiva],

        loadComponent: () =>

          import('./modulos/panel/pagina-panel/pagina-panel.component').then(

            (m) => m.PaginaPanelComponent

          )

      },

      {

        path: 'equipo',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'solicitud.aprobar' },

        loadComponent: () =>

          import('./modulos/equipo/pagina-equipo/pagina-equipo.component').then(

            (m) => m.PaginaEquipoComponent

          )

      },

      {

        path: 'equipo/aprobaciones',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'solicitud.aprobar' },

        loadComponent: () =>

          import('./modulos/equipo/pagina-equipo-aprobaciones/pagina-equipo-aprobaciones.component').then(

            (m) => m.PaginaEquipoAprobacionesComponent

          )

      },

      {

        path: 'usuarios',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'usuario.ver' },

        loadComponent: () =>

          import('./modulos/usuarios/pagina-usuarios/pagina-usuarios.component').then(

            (m) => m.PaginaUsuariosComponent

          )

      },

      {

        path: 'roles',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'rol.ver' },

        loadComponent: () =>

          import('./modulos/roles/pagina-roles/pagina-roles.component').then(

            (m) => m.PaginaRolesComponent

          )

      },

      {

        path: 'empleados',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'empleado.ver' },

        loadComponent: () =>

          import('./modulos/empleados/pagina-empleados/pagina-empleados.component').then(

            (m) => m.PaginaEmpleadosComponent

          )

      },

      {

        path: 'empleados/:id/legajo',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'empleado.ver' },

        loadComponent: () =>

          import('./modulos/empleados/pagina-legajo-empleado/pagina-legajo-empleado.component').then(

            (m) => m.PaginaLegajoEmpleadoComponent

          )

      },

      {

        path: 'organizacion',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'organizacion.gestionar' },

        loadComponent: () =>

          import('./modulos/organizacion/pagina-organizacion/pagina-organizacion.component').then(

            (m) => m.PaginaOrganizacionComponent

          )

      },

      {

        path: 'configuracion-workflow',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['workflow.configurar', 'workflow.ver'] },

        loadComponent: () =>

          import('./modulos/workflow/pagina-workflow/pagina-workflow.component').then(

            (m) => m.PaginaWorkflowComponent

          )

      },

      {

        path: 'solicitudes',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'solicitud.ver' },

        loadComponent: () =>

          import('./modulos/solicitudes/pagina-solicitudes/pagina-solicitudes.component').then(

            (m) => m.PaginaSolicitudesComponent

          )

      },

      {

        path: 'vacaciones',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['vacacion.ver', 'vacacion.politica.ver'] },

        loadComponent: () =>

          import('./modulos/vacaciones/pagina-vacaciones/pagina-vacaciones.component').then(

            (m) => m.PaginaVacacionesComponent

          )

      },

      {

        path: 'ausentismo',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'ausencia.ver' },

        loadComponent: () =>

          import('./modulos/ausentismo/pagina-ausentismo/pagina-ausentismo.component').then(

            (m) => m.PaginaAusentismoComponent

          )

      },

      {

        path: 'certificados-medicos',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: {
          permisos: ['certificado.ver_resumen', 'certificado.ver_completo', 'certificado.validar', 'certificado.configurar']
        },

        loadComponent: () =>

          import('./modulos/certificados-medicos/pagina-certificados-medicos/pagina-certificados-medicos.component').then(

            (m) => m.PaginaCertificadosMedicosComponent

          )

      },

      {

        path: 'certificados-medicos/configuracion',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'certificado.configurar' },

        loadComponent: () =>

          import('./modulos/certificados-medicos/pagina-configuracion-certificados/pagina-configuracion-certificados.component').then(

            (m) => m.PaginaConfiguracionCertificadosComponent

          )

      },

      {

        path: 'documentos',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['documento.ver', 'documento.gestionar'] },

        loadComponent: () =>

          import('./modulos/documentos/pagina-documentos/pagina-documentos.component').then(

            (m) => m.PaginaDocumentosComponent

          )

      },

      {

        path: 'recibos',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'recibo.ver' },

        loadComponent: () =>

          import('./modulos/recibos/pagina-recibos/pagina-recibos.component').then(

            (m) => m.PaginaRecibosComponent

          )

      },

      {

        path: 'comunicaciones',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'comunicacion.ver' },

        loadComponent: () =>

          import('./modulos/comunicaciones/pagina-comunicaciones/pagina-comunicaciones.component').then(

            (m) => m.PaginaComunicacionesComponent

          )

      },

      {

        path: 'plantillas-checklists',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['onboarding.gestionar', 'offboarding.gestionar'] },

        loadComponent: () =>

          import('./modulos/checklists/pagina-plantillas-checklists/pagina-plantillas-checklists.component').then(

            (m) => m.PaginaPlantillasChecklistsComponent

          )

      },

      {

        path: 'activos',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['activo.ver', 'activo.gestionar'] },

        loadComponent: () =>

          import('./modulos/activos/pagina-activos/pagina-activos.component').then(

            (m) => m.PaginaActivosComponent

          )

      },

      {

        path: 'novedades',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permisos: ['novedad.ver', 'novedad.gestionar'] },

        loadComponent: () =>

          import('./modulos/novedades/pagina-novedades/pagina-novedades.component').then(

            (m) => m.PaginaNovedadesComponent

          )

      },

      {

        path: 'fichajes',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'fichaje.ver' },

        loadComponent: () =>

          import('./modulos/fichajes/pagina-fichajes/pagina-fichajes.component').then(

            (m) => m.PaginaFichajesComponent

          )

      },

      {

        path: 'fichajes/zonas',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'fichaje.zona.ver' },

        loadComponent: () =>

          import('./modulos/fichajes/pagina-fichajes-zonas/pagina-fichajes-zonas.component').then(

            (m) => m.PaginaFichajesZonasComponent

          )

      },

      {

        path: 'fichajes/configuracion',

        canActivate: [guardiaEmpresaActiva, guardiaPermiso],

        data: { permiso: 'fichaje.configurar' },

        loadComponent: () =>

          import('./modulos/fichajes/pagina-fichajes-configuracion/pagina-fichajes-configuracion.component').then(

            (m) => m.PaginaFichajesConfiguracionComponent

          )

      }

    ]

  },

  {

    path: '',

    component: LayoutEmpleadoComponent,

    canActivate: [guardiaAutenticacion, guardiaPortalEmpleado, guardiaEmpresaActiva],

    children: [

      {

        path: 'mi-panel',

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-panel/pagina-mi-panel.component').then(

            (m) => m.PaginaMiPanelComponent

          )

      },

      {

        path: 'mi-pedidos',

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-pedidos/pagina-mi-pedidos.component').then(

            (m) => m.PaginaMiPedidosComponent

          )

      },

      {

        path: 'mi-archivos',

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-archivos/pagina-mi-archivos.component').then(

            (m) => m.PaginaMiArchivosComponent

          )

      },

      {

        path: 'mi-mas',

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-mas/pagina-mi-mas.component').then(

            (m) => m.PaginaMiMasComponent

          )

      },

      {

        path: 'mi-solicitudes',

        canActivate: [guardiaPermiso],

        data: { permiso: 'solicitud.crear' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-solicitudes/pagina-mi-solicitudes.component').then(

            (m) => m.PaginaMiSolicitudesComponent

          )

      },

      {

        path: 'mi-vacaciones',

        canActivate: [guardiaPermiso],

        data: { permiso: 'vacacion.solicitar' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-vacaciones/pagina-mi-vacaciones.component').then(

            (m) => m.PaginaMiVacacionesComponent

          )

      },

      {

        path: 'mi-documentos',

        canActivate: [guardiaPermiso],

        data: { permiso: 'documento.mis.ver' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-documentos/pagina-mi-documentos.component').then(

            (m) => m.PaginaMiDocumentosComponent

          )

      },

      {

        path: 'mis-recibos',

        canActivate: [guardiaPermiso],

        data: { permiso: 'recibo.mis.ver' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mis-recibos/pagina-mis-recibos.component').then(

            (m) => m.PaginaMisRecibosComponent

          )

      },

      {

        path: 'mi-comunicaciones',

        canActivate: [guardiaPermiso],

        data: { permiso: 'comunicacion.mis.ver' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-comunicaciones/pagina-mi-comunicaciones.component').then(

            (m) => m.PaginaMiComunicacionesComponent

          )

      },

      {

        path: 'mi-notificaciones',

        canActivate: [guardiaPermiso],

        data: { permiso: 'notificacion.ver' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-notificaciones/pagina-mi-notificaciones.component').then(

            (m) => m.PaginaMiNotificacionesComponent

          )

      },

      {

        path: 'mi-perfil',

        canActivate: [guardiaPermiso],

        data: { permiso: 'empleado.ver' },

        loadComponent: () =>

          import('./modulos/portal-empleado/pagina-mi-perfil/pagina-mi-perfil.component').then(

            (m) => m.PaginaMiPerfilComponent

          )

      }

    ]

  },

  { path: '**', redirectTo: 'iniciar-sesion' }
];


