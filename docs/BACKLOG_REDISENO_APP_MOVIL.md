# Backlog — Rediseño app móvil Ligo

Estados: `pendiente` · `en_curso` · `hecho` · `bloqueado`

Documento padre: [REDISENO_APP_MOVIL.md](./REDISENO_APP_MOVIL.md)  
Checklist PR: [CHECKLIST_PR_APP_MOVIL.md](./CHECKLIST_PR_APP_MOVIL.md)

---

## E0 — Fundaciones

| ID | Story | Estado | Notas |
|----|-------|--------|-------|
| E0-01 | Diccionario central de estados (recibos, docs, certificados, fichaje, solicitudes) | hecho | `etiquetas-estado.ts` |
| E0-02 | Componente `ChipEstado` (neutro / alerta / éxito / error / info) | hecho | `chip-estado` |
| E0-03 | `EstadoCarga`: modo `lista` por defecto; `esqueleto` solo dashboards | hecho | Inicio usa `esqueleto` |
| E0-04 | Mensajes de error de producto (sin puerto 8080 / iniciar.bat) | hecho | `mensajes-error-producto.ts` |
| E0-05 | Flag `navegacionV2` en environments | hecho | default `false` |
| E0-06 | Piloto Recibos: diccionario + skeleton lista + subtítulo sin legales | hecho | |
| E0-07 | ADR / decisión: app móvil = solo empleado este ciclo | hecho | en REDISENO §2 |
| E0-08 | Template PR: checklist touch ≥ 44pt + copy humano | hecho | `CHECKLIST_PR_APP_MOVIL.md` |
| E0-09 | Eventos analytics stub (`fichar_ok`, `recibo_firmado`, …) | hecho | `servicio-analitica` + cableado en login/fichaje/recibos/docs/vacaciones/certificados/home |
| E0-10 | Botón primario alto 52 + sheet acciones base | hecho | `--min-height: 52` global; ActionSheet en `tarjeta-fila` |

---

## E1 — Navegación + Home

| ID | Story | Estado |
|----|-------|--------|
| E1-01 | `LayoutTabsV2` — 4 tabs + feature flag | hecho |
| E1-02 | Hub Mi tiempo (shell + links a rutas existentes) | hecho |
| E1-03 | Hub Mis papeles (shell) | hecho |
| E1-04 | Hub Perfil ampliado (shell: avisos, notif, equipos, solicitudes) | hecho |
| E1-05 | Redirects desde `/tabs/mas` (si `navegacionV2`) | hecho |
| E1-06 | Home “Para hoy” (agregación cliente de pendientes) | hecho |
| E1-07 | Acciones rápidas Home (fichar, vacaciones, falta, certificado) | hecho |
| E1-08 | Badges en tabs (papeles / notificaciones) | hecho |
| E1-09 | Deep link desde notificación al destino | hecho |
| E1-10 | Banner “Se movió acá” one-time post-rollout | hecho |
| E1-11 | QA guards + permisos + rutas viejas | pendiente |

---

## E2 — Mi tiempo

| ID | Story | Estado |
|----|-------|--------|
| E2-01 | Fichaje: UI foco, copy GPS, confirmación salida, haptics | hecho (sin haptics nativos aún) |
| E2-02 | Guía permiso ubicación denegado → Ajustes | hecho |
| E2-03 | Vacaciones en steps (fechas → revisar → enviar) | hecho |
| E2-04 | LCT en lenguaje claro + CTA fecha sugerida | hecho |
| E2-05 | Eliminar formulario vacaciones duplicado en Solicitudes (deep link) | hecho |
| E2-06 | Ausencias: “Avisar que no voy” corto | hecho |
| E2-07 | Certificados: flujo separado + sheet Cámara/Galería/Archivo | hecho |
| E2-08 | Estados certificado humanos en UI | hecho |
| E2-09 | Historial fichaje con filtro mes | hecho |

---

## E3 — Mis papeles

| ID | Story | Estado |
|----|-------|--------|
| E3-01 | Recibos: segmented Pendientes / Firmados | hecho |
| E3-02 | Detalle recibo: legales colapsados + pantalla éxito | hecho |
| E3-03 | Comprobante PDF (dejar de ofrecer JSON al usuario) | hecho |
| E3-04 | Copy “Tu equipo debe volver a publicar…” (sin “republicación”) | hecho |
| E3-05 | Documentos: abrir = leído; firmar explícito | hecho |
| E3-06 | Canvas firma ≥ 220px + hint “Firmá con el dedo” | hecho |
| E3-07 | Acciones de fila: primaria + menú ⋯ (touch) | hecho | `tarjeta-fila` ActionSheet |
| E3-08 | Patrón compartido de confirmación / éxito de firma | hecho |

---

## E4 — Perfil y confianza

| ID | Story | Estado |
|----|-------|--------|
| E4-01 | Perfil hub completo | hecho |
| E4-02 | Login: show password | hecho |
| E4-03 | Olvidé mi contraseña (spike backend si falta) | hecho (UI + flujo RRHH; sin API reset aún) |
| E4-04 | Splash Ligo | hecho |
| E4-05 | Onboarding producto 3 slides | hecho |
| E4-06 | Onboarding laboral con deep links reales | hecho |
| E4-07 | Config: dark mode follow system, versión app | hecho |
| E4-08 | Ayuda / FAQ + contacto RRHH | hecho |
| E4-09 | Confirmación cerrar sesión | hecho |
| E4-10 | Gate RRHH móvil → mensaje “usá la web” | hecho |

---

## E5 — Delight

| ID | Story | Estado |
|----|-------|--------|
| E5-01 | Microinteracciones (fichaje, firma, éxito) | hecho |
| E5-02 | Haptics en flujos críticos | hecho (Vibration API) |
| E5-03 | Widgets / app shortcuts | diferido (requiere proyectos nativos sync) |
| E5-04 | Offline suave fichaje (cola) | diferido |
| E5-05 | Pase a11y VoiceOver/TalkBack | parcial (labels/aria en tabs, splash, tips) |
| E5-06 | Activar `navegacionV2` en prod + tip “Se movió acá” | hecho |

También: historial fichaje con filtro por mes (E2-09).


---

## Orden de arranque recomendado

1. Cerrar E0-08 … E0-10 si hace falta antes de E1  
2. E1-01 → E1-06 (navegación + Home)  
3. En paralelo tras E1: E2-01 y E3-01  
4. E4 cuando login/reset no bloqueen  
5. E5 al estabilizar  

---

## Definition of Done (por story)

- [ ] Copy en voseo argentino, sin enums crudos  
- [ ] Empty + loading + error contemplados  
- [ ] Touch targets ≥ 44pt en acciones  
- [ ] Probado en viewport móvil (o device)  
- [ ] Sin mensajes de infra (`8080`, `iniciar.bat`) en UI usuario  
