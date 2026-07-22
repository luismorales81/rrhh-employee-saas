# Procedimiento de acceso alternativo a recibos de sueldo

**Versión:** 1.0  
**Última actualización:** julio 2026  
**Norma:** Ley 27.802 — principio de no discriminación de trabajadores sin acceso digital  
**Relacionado:** [DECRETO_407_2026_CHECKLIST.md](DECRETO_407_2026_CHECKLIST.md) (CP-14)

---

## Objetivo

Garantizar que **todos los trabajadores** puedan recibir, revisar y dejar constancia de la recepción (o disconformidad) de sus recibos de sueldo, aunque no dispongan de smartphone, PC o conectividad en su domicilio.

---

## Ámbito

Aplica a empleados de empresas que usan Ligo y que, por cualquier motivo, no puedan completar el flujo digital de forma autónoma:

- Sin dispositivo compatible
- Sin conectividad estable
- Baja alfabetización digital
- Situaciones transitorias (avería, viaje, etc.)

---

## Canales alternativos (elegir uno o combinar)

### Opción A — Kiosco en planta (recomendada)

1. La empresa habilita un **PC o tablet** en sector de RRHH o comedor con acceso al portal empleado.
2. RRHH comunica al trabajador la disponibilidad del recibo (cartelera, supervisor, etc.).
3. El trabajador ingresa con sus credenciales **o** RRHH asiste con sesión supervisada (sin compartir contraseña: el empleado la ingresa).
4. Se registra visualización y firma en el sistema con la misma trazabilidad que el acceso remoto.
5. RRHH verifica en el listado que el estado pasó a `FIRMADO` o `DISCONFORMIDAD`.

**Registro adicional:** anotar en planilla interna fecha, legajo y si hubo asistencia de RRHH.

### Opción B — RRHH asistido (mesa de ayuda)

1. El empleado se presenta en RRHH con DNI.
2. RRHH verifica identidad y abre el legajo del empleado en `/empleados/{id}/legajo` → sección Recibos.
3. Se imprime el PDF del recibo vigente si el trabajador lo solicita.
4. El empleado firma en el sistema desde un equipo de RRHH (mismo flujo que kiosco).
5. Si hay disconformidad, se registra el comentario en presencia del empleado.

### Opción C — Constancia en soporte físico (excepcional)

Cuando no sea posible el acceso digital en un plazo razonable:

1. RRHH entrega **ejemplar impreso** del recibo firmado por el empleador.
2. El trabajador firma conformidad o disconformidad en el papel con fecha y aclaración.
3. RRHH escanea el documento firmado y lo carga como archivo en el legajo documental (módulo Documentos), vinculado al período.
4. En el sistema digital se publica un recibo marcado como gestionado por canal alternativo (nota en legajo).

> La opción C **no reemplaza** el expediente digital del recibo original publicado por RRHH; complementa la constancia física exigida por el proceso interno.

---

## Flujo resumido

```
RRHH publica recibo (digital)
        │
        ├─► Empleado con acceso → /mis-recibos o app móvil
        │
        └─► Sin acceso digital → Opción A, B o C
                    │
                    └─► Constancia registrada (digital y/o física archivada)
```

---

## Responsabilidades

| Rol | Responsabilidad |
|-----|-----------------|
| RRHH | Detectar empleados sin acceso, ofrecer canal alternativo, conservar evidencia |
| Supervisor | Derivar al empleado al kiosco o RRHH cuando corresponda |
| Empleador | Proveer medios razonables (equipo, horario, asistencia) |
| Sistema | Registrar eventos con IP/user-agent cuando la firma es digital (incluso en kiosco) |

---

## Configuración recomendada en producción

| Parámetro | Valor sugerido |
|-----------|----------------|
| `app.correo.habilitado` | `true` si hay email laboral masivo |
| `app.recibos.dias-alerta-pendiente` | Ajustar según política interna / criterio legal |
| Kiosco | Navegador en modo incógnito o cierre de sesión automático tras firma |

---

## Indicadores de seguimiento

- Recibos en estado `PENDIENTE` / `VISTO` por más de N días → widget CP-08 en panel RRHH
- Listado filtrado por empleados sin usuario vinculado (revisar en módulo Empleados)
- Reenvío de notificación: botón **Reenviar aviso** en `/recibos`

---

## Revisión y mejora

- Revisar este procedimiento ante cambios del Decreto 407/2026 o políticas internas.
- Incorporar lecciones aprendidas de inspecciones o auditorías.
- Actualizar [DECRETO_407_2026_CHECKLIST.md](DECRETO_407_2026_CHECKLIST.md) si el reglamento exige plazos o canales específicos.
