# whatsapp-bot — Guía para agentes

> Contexto común para cualquier agente de IA (Claude Code, Codex, Copilot, Cursor…).
> Última actualización: [2026-11-14]
> Configuración de specs y reglas por artefacto: `openspec/config.yaml`.

## Qué es este proyecto

Bot de WhatsApp para un restaurante que atiende automáticamente a los clientes que quieren
reservar mesa, a través de la WhatsApp Cloud API de Meta. Gestiona la conversación en español,
aplica el horario y el aforo por reserva, y deriva a una persona los casos que no puede resolver,
como los grupos que superan el máximo de personas configurado en base de datos.

## Reglas de oro

1. **Spec primero**: ninguna funcionalidad nueva sin un cambio en `openspec/changes/<nombre>/`
   con `proposal.md`, `specs/` y `tasks.md` revisados.
2. Implementa siguiendo `tasks.md` en orden y marca `[x]` cada tarea al terminarla.
3. **Tests primero**: escribe el test, comprueba que falla y luego implementa.
4. Si un requisito es ambiguo, **pregunta**. No inventes comportamiento.
5. Cambios mínimos y acotados a la tarea. Nada de refactors no pedidos.
6. `openspec/specs/` describe el comportamiento actual: no lo edites a mano,
   se actualiza al archivar un cambio.
7. [REGLA_DEL_EQUIPO]

## Stack

- Java 27
- Build: Maven (usar siempre el wrapper `./mvnw`)
- Framework: Spring Boot 4.1
- WhatsApp: WhatsApp Cloud API de Meta (Graph API)
- Persistencia: PostgreSQL + Spring Data JPA; esquema gestionado solo con Flyway (`ddl-auto: validate`)
- Tests: JUnit Jupiter 6 + Mockito + Testcontainers (PostgreSQL)
- Otros: Lombok

## Estructura

```text
src/
├── main/java/com/restaurante/whatsappbot/
│   ├── WhatsappBotApplication.java
│   ├── config/          # configuración y beans (propiedades whatsapp.api.*, clientes HTTP)
│   ├── whatsapp/        # integración con la WhatsApp Cloud API
│   │   ├── controller/  # webhook: verificación (GET) y recepción de mensajes (POST)
│   │   ├── dto/         # payloads entrantes/salientes de Meta
│   │   └── client/      # envío de mensajes a la Graph API
│   ├── reservation/     # dominio de reservas
│   │   ├── domain/      # entidades y reglas (horario, aforo, derivación a humano)
│   │   ├── service/     # casos de uso (crear, consultar, cancelar reserva)
│   │   └── repository/  # Spring Data JPA
│   └── conversation/    # estado y flujo de la conversación con el cliente
├── main/resources/
│   ├── application.yml
│   └── db/migration/    # scripts Flyway (V1__...sql)
└── test/java/com/restaurante/whatsappbot/
openspec/
├── config.yaml          # contexto y reglas para generar artefactos
├── specs/               # fuente de verdad del comportamiento actual
└── changes/             # cambios en curso (y archive/)
```

## Comandos

> Los tests arrancan PostgreSQL con Testcontainers: **Docker debe estar en marcha**
> antes de `./mvnw test` o `./mvnw clean verify`.

```bash
./mvnw clean verify          # compilar + tests + checks (usar antes de dar algo por terminado)
./mvnw test                  # solo tests
./mvnw test -Dtest=NombreTest   # un test concreto
./mvnw spring-boot:run       # arrancar en local   [ajustar si no es Spring Boot]
[COMANDO_LINT_O_FORMATO, p. ej. ./mvnw spotless:apply]
```

```bash
openspec list                # cambios activos
openspec show <cambio>       # ver un cambio
openspec validate <cambio> --strict
```

## Estilo de código

- [Convención: p. ej. Google Java Style / la del formateador del proyecto]
- Nombres de clases, métodos y variables en [inglés/español]; comentarios en [idioma].
- Inyección por constructor; nada de `@Autowired` en campos.
- No capturar excepciones para ignorarlas; registrar con contexto y relanzar o tratar.
- Logs con SLF4J, sin datos personales (números de teléfono, contenido de mensajes) en texto plano.
- Commits: [p. ej. Conventional Commits — `feat(webhook): ...`], referencian el cambio de OpenSpec.

## Seguridad y datos

- Tokens de WhatsApp y credenciales **solo** por variables de entorno o gestor de secretos.
  Nunca en el código, en `application.yml` versionado ni en los artefactos de OpenSpec.
- Validar la firma de los webhooks entrantes.
- [Requisitos de privacidad/RGPD del proyecto]

## Definición de terminado

- [ ] `./mvnw clean verify` pasa
- [ ] Tests nuevos cubren los escenarios de la spec
- [ ] Tareas marcadas en `tasks.md`
- [ ] Sin secretos ni datos personales en código o logs

## No hacer

- Editar las carpetas generadas `.agents/`, `.claude/skills/`, `.codex/` (las regenera `openspec update`).
- Añadir dependencias al `pom.xml` sin justificarlas en `design.md`.
- Desactivar o saltarse tests (`-DskipTests`) para que algo pase.
- [PROHIBICION_DEL_EQUIPO]

## Cambios recientes

<!-- Últimos 3 cambios archivados, el más reciente arriba -->
- [nombre-cambio]: [qué se añadió]
