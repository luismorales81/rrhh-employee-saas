# Ligo UI — Design System

Fuente de verdad visual del producto Ligo.  
Implementación: tokens en `frontend/src/_tokens.scss` y componentes en `frontend/src/app/ui/`.

## Principios

1. Una acción primaria por vista.
2. Espacio > bordes.
3. Color con propósito (menta guía; coral solo acento/alerta).
4. Lenguaje humano (nunca códigos de sistema).
5. Mismo Ligo en web, portal y móvil.

## Paleta

| Token | Claro | Uso |
|-------|-------|-----|
| `--color-primario` | `#2BB89A` | CTA, focus, links |
| `--color-primario-hover` | `#229E84` | Hover primario |
| `--color-primario-pressed` | `#1B8670` | Pressed |
| `--color-primario-suave` | `#ECFAF6` | Fondos selected |
| `--color-secundario` | `#0D7377` | Marca profunda |
| `--color-acento` | `#F28B82` | Acento humano |
| `--color-fondo` | `#F5F8F7` | Fondo app |
| `--color-superficie` | `#FFFFFF` | Cards / paneles |
| `--color-texto` | `#14201C` | Texto principal |
| `--color-texto-secundario` | `#5B6B66` | Texto secundario |
| `--color-borde` | `#E2EAE7` | Bordes |
| `--color-exito-*` | ver tokens | Éxito |
| `--color-alerta-*` | ver tokens | Warning |
| `--color-error-*` | ver tokens | Error |
| `--color-info-*` | ver tokens | Info |

Dark mode: activar con `data-tema="oscuro"` en `<html>` (tokens listos, UI no forzada).

## Tipografía

- Familia: **Plus Jakarta Sans**
- Escala: Display 32 · H1 24 · H2 20 · H3 16 · Body 14 · Caption 12
- Pesos: 400 body · 500 UI · 600 títulos · 700 marca

## Espaciado

Base 4px; layout preferir múltiplos de 8.  
Sidebar 240px / rail 64px. Contenido RRHH max 1280px. Portal 720px.

## Componentes (`app/ui/`)

| Componente | Selector | Rol |
|------------|----------|-----|
| Boton | `ui-boton` | Primario / secundario / peligro / fantasma |
| BotonIcono | `ui-boton-icono` | Acciones icon-only |
| Insignia | `ui-insignia` | Badges de estado |
| Avatar | `ui-avatar` | Iniciales / imagen |
| Campo | `ui-campo` | Input con label y error |
| Cajon | `ui-cajon` | Drawer lateral |
| Dialogo | `ui-dialogo` | Modal centrado |
| EstadoVacio | `ui-estado-vacio` | Empty states |
| EstadoError | `ui-estado-error` | Error states |
| Esqueleto | `ui-esqueleto` | Loading skeleton |
| Migas | `ui-migas` | Breadcrumbs |
| PaletaComandos | `ui-paleta-comandos` | Command palette Ctrl+K |
| Alerta | `ui-alerta` | Banner inline |
| Pestanas | `ui-pestanas` | Tabs |

## Estados de interacción

| Estado | Regla |
|--------|-------|
| Hover | 120–160ms ease |
| Focus | ring `0 0 0 3px` primario-200 |
| Loading | skeleton en listas; spinner en botón |
| Disabled | opacity 0.45 |
| Selected | fondo primario-suave |
| Error | mensaje bajo el campo |

## Do / Don’t

- **Do:** una CTA primaria; drawers para formularios; etiquetas humanas de estado.
- **Don’t:** formularios inline que empujan tablas; enums crudos; bottom-nav > 5 ítems.
