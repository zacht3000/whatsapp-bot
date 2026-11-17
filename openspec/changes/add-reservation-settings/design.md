# Design

## Context

El proyecto es un esqueleto de Spring Boot 4.1 (Java 27) con JPA y el driver de PostgreSQL. Su estado actual:

- No hay entidades, migraciones ni la carpeta `db/migration`.
- `application.yml` activa `spring.flyway.enabled`, pero Flyway no está en el `pom.xml`, así que hoy no se aplica nada.
- `ddl-auto: update` hace que Hibernate modifique el esquema por su cuenta, lo que choca con gestionarlo mediante migraciones.
- El único test (`WhatsappBotApplicationTests.contextLoads`) arranca el contexto completo y necesita una base de datos accesible.
- El paquete previsto para el dominio es `com.restaurante.whatsappbot.reservation` (`domain/`, `service/`, `repository/`), según `AGENTS.md`.

Motivación y alcance: ver `proposal.md`. Requisitos: ver `specs/reservation-rules/spec.md`.

## Goals / Non-Goals

**Goals:**
- Que Flyway sea la única fuente del esquema y Hibernate solo lo valide.
- Un modelo de datos que el futuro panel pueda editar tal cual: varias franjas por día y un mínimo opcional.
- Que la lógica de reglas sea pura y fácil de probar sin base de datos.

**Non-Goals:**
- Caché de la configuración: se lee en cada consulta (el volumen esperado es bajo).
- Franjas que crucen la medianoche.
- Antelación mínima o máxima de reserva (por ejemplo, "al menos con 1 hora" o "como mucho a 2 meses vista").
- Granularidad de horas (por ejemplo, solo cada 15 o 30 minutos).

## Decisions

### 1. Dependencias nuevas
- `spring-boot-starter-flyway` y `org.flywaydb:flyway-database-postgresql`. En Spring Boot 4 la autoconfiguración de Flyway está en su propio módulo, así que hace falta el starter. Desde Flyway 10 el soporte de PostgreSQL va en un artefacto aparte.
- Para tests: `spring-boot-testcontainers` y el módulo PostgreSQL de Testcontainers. Las versiones las gestiona el BOM de Spring Boot; los nombres exactos de los artefactos se comprueban contra ese BOM al implementar.
- *Alternativa descartada:* H2 en memoria para tests. Las migraciones usan SQL de PostgreSQL (tipos `time`, `CHECK`, `smallint`) y `ddl-auto: validate` compara con el esquema real, así que H2 daría falsos positivos y negativos.

### 2. Esquema
```sql
reservation_time_slot (
  id               bigint generated always as identity primary key,
  day_of_week      smallint not null check (day_of_week between 1 and 7), -- ISO-8601: 1 = lunes
  opens_at         time not null,
  closes_at        time not null,
  last_booking_at  time not null,
  check (opens_at <= last_booking_at and last_booking_at <= closes_at)
)
index on (day_of_week)

reservation_settings (
  id              smallint primary key check (id = 1),   -- fila única
  max_party_size  integer not null check (max_party_size >= 1),
  min_party_size  integer null check (min_party_size >= 1),
  check (min_party_size is null or min_party_size <= max_party_size)
)
```
- **Varias franjas por día, sin `unique(day_of_week)`.** Así el panel podrá crear turnos partidos (comida y cena) sin migrar el esquema.
- **Un día cerrado es un día sin filas**, en lugar de una columna `closed`. Evita estados contradictorios, como "cerrado pero con horas". Los cierres por fecha concreta (festivos) serán una tabla aparte en el futuro.
- **`reservation_settings` es una tabla de fila única** (`id = 1`) con columnas tipadas, en lugar de un clave/valor genérico, para tener restricciones `CHECK` y tipos reales. Si mañana hace falta otro ajuste, se añade una columna con su migración.
- `day_of_week` se guarda con la numeración ISO para mapear directamente a `java.time.DayOfWeek`.

### 3. Migraciones
- `V1__create_reservation_rules_tables.sql`: crea las dos tablas.
- `V2__seed_reservation_rules.sql`: inserta seis franjas de martes (2) a domingo (7), de 13:00 a 23:00 con última reserva a las 22:30, y la fila `(1, 15, null)`.
- Esquema y datos van separados para que el panel pueda sustituir los datos iniciales sin tocar V1.

