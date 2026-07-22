# Fichaje móvil con zonas geográficas

## Objetivo

Permitir que los empleados registren entrada y salida desde la app móvil, validando que estén dentro de zonas habilitadas por RRHH. La ubicación se captura **solo en el instante del fichaje**; no hay seguimiento continuo.

## Roles y permisos

| Permiso | Uso |
|---------|-----|
| `fichaje.zona.ver` | Ver zonas en web RRHH |
| `fichaje.zona.gestionar` | Crear/editar polígonos y asignaciones |
| `fichaje.configurar` | Habilitar fichaje móvil por empleado y política |
| `fichaje.ver` | Ver marcas y reportes |
| `fichaje.revisar` | Aprobar/rechazar fichajes pendientes |
| `fichaje.registrar` | Fichar desde app móvil |
| `fichaje.mis.ver` | Ver propio historial |

## Entidades

- **ZonaFichaje**: polígono (`coordenadas` JSON), alcance `GLOBAL` o `ASIGNADA`
- **AsignacionZonaFichaje**: vínculo zona ↔ empleado / sector / área / gerencia
- **ConfiguracionFichajeEmpleado**: fichaje móvil habilitado, consentimiento
- **PoliticaFichajeEmpresa**: texto de transparencia, versión, precisión GPS máxima
- **MarcaFichaje**: evento entrada/salida con GPS, dispositivo, estado

## Estados de marca

| Estado | Significado |
|--------|-------------|
| `VALIDADA` | Dentro de zona habilitada |
| `PENDIENTE_REVISION` | Fuera de zona, sin zona, o precisión GPS baja |
| `RECHAZADA` | RRHH rechazó la marca |

## Flujo empleado (móvil)

1. RRHH habilita fichaje móvil en **Fichajes → Configuración**
2. Empleado abre **Fichaje** (tab Más o Inicio)
3. Acepta política de transparencia (qué datos se registran y qué no)
4. Toca **Fichar entrada** o **Fichar salida** → GPS puntual + datos dispositivo
5. Ve resultado: validada o enviada a revisión

## Flujo RRHH (web)

1. **Fichajes → Zonas**: dibujar polígono en Google Maps
2. **Configuración**: habilitar empleados, editar texto legal
3. **Fichajes → Marcas**: filtrar, exportar CSV, revisar pendientes

## API

Base: `/api/fichajes`

Ver controlador `ControladorFichajes` para endpoints completos.

## Configuración Google Maps (web)

1. Copiar `frontend/src/environments/claves-local.example.ts` → `claves-local.ts`
2. Completar `claveGoogleMaps` (archivo en `.gitignore`)
3. En Google Cloud Console:
   - Habilitar **Maps JavaScript API** y **Maps Drawing Library**
   - Restringir clave por HTTP referrer (`localhost:4200`, dominio prod)
   - **No commitear la clave** al repositorio

## Demo

Tras arrancar con datos demo:

- Zona global **Casa Central Demo** (microcentro CABA)
- Juan Pérez (`empleado@demo.local`) con fichaje móvil habilitado
- RRHH gestiona zonas en `/fichajes/zonas`

## Cumplimiento

- Ley 25.326: finalidad limitada, acceso restringido, política visible al empleado
- Retención sugerida: 5 años (configurable por empresa)
- Revisar textos legales con asesoría antes de producción

## Backlog

- Integración con novedades de liquidación (horas extras)
- Notificaciones push al aprobar/rechazar
- Fichaje alternativo (kiosco) para quien no tiene dispositivo
