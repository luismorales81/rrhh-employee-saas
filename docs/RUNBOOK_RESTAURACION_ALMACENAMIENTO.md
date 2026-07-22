# Runbook: restauración de almacenamiento y base de datos

**Versión:** 1.0  
**Uso:** recuperación ante pérdida de datos o verificación de backups (CP-01)

## Prerrequisitos

- Acceso al servidor o entorno de staging.
- Copia de backup de PostgreSQL (`pg_dump` o snapshot gestionado).
- Copia de la carpeta de archivos (`data/archivos`) o snapshot del bucket S3/MinIO.

## Escenario A — Desarrollo / demo (disco local)

### 1. Detener el backend

```bat
scripts\liberar-puertos.bat
```

### 2. Restaurar base de datos

```bash
# Ejemplo con PostgreSQL local
dropdb -h localhost -U rrhh rrhh_saas
createdb -h localhost -U rrhh rrhh_saas
psql -h localhost -U rrhh -d rrhh_saas -f backup\rrhh_saas_YYYYMMDD.sql
```

### 3. Restaurar archivos

```bat
rmdir /s /q backend\data\archivos
xcopy /E /I backup\archivos_YYYYMMDD backend\data\archivos
```

### 4. Arrancar y verificar

```bat
iniciar.bat
```

- Login `rrhh@demo.local` → `/recibos` → abrir un recibo y descargar PDF.
- Revisar logs del job de integridad o ejecutar manualmente la verificación en staging.

## Escenario B — Producción (S3/MinIO + PostgreSQL gestionado)

1. Restaurar snapshot de BD al punto temporal requerido.
2. Restaurar bucket desde versión o réplica cross-region.
3. Confirmar que `archivo_almacenado.clave_almacenamiento` coincide con objetos en bucket.
4. Ejecutar verificación de integridad (job nocturno o endpoint de administración futuro).
5. Documentar incidente y resultado en ticket de operaciones.

## Verificación post-restauración

| Check | Criterio |
|-------|----------|
| Recibos listados | `GET /api/recibos` devuelve registros esperados |
| PDF accesible | `GET /api/recibos/{id}/pdf` responde 200 |
| Hash íntegro | Sin eventos `RECIBO_INTEGRIDAD_FALLIDA` en auditoría |
| Expediente | `GET /api/recibos/{id}/expediente` genera ZIP válido |

## Script de backup (desarrollo)

Ver `scripts/backup-almacenamiento.bat` para copia de referencia de carpeta de archivos.

## Contactos

Definir en operaciones: responsable de backups, ventana de restauración y escalamiento legal/RRHH.
