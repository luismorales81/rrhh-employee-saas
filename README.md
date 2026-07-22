# Ligo

La plataforma que conecta personas y empresas durante todo el ciclo laboral.

Incluye portal web para equipos, espacio del empleado y app móvil Ionic.

## Requisitos

- **Java 17+** y Maven 3.9+
- **Node.js 20+** y npm 10+
- **Docker** (opcional, para PostgreSQL local)

## Arranque rápido

```bat
iniciar.bat
```

| Servicio | URL |
|----------|-----|
| Backend API | http://localhost:8080 |
| Frontend web | http://localhost:4200 |
| App móvil | http://localhost:4201 |

Guía de demo y credenciales: [docs/DEMO_GUIA.md](docs/DEMO_GUIA.md)

## Estructura del proyecto

```
backend/       Spring Boot 3, API REST, Flyway, JWT
frontend/      Angular — panel de equipo y espacio del empleado
app-movil/     Ionic/Angular — experiencia móvil
docs/          Especificaciones y roadmap
scripts/       Scripts de arranque por componente
```

## Comandos útiles

### Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

### Frontend web

```bash
cd frontend
npm install
npm start          # :4200
npm run build
npm test
```

### App móvil

```bash
cd app-movil
npm install
npm start          # :4201
npm run build
```

### Liberar puertos

```bat
scripts\liberar-puertos.bat
```

## Documentación

- [Guía de demostración](docs/DEMO_GUIA.md)
- [Roadmap MVP/V1](docs/24_MVP_ROADMAP.md)
- [Reglas de negocio](docs/11_BUSINESS_RULES.md)
- [Diseño de base de datos — eliminación lógica](docs/16_DATABASE_DESIGN.md#soft-delete-eliminación-lógica)

## Eliminación lógica (soft delete)

Todas las operaciones de **eliminar** o **desactivar** en la API son **eliminaciones lógicas**: los registros permanecen en base de datos con `is_deleted = true` y no se muestran en consultas habituales. Excepción relevante: **desactivar empleado** lo deja en estado `DESVINCULADO` (sin soft delete) para conservar legajo y offboarding. Detalle completo en [docs/16_DATABASE_DESIGN.md](docs/16_DATABASE_DESIGN.md).
