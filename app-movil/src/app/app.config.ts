import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, RouteReuseStrategy } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideIonicAngular } from '@ionic/angular/standalone';

import { routes } from './app.routes';
import { EstrategiaReutilizacionRutas } from './nucleo/estrategia-reutilizacion-rutas';
import { interceptorAutenticacion } from './nucleo/servicios/interceptor-autenticacion';
import { interceptorErroresHttp } from './nucleo/servicios/interceptor-errores-http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([interceptorAutenticacion, interceptorErroresHttp])),
    provideIonicAngular({ mode: 'ios' }),
    // Evita apilar ion-page al cambiar de tab (bloquea clics en toda la app).
    { provide: RouteReuseStrategy, useClass: EstrategiaReutilizacionRutas }
  ]
};
