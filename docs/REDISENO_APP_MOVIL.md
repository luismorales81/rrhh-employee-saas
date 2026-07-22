# Rediseño app móvil Ligo Empleados

**Estado:** rediseño mobile E0–E5 entregado (widgets/offline diferidos)  
**Stack:** Angular 19 + Ionic 8 + Capacitor 7 (`app-movil/`)  
**Alcance de este ciclo:** solo portal **empleado**. El portal RRHH móvil se congela (sin rediseño).

Documento vivo: auditoría UX, design system mobile, navegación, plan de ejecución y criterios de éxito.

---

## 1. Diagnóstico ejecutivo

La app es funcionalmente rica, pero se siente como un **portal web comprimido en tabs**:

- Home = grilla de métricas (ERP), no asistente del día
- Vacaciones, recibos, fichaje y ausencias detrás de **Más** (+1–2 toques)
- Jerga de sistema (estados en MAYÚSCULAS, LCT en listados, “republicación”)
- Flujos legales necesarios (firma, GPS) con poca contención emocional

**Veredicto:** no hace falta cambiar de framework. Hay que reordenar navegación, humanizar lenguaje y convertir la Home en centro de pendientes.

---

## 2. Decisiones de producto (congeladas)

| Decisión | Valor |
|----------|--------|
| Framework | Mantener Ionic + Angular |
| Portal RRHH en móvil | **Congelado** en este ciclo; foco 100 % empleado |
| Feature flag navegación | `environment.navegacionV2` (default `false` hasta E1) |
| Rollout | Interno → 1 tenant piloto → todos |
| Home pendientes (v1) | Agregación en cliente; endpoint dedicado si duele performance |
| Regla de toques | Toda acción frecuente ≤ 3 toques; ideales ≤ 2 |

---

## 3. Problemas priorizados (resumen)

### P0 — Críticos

| ID | Problema | Propuesta |
|----|----------|-----------|
| P0-01 | Home launcher, no pendientes | Cola “Para hoy” + acciones rápidas |
| P0-02 | Features clave detrás de Más | 4 tabs: Inicio · Mi tiempo · Mis papeles · Perfil |
| P0-03 | Vacaciones duplicadas | Una sola entrada; Solicitudes = hub tipado |
| P0-04 | Estados técnicos en UI | Diccionario central `etiquetas-estado` |
| P0-05 | Errores con copy de desarrollo | Mensajes de producto |
| P0-06 | Ausencias + certificados densos | Flujos separados |
| P0-07 | Fichaje GPS sin guía | Estados claros + confirmación salida |
| P0-08 | Canvas firma chico | Área ≥ 220px + hint |
| P0-09 | Legales en listado recibos / JSON | Legales al firmar; PDF comprobante |
| P0-10 | Notificaciones sin deep link | Tap = destino + auto-leída |

### Quick wins (E0)

1. Diccionario de estados  
2. Skeletons de lista (no métricas por defecto)  
3. Copy de errores de producto  
4. Chip de estado reutilizable  
5. Piloto Recibos sin legales en subtítulo de lista  

Ver backlog completo: [BACKLOG_REDISENO_APP_MOVIL.md](./BACKLOG_REDISENO_APP_MOVIL.md).

---

## 4. Design System Mobile (contrato)

Principios: tranquilidad, confianza, claridad, cercanía, simplicidad, rapidez.

### Paleta (tokens existentes en `app-movil/src/_tokens.scss`)

| Token | Uso |
|-------|-----|
| Primario `#2BB89A` | CTA, progreso |
| Secundario `#0D7377` | Énfasis |
| Fondo `#F5F8F7` | Canvas |
| Texto `#14201C` | Body |
| Alerta / error / éxito | Chips y feedback |

### Tipografía

Plus Jakarta Sans. Body formularios ≥ 16px. Display saludo Home 28/700.

### Componentes E0+

