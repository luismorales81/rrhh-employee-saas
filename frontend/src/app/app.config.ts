import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  provideLucideIcons,
  LucideLayoutDashboard,
  LucideKeyRound,
  LucideShield,
  LucideUsers,
  LucideBuilding2,
  LucideWorkflow,
  LucideListChecks,
  LucideClipboardList,
  LucideTreePalm,
  LucideHeartPulse,
  LucideStethoscope,
  LucideFileText,
  LucideReceipt,
  LucideMegaphone,
  LucideLaptop,
  LucideTrendingUp,
  LucideClock,
  LucideHome,
  LucideBuilding,
  LucideBell,
  LucideUser,
  LucideLayoutGrid,
  LucideUsersRound,
  LucideMapPin,
  LucideSearch,
  LucideChevronDown
} from '@lucide/angular';

import { routes } from './app.routes';
import { interceptorAutenticacion } from './nucleo/interceptores/interceptor-autenticacion';
import { interceptorErroresHttp } from './nucleo/interceptores/interceptor-errores-http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([interceptorAutenticacion, interceptorErroresHttp])),
    provideLucideIcons(
      LucideLayoutDashboard,
      LucideKeyRound,
      LucideShield,
      LucideUsers,
      LucideBuilding2,
      LucideWorkflow,
      LucideListChecks,
      LucideClipboardList,
      LucideTreePalm,
      LucideHeartPulse,
      LucideStethoscope,
      LucideFileText,
      LucideReceipt,
      LucideMegaphone,
      LucideLaptop,
      LucideTrendingUp,
      LucideClock,
      LucideHome,
      LucideBuilding,
      LucideBell,
      LucideUser,
      LucideLayoutGrid,
      LucideUsersRound,
      LucideMapPin,
      LucideSearch,
      LucideChevronDown
    )
  ]
};