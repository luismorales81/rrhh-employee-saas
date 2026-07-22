import { ActivatedRouteSnapshot, DetachedRouteHandle, RouteReuseStrategy } from '@angular/router';

/** No apila vistas al navegar (evita overlays invisibles de Ionic). */
export class EstrategiaReutilizacionRutas implements RouteReuseStrategy {
  shouldDetach(): boolean {
    return false;
  }

  store(): void {}

  shouldAttach(): boolean {
    return false;
  }

  retrieve(): DetachedRouteHandle | null {
    return null;
  }

  shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
    return future.routeConfig === curr.routeConfig;
  }
}
