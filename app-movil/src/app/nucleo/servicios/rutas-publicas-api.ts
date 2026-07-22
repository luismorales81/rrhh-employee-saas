/** Rutas que no deben llevar token JWT ni disparar refresco de sesión. */
export function esPeticionPublica(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/refrescar') ||
    url.includes('/api-docs') ||
    url.includes('/swagger-ui') ||
    url.includes('/actuator/')
  );
}