| Componente | Estado |
|------------|--------|
| `ChipEstado` | E0 — implementado |
| `EstadoCarga` modos `lista` / `esqueleto` / `spinner` | E0 — implementado |
| Diccionario `etiquetas-estado` | E0 — implementado |
| Mensajes error producto | E0 — implementado |
| Botón 52 / bottom sheet / card pendiente | E1 |
| Canvas firma grande | E2–E3 |

### Bottom navigation objetivo (E1)

| Tab | Dominio |
|-----|---------|
| Inicio | Pendientes + acciones rápidas + novedades |
| Mi tiempo | Fichar · Vacaciones · Ausencias · Historial |
| Mis papeles | Recibos · Documentos · Firmas |
| Perfil | Datos · Notificaciones · Avisos · Equipos · Ayuda · Config |

Eliminar tabs “Más” y “Pedidos” como raíces.

### Home objetivo

```
Hola, Ana                    🔔
Martes 21 jul

PARA HOY
[ Firmar recibo junio     → ]
[ Documento: código ética → ]

ACCIONES RÁPIDAS
[Fichar] [Vacaciones] [Avisar falta] [Certificado]

NOVEDADES
Aviso: feriado …
```

---

## 5. Plan de ejecución

```
E0 Fundaciones → E1 Nav+Home → E2 Mi tiempo
                              ↘ E3 Mis papeles → E4 Perfil/confianza → E5 Delight
```

| Etapa | Duración | Entrega |
|-------|----------|---------|
| **E0** | 1–2 sprints | DS base, estados, errores, piloto Recibos |
| **E1** | 2 sprints | 4 tabs + Home pendientes + badges + deep links |
| **E2** | 2 sprints | Fichaje, vacaciones steps, ausencias/certificados |
| **E3** | 2 sprints | Recibos/docs/firma, legales en contexto |
| **E4** | 1–2 sprints | Perfil, login, splash, onboarding, ayuda |
| **E5** | 1 sprint | Microinteracciones, dark mode UI, widgets, quitar flag |

Hito “se siente otra app”: fin E1 + fichaje E2 (~6 semanas con foco).

---

## 6. Glosario de producto

| Evitar | Usar |
|--------|------|
| Pedidos / Solicitudes mezclados | **Solicitudes** |
| Comunicaciones | **Avisos** |
| Control de asistencia | **Fichar** / **Mi jornada** |
| Activos | **Mis equipos** |
| Legajo | **Mis datos** |
| `PENDIENTE` crudo | **Para firmar** / texto del diccionario |
| Republicación | “Tu equipo debe volver a publicar el recibo” |
| Justificada (toggle crudo) | “¿Tenés certificado o constancia?” |

---

## 7. Criterios de éxito

| Etapa | Prueba | Métrica |
|-------|--------|---------|
| E1 | “¿Qué harías primero?” | Pendiente visible &lt; 10s |
| E2 | Pedir vacaciones + fichar | Compleción sin ayuda ≥ 4/5 |
| E3 | Firmar recibo | &lt; 60s; estado comprendido |
| E4 | Primer login | Onboarding sin abandono |

Script de regresión por release: (1) fichar, (2) resolver pendiente, (3) vacaciones o certificado.

---

## 8. Qué no hacer

- Rediseñar visual sin cambiar navegación  
- Volver a un hub “Más” con 8 accesos  
- Reescribir en Flutter/RN sin necesidad  
- Mezclar este proyecto con cambios de OCR/Vertex  
- Esperar el DS perfecto antes de shippear Home  

---

## 9. Referencias de código

| Artefacto | Ruta |
|-----------|------|
| App móvil | `app-movil/` |
| Tokens | `app-movil/src/_tokens.scss` |
| Etiquetas estado | `app-movil/src/app/nucleo/utilidades/etiquetas-estado.ts` |
| Mensajes error | `app-movil/src/app/nucleo/utilidades/mensajes-error-producto.ts` |
| Chip estado | `app-movil/src/app/compartido/componentes/chip-estado/` |
| Flag nav | `environment.navegacionV2` |
| Backlog | [BACKLOG_REDISENO_APP_MOVIL.md](./BACKLOG_REDISENO_APP_MOVIL.md) |
| DS web (no copiar layout) | [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md) |
