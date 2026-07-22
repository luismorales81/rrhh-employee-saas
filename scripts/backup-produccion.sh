#!/usr/bin/env bash
# Backup de referencia para producción / staging
# Uso: ./scripts/backup-produccion.sh [contenedor_postgres]

set -euo pipefail

CONTENEDOR="${1:-rrhh-postgres}"
DESTINO="backup/prod_$(date +%Y%m%d_%H%M%S)"

echo "=== Backup producción Ligo ==="
mkdir -p "$DESTINO"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: Docker no encontrado."
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -qx "$CONTENEDOR"; then
  echo "ERROR: Contenedor $CONTENEDOR no está en ejecución."
  exit 1
fi

echo "[1/2] Volcado PostgreSQL..."
docker exec -t "$CONTENEDOR" pg_dump -U rrhh -d rrhh_saas --no-owner --no-acl > "$DESTINO/rrhh_saas.sql"

echo "[2/2] Almacenamiento local (si existe)..."
if [ -d "backend/data/archivos" ]; then
  cp -a backend/data/archivos "$DESTINO/archivos"
else
  echo "Sin backend/data/archivos — en prod usar snapshot del bucket S3/MinIO."
fi

echo "Backup completado en $DESTINO"
