# CLAUDE.md

@AGENTS.md

<!--
Lo común (stack, comandos, estilo, reglas) vive en AGENTS.md y se importa arriba.
Aquí solo va lo específico de Claude Code. No dupliques información.
-->

## Flujo con OpenSpec en Claude Code

| Momento | Comando |
|---|---|
| Pensar una idea / investigar el código | `/openspec-explore <idea>` |
| Crear el cambio con todos sus artefactos | `/openspec-propose <nombre-o-idea>` |
| Corregir proposal/specs/design/tasks | `/openspec-update-change <nombre> <qué cambiar>` |
| Implementar | `/openspec-apply-change <nombre>` |
| Fusionar specs sin archivar (opcional) | `/openspec-sync-specs <nombre>` |
| Cerrar el cambio | `/openspec-archive-change <nombre>` |

- Tras `/openspec-propose` o `/openspec-update-change`, **para y espera mi revisión** antes de implementar.
- Durante `/openspec-apply-change`, ejecuta `./mvnw test` al terminar cada grupo de tareas
  y `./mvnw clean verify` antes de dar el cambio por terminado.
- Si al implementar descubres que la spec está mal o incompleta, **no la ignores**:
  avísame y propón `/openspec-update-change`.

## Forma de trabajar

- Antes de editar, lee los archivos implicados; no supongas su contenido.
- Muéstrame un plan corto antes de cambios que toquen más de [3] archivos.
- Responde en español; código y nombres según el estilo de AGENTS.md.
- Si una tarea es ambigua, pregunta en lugar de elegir por mí.

## Permisos y límites

- Puedes ejecutar sin preguntar: `./mvnw`, `openspec list|show|validate|status`, `git status|diff|log`.
- Pregunta antes de: `git commit`, `git push`, borrar archivos, cambiar `pom.xml`.
- Nunca leas ni muestres `.env` ni archivos con credenciales.

## Al compactar el contexto

Conserva: nombre del cambio de OpenSpec activo, tareas pendientes de `tasks.md`,
archivos modificados y resultado del último `./mvnw test`.
