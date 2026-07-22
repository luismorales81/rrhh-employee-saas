export interface RespuestaAutenticacion {
  tokenAcceso: string;
  tokenRefresco: string;
  usuarioId: number;
  empresaId: number | null;
  empresaNombre?: string | null;
  email: string;
  esSuperAdmin: boolean;
  permisos: string[];
}

export interface SesionUsuario {
  tokenAcceso: string;
  tokenRefresco: string;
  usuarioId: number;
  empresaId: number | null;
  empresaNombre?: string | null;
  email: string;
  esSuperAdmin: boolean;
  permisos: string[];
}

export interface SolicitudLogin {
  email: string;
  password: string;
}