### 4. Código (paquete `reservation`)
- `domain/`: entidades JPA `ReservationTimeSlot` y `ReservationSettings`, más los resultados como `enum`:
  - `ReservationTimeCheck { ALLOWED, PAST, OUTSIDE_HOURS, CLOSED_DAY }`
  - `PartySizeCheck { ALLOWED, REQUIRES_HUMAN, INVALID }`
- `repository/`: `ReservationTimeSlotRepository.findByDayOfWeek(...)` y `ReservationSettingsRepository`. Si falta la fila de ajustes, se lanza un error explícito de configuración en lugar de usar valores por defecto.
- `service/ReservationRulesService` (de solo lectura, `@Transactional(readOnly = true)`):
  - `checkDateTime(LocalDateTime)`: la fecha y hora llegan en hora local de Europe/Madrid. Devuelve primero `PAST` si no es posterior a `LocalDateTime.now(clock)`; si no, `CLOSED_DAY` si el día no tiene franjas, `ALLOWED` si cae en alguna franja (`opensAt <= t <= lastBookingAt`) y `OUTSIDE_HOURS` en otro caso.
  - `checkPartySize(int)`: devuelve `INVALID` si es menor que el mínimo efectivo (`min ?? 1`), `REQUIRES_HUMAN` si supera el máximo y `ALLOWED` en otro caso.
- Se usan enums en lugar de excepciones porque el flujo de conversación necesita ramificar según el motivo para contestar al cliente; no son errores.
- La zona horaria es una constante del dominio (`ZoneId.of("Europe/Madrid")`), expuesta para que el flujo de conversación convierta los instantes. No va en base de datos porque no es un dato editable por el negocio.
- **Se inyecta un `java.time.Clock`** (un bean `Clock.system(Europe/Madrid)` en `config/`) en lugar de llamar a `LocalDateTime.now()` directamente. Así los tests fijan el "ahora" con `Clock.fixed(...)` y los escenarios de fechas pasadas son deterministas. *Alternativa descartada:* pasar el "ahora" como parámetro, porque obliga a cada llamador a acordarse de la zona horaria.
- **`PAST` se comprueba antes que el horario.** A un cliente que pide un lunes pasado se le dice que la fecha ya pasó, no que el lunes cerramos.

### 5. Configuración
- `ddl-auto: validate`.
- Se mantiene `application.properties`: repite `spring.application.name` con el mismo valor que `application.yml` y no afecta al comportamiento.

### 6. Estrategia de tests
- **Unitarios** de `ReservationRulesService`, con repositorios simulados con Mockito. Cubren todos los escenarios de la spec, incluidos los límites 13:00, 22:30, 12:59 y 22:31.
- **Integración** con `@DataJpaTest` y Testcontainers PostgreSQL. Comprueban que las migraciones aplican, que los datos iniciales coinciden con la spec, que las restricciones `CHECK` rechazan datos incoherentes y que un cambio en base de datos se refleja en la siguiente consulta.
- `contextLoads` pasa a usar el mismo contenedor mediante `@ServiceConnection`.

## Risks / Trade-offs

- [Los tests necesitan Docker] → Hay que tener Docker Desktop arrancado para `./mvnw test`. Se documenta en `AGENTS.md`. Es la contrapartida de probar contra PostgreSQL real.
- [`ddl-auto: validate` falla al arrancar si una entidad no cuadra con el esquema] → Es el comportamiento buscado: detecta los desajustes pronto.
- [Ninguna franja puede cruzar la medianoche] → Con el horario actual (cierre a las 23:00) no hace falta. Si algún día se necesita, se cambia la restricción y la comparación.
- [Cambio de hora (horario de verano/invierno): una hora local puede no existir o repetirse] → Las franjas de reserva (13:00–22:30) no coinciden con las horas del cambio (2:00–3:00), así que no afecta.
- [Si falta la fila de ajustes, las consultas de personas fallan] → La crea la migración V2 y se cubre con un test de integración.

## Migration Plan

1. Al arrancar, Flyway aplica V1 y V2 sobre una base vacía. Ahora mismo no hay datos ni tablas previas que conservar.
2. Si una base local ya tiene tablas creadas por `ddl-auto: update`, se borra y se recrea. No hay entornos productivos todavía.
3. Marcha atrás: revertir el commit y borrar la base local. No hacen falta migraciones de deshacer mientras no haya producción.
