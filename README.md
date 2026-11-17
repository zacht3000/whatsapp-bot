# Asistente con Python, LangChain y OpenRouter

Proyecto mínimo de consola para enviar una pregunta a un modelo de OpenRouter.
Usa `langchain-openai` contra la API compatible con OpenAI de OpenRouter y una cadena:
`ChatPromptTemplate → ChatOpenAI → StrOutputParser`.
Cada ejecución realiza una consulta independiente, sin historial persistente.

## Preparación

Requiere Python 3.11 o superior y una cuenta de OpenRouter con acceso al modelo elegido.
Validado con CPython 3.11, 3.12, 3.13, 3.14 y 3.15. En Windows, usa Developer PowerShell
for VS 2022 para instalar si alguna dependencia debe compilarse desde su código fuente.
En PowerShell, desde la raíz del proyecto:

```powershell
py -3 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"
Copy-Item .env.example .env
```

Edita `.env`: establece `OPENROUTER_API_KEY` con tu clave y `OPENROUTER_MODEL`
con el identificador `proveedor/modelo` del catálogo. Ambos son obligatorios;
no hay un modelo ni credenciales fijados en el código.

```powershell
.\.venv\Scripts\ai-chat.exe "Explica qué es LangChain en tres frases"
# Alternativa equivalente:
.\.venv\Scripts\python.exe -m openrouter_assistant "¿Cómo funciona OpenRouter?"
```

En Linux/macOS usa `python3 -m venv .venv`, `.venv/bin/python -m pip install -e ".[dev]"`,
`cp .env.example .env` y `.venv/bin/ai-chat "Hola"`.

Para instalar exactamente las versiones verificadas, el proyecto incluye `uv.lock`.
Si tienes [uv](https://docs.astral.sh/uv/) instalado, puedes sustituir la creación
del entorno y la instalación con pip por `uv sync --locked --extra dev`, y ejecutar
`uv run --locked ai-chat "Hola"` después de configurar `.env`.

## Configuración

Se lee `.env` del directorio desde el que ejecutas el comando. Las variables del
entorno tienen prioridad. En despliegues, configura estas variables desde el gestor
de secretos del entorno; no necesitas crear `.env`.

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `OPENROUTER_API_KEY` | Obligatoria | Clave secreta de OpenRouter |
| `OPENROUTER_MODEL` | Obligatoria | Identificador del modelo |
| `AI_SYSTEM_PROMPT` | Asistente en español | Instrucciones del sistema |
| `AI_TEMPERATURE` | `0.7` | Creatividad, entre 0 y 2 |
| `AI_MAX_TOKENS` | `512` | Máximo de tokens de salida |
| `AI_TIMEOUT_MS` | `30000` | Timeout por intento, en milisegundos |
| `AI_MAX_RETRIES` | `2` | Reintentos del cliente, entre 0 y 5 |

Los valores inválidos se rechazan antes de consultar la API. Algunos modelos tienen
restricciones propias sobre temperatura y tokens; ajusta la configuración según su ficha.
Los reintentos pueden alargar la duración total de una consulta.

`.env` está excluido de Git. La clave se representa como `SecretStr` y los errores
de consola no imprimen excepciones del proveedor ni valores de configuración.
Las preguntas se envían a OpenRouter y al proveedor que sirve el modelo; las llamadas
pueden generar costes según la cuenta y el modelo elegido.

## Estructura y validación

- `src/openrouter_assistant/config.py`: configuración y validación con Pydantic Settings.
- `src/openrouter_assistant/chat.py`: construcción de la cadena e inyección del modelo.
- `src/openrouter_assistant/cli.py`: entrada de consola y manejo de errores.
- `tests/`: pruebas locales sin llamadas a OpenRouter.

```powershell
.\.venv\Scripts\python.exe -m pytest
.\.venv\Scripts\python.exe -m ruff check .
.\.venv\Scripts\python.exe -m ruff format --check .
```

Códigos de salida: `0` éxito, `1` fallo de consulta, `2` configuración o argumentos
inválidos y `130` consulta interrumpida.

Referencias: [integración de LangChain](https://docs.langchain.com/oss/python/integrations/chat/openrouter),
[claves de OpenRouter](https://openrouter.ai/settings/keys) y
[catálogo de modelos](https://openrouter.ai/models).
