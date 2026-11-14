# Proposal

## Why

El bot de reservas necesita saber cuándo se puede reservar y para cuántas personas, pero hoy el proyecto no tiene esquema de base de datos ni migraciones. Estas reglas deben vivir en base de datos para que el futuro panel de administración pueda cambiarlas sin desplegar. Por eso hay que montar ahora esa base, antes del flujo de conversación que la va a consumir.

## What Changes

- Incorporar Flyway al proyecto como única vía de gestión del esquema y dejar de generar el esquema con Hibernate (`ddl-auto: update` pasa a `validate`).
- Crear el modelo de datos de configuración de reservas:
  - Franjas horarias de reserva por día de la semana (apertura, cierre y última hora de reserva), con varias franjas posibles por día.
  - Ajustes generales de reserva: máximo de personas por reserva y un mínimo opcional (vacío = sin mínimo), pensado para el panel futuro.
- Cargar los valores iniciales con una migración de Flyway:
  - De martes a domingo: apertura 13:00, cierre 23:00, última reserva 22:30.
  - Lunes: sin franjas, es decir, cerrado.
  - Máximo de 15 personas por reserva y sin mínimo.
- Exponer a nivel de dominio un servicio de solo lectura que, leyendo siempre de base de datos, responda:
  - si una fecha y hora (hora local de España) se puede reservar: no puede estar en el pasado y debe caer dentro del horario;
  - si un número de personas se admite, es inválido o debe derivarse a un humano por superar el máximo.

Fuera de alcance: el panel de administración, los cierres por fechas concretas (festivos, vacaciones), la entidad de reserva en sí, el flujo de conversación por WhatsApp y el control de disponibilidad por mesas o aforo total.

## Capabilities

### New Capabilities
- `reservation-rules`: reglas que determinan si una reserva solicitada es admisible, es decir, horario de reservas por día de la semana y límites de personas por reserva. Todo se configura en base de datos y se carga inicialmente con Flyway.

### Modified Capabilities
<!-- Ninguna: no existen specs previas en openspec/specs/. -->

## Impact

- **Dependencias (`pom.xml`)**: Flyway para PostgreSQL y, para los tests de integración, Testcontainers con PostgreSQL. Ambas se justifican en `design.md` y requieren aprobación antes de tocar el `pom.xml`.
- **Configuración**: en `application.yml`, `spring.jpa.hibernate.ddl-auto` pasa de `update` a `validate`. Se elimina `application.properties`, que duplica `spring.application.name`.
- **Base de datos**: nuevas tablas y migraciones en `src/main/resources/db/migration/`.
- **Código**: nuevo paquete `reservation` (entidades, repositorios y servicio de reglas). No afecta al webhook de WhatsApp.
- **Tests**: el test de arranque de contexto pasa a necesitar una base de datos PostgreSQL (Testcontainers).
