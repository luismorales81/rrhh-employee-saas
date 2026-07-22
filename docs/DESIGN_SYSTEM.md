# Ligo — Design System

Version: 2.0

---

# Filosofía del Producto

Ligo no es un software de RRHH tradicional.

Es la plataforma que conecta personas y empresas durante todo el ciclo laboral.

Cada pantalla debe transmitir:

- simplicidad
- cercanía
- confianza
- rapidez
- tranquilidad
- tecnología invisible

La aplicación debe ayudar al usuario a pensar menos.

La interfaz debe eliminar ruido.

Siempre priorizar simplicidad antes que cantidad de información.

Pregunta guía: ¿Hace que Ligo se sienta más simple, más cercano y más humano?

---

# Posicionamiento

Sí vender:

- La plataforma que conecta personas y empresas.
- Todo el ciclo laboral en un solo lugar.
- La forma más simple de gestionar personas.

No vender:

- Software de Recursos Humanos
- Suite empresarial / ERP
- Gestión de capital humano

---

# Principios de Diseño

## 1. Menos es más

Eliminar todo elemento que no aporte valor.

No agregar decoraciones innecesarias.

Cada componente debe tener un propósito.

---

## 2. Escaneable en 5 segundos

El usuario debe entender una pantalla en menos de cinco segundos.

Debe identificar inmediatamente:

- dónde está
- qué puede hacer
- cuál es la acción principal

---

## 3. Una acción principal

Cada pantalla debe tener una única acción primaria.

Nunca competir entre varios botones destacados.

---

## 4. Aire

El espacio vacío también es diseño.

No llenar pantallas.

Preferimos una pantalla larga antes que una pantalla saturada.

---

# Personalidad Visual

La aplicación debe sentirse:

✔ moderna

✔ minimalista

✔ cercana

✔ confiable

✔ latinoamericana

Debe evitar verse:

✖ burocrática

✖ gubernamental

✖ empresarial pesada

✖ SAP / ERP tradicional

---

# Tono de comunicación

Cercano, optimista, con voseo argentino.

Preferir:

- Tus documentos, siempre disponibles.
- Pedí tus vacaciones en segundos.
- Registrá tu ingreso desde la app.

Evitar:

- Optimice procesos
- Gestión documental
- Control de asistencia
- Suite / ERP / capital humano

---

# Inspiración

Tomar inspiración únicamente de UX (simplicidad y claridad).

No copiar estilos.

Referencias de experiencia:

- Linear
- Notion
- Slack
- Mercado Pago / Nubank / Ualá (cercanía latinoamericana)

---

# Tecnología UI (implementación actual)

Angular 19 (standalone)

TypeScript

SCSS global + tokens CSS

Lucide Icons (`@lucide/angular`)

App móvil: Ionic + Capacitor

**No se usa en este proyecto:** Tailwind, shadcn/ui, Framer Motion, React.

Los principios visuales de este documento se aplican mediante clases compartidas en `frontend/src/styles.scss` y variables en `frontend/src/_tokens.scss`.

---

# Colores

Fuente de verdad: `frontend/src/_tokens.scss` (espejo en `app-movil/src/_tokens.scss`).

## Primario — menta / esmeralda suave

`#2bb89a`

Representa conexión, crecimiento y tranquilidad.

Usar para:

- botones principales
- enlaces importantes
- estados positivos / ítems activos

## Secundario — turquesa / azul petróleo

`#0d7377`

Para información, links secundarios y énfasis calmado.

## Acento — coral suave

`#f28b82`

Para highlights amables y acentos de marca (no para errores críticos).

## Base

- Fondo: `#f7faf9`
- Superficie: `#ffffff`
- Texto: `#1f2937`
- Texto secundario: `#6b7280`
- Bordes: `#e5ebe9`

Evitar azul corporativo oscuro y grises pesados.

---

## Estados

Success — menta suave

Warning — naranja suave

Error — rojo suave

Info — turquesa secundario

Nunca utilizar colores saturados agresivos.

---

# Tipografía

Fuente:

**Plus Jakarta Sans**

Jerarquía:

Display

Heading XL

Heading L

Heading M

Body

Small

Caption

Labels

Nunca utilizar más de dos pesos tipográficos en una misma sección.

---

# Espaciado

Sistema basado en múltiplos de 8.

4 / 8 / 16 / 24 / 32 / 40 / 48 / 64

Nunca inventar medidas arbitrarias.

---

# Bordes

Radios más generosos que en v1 (suaves, amigables):

- Controles: `--radio-sm` / `--radio-md`
- Tarjetas: `--radio-lg`
- Diálogos / hero: `--radio-xl`

Todo debe sentirse consistente.

---

# Sombras

Muy suaves.

No utilizar sombras oscuras.

La elevación debe ser casi imperceptible.

---

# Iconografía

Lucide exclusivamente.

Nunca mezclar estilos.

Siempre 20 o 24 px.

Marca: `assets/marca/ligo-icono.svg` y `ligo-logo.svg`.

---

# Botones

Tipos

Primary

Secondary

Ghost

Outline

Danger

Solo un botón Primary por pantalla.

---

# Cards

Las cards son el componente principal para interacciones y agrupación de contenido.

No utilizar cajas dentro de cajas.

---

# Tablas

Las tablas deben ser livianas.

Evitar líneas innecesarias.

Mayor separación entre filas.

Hover suave.

Filtros arriba.

Búsqueda visible.

Acciones agrupadas.

No utilizar más de 8 columnas visibles.

El panel principal no debe ser una tabla enorme.

---

# Formularios

Agrupar por bloques.

No crear formularios infinitos.

Cada sección debe tener:

Título

Descripción

Contenido

Los campos relacionados deben vivir juntos.

Siempre mostrar validaciones claras.

---

# Navegación Web

Sidebar izquierda (RRHH / plataforma).

Header + navegación inferior (portal empleado).

El empleado debe sentir que es “su espacio”.

---

# App del empleado

Debe sentirse como una aplicación personal.

Tranquilidad, claridad, todo a un clic.

---

# Panel RRHH

Orden y baja carga cognitiva.

Priorizar pendientes, alertas, solicitudes y vencimientos.

---

# Archivos clave

| Archivo | Rol |
|---------|-----|
| `frontend/src/_tokens.scss` | Tokens de diseño web |
| `frontend/src/styles.scss` | Sistema de componentes |
| `frontend/src/index.html` | Título, fuente, favicon |
| `frontend/src/assets/marca/` | Logo e icono Ligo |
| `app-movil/src/_tokens.scss` | Tokens móvil |
| `app-movil/src/theme/variables.scss` | Mapeo Ionic |

---

# Taglines

- Ligo — Conectando personas y empresas.
- Ligo — Todo el ciclo laboral en un solo lugar.
- Ligo — La forma simple de gestionar personas.
- Ligo — Más conexión. Menos burocracia.
