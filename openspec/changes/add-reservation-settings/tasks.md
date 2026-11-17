# Tasks

## 1. Infraestructura: Flyway y tests con PostgreSQL

- [x] 1.1 Pedir aprobación y añadir al `pom.xml` `spring-boot-starter-flyway`, `flyway-database-postgresql`, `spring-boot-testcontainers` (test) y el módulo PostgreSQL de Testcontainers (test), comprobando los nombres de los artefactos contra el BOM de Spring Boot 4.1; verificar con `./mvnw dependency:tree` que se resuelven
- [x] 1.2 Cambiar `spring.jpa.hibernate.ddl-auto` a `validate` en `application.yml`, sin tocar `application.properties`; verificar que `spring.application.name` tiene el mismo valor en ambos ficheros
- [x] 1.3 Crear una configuración de test con `@ServiceConnection` sobre un `PostgreSQLContainer` y hacer que `WhatsappBotApplicationTests.contextLoads` la use; verificar que `./mvnw test` pasa con Docker arrancado
- [x] 1.4 Documentar en `AGENTS.md` (sección Comandos/Stack) que los tests requieren Docker y rellenar el stack real (Java 27, Spring Boot 4.1, PostgreSQL + JPA + Flyway, JUnit 5 + Mockito + Testcontainers); verificar que no queda ningún marcador `[...]` en la sección Stack

## 2. Esquema y datos iniciales

- [x] 2.1 Escribir un test de integración (`@DataJpaTest` + Testcontainers) que espere las franjas iniciales (martes a domingo, 13:00/23:00/22:30), que no haya franjas el lunes y que los ajustes sean `max = 15` y `min = null`; verificar que falla
- [x] 2.2 Crear `db/migration/V1__create_reservation_rules_tables.sql` con `reservation_time_slot` y `reservation_settings`, con sus restricciones `CHECK` e índice, según `design.md`
- [x] 2.3 Crear `db/migration/V2__seed_reservation_rules.sql` con las seis franjas y la fila de ajustes `(1, 15, null)`; verificar que el test de 2.1 pasa
- [x] 2.4 Añadir tests de integración que comprueben que la base rechaza datos incoherentes (última reserva posterior al cierre, `day_of_week = 8`, `max_party_size = 0`, `min > max`, una segunda fila de ajustes); verificar que pasan

## 3. Modelo JPA y repositorios

- [x] 3.1 Crear las entidades `ReservationTimeSlot` y `ReservationSettings` en `reservation/domain` (con `DayOfWeek` mapeado a número ISO) y los repositorios en `reservation/repository` (`findByDayOfWeek`); verificar que el contexto arranca con `ddl-auto: validate` (`contextLoads` en verde)
- [x] 3.2 Añadir un test de integración de repositorio que lea las franjas de un día y la fila de ajustes sembradas por Flyway; verificar que pasa

## 4. Servicio de reglas

- [x] 4.1 Crear los enums `ReservationTimeCheck` (incluido `PAST`) y `PartySizeCheck`, la constante de zona horaria Europe/Madrid en `reservation/domain` y el bean `Clock` en `config/`; verificar que compila y que `contextLoads` sigue en verde
- [x] 4.2 Escribir tests unitarios de `ReservationRulesService.checkDateTime` (con Mockito y `Clock.fixed`) para todos los escenarios de las specs "Rechazo de fechas pasadas" (día anterior, hora pasada del mismo día, hora futura del mismo día, lunes pasado → `PAST`) y "Admisión de fecha y hora de reserva" (sábado 21:00, miércoles 13:00 y 22:30, jueves 12:59, viernes 22:31, lunes 14:00), más el día sin franjas; verificar que fallan
- [x] 4.3 Implementar `checkDateTime` en `reservation/service/ReservationRulesService`; verificar que pasan los tests de 4.2
- [x] 4.4 Escribir tests unitarios de `checkPartySize` (1, 15, 16, 0, negativo, mínimo configurado mayor que 1) y del error explícito cuando falta la fila de ajustes; verificar que fallan
- [x] 4.5 Implementar `checkPartySize`; verificar que pasan los tests de 4.4

## 5. Verificación integrada

- [x] 5.1 Test de integración del servicio contra PostgreSQL real que cambie en base de datos la última hora de reserva del martes a 22:00 y el máximo a 12, y compruebe que las siguientes consultas devuelven `OUTSIDE_HOURS` para martes 22:15 y `REQUIRES_HUMAN` para 13 personas (escenarios "sin desplegar" de la spec); verificar que pasa
- [x] 5.2 Ejecutar `./mvnw clean verify` y `openspec validate add-reservation-settings --strict`; verificar que ambos terminan sin errores

## Workflow follow-up

- Revisión de los artefactos por el equipo antes de `/openspec-apply-change`.
- Archivar el cambio con `/openspec-archive-change` cuando esté implementado y revisado.
