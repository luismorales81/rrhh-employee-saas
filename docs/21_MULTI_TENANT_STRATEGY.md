# MULTI TENANT STRATEGY

## Principio

Una empresa nunca debe ver información de otra.

---

## Tenant

CompanyId

Es obligatorio.

---

## Seguridad

Toda consulta debe incluir CompanyId.

---

## Auditoría

Toda auditoría debe almacenar CompanyId.

---

## Archivos

Todos los documentos deben almacenarse separados por empresa.

Ejemplo:

company-1/documents

company-2/documents

---

## Caché

La caché debe aislar información por empresa.

---

## IA

La IA nunca puede consultar información de otra empresa.