# Checklist PR — App móvil Ligo

Usar en todo cambio de `app-movil/` durante el rediseño.

## UX / copy

- [ ] Textos en voseo argentino
- [ ] Sin enums crudos en UI (`PENDIENTE`, `EN_PROCESAMIENTO`, …) — usar `etiquetas-estado`
- [ ] Sin mensajes de infra (`8080`, `iniciar.bat`, paths locales)
- [ ] Empty + loading + error contemplados en pantallas tocadas

## Interacción

- [ ] Acciones táctiles ≥ 44px de alto
- [ ] Una acción primaria clara por pantalla / fila
- [ ] CTA Reintentar si hay error de red

## Técnico

- [ ] Componentes nuevos en español (identificadores)
- [ ] Feature flag `navegacionV2` no hardcodeado a true en prod sin QA
- [ ] Probado en viewport móvil (DevTools o device)

## Referencias

- [REDISENO_APP_MOVIL.md](./REDISENO_APP_MOVIL.md)
- [BACKLOG_REDISENO_APP_MOVIL.md](./BACKLOG_REDISENO_APP_MOVIL.md)
