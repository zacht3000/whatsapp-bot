"""Entrada de consola. Los errores nunca imprimen respuestas crudas del proveedor."""

import argparse
import sys

from pydantic import ValidationError

from openrouter_assistant.chat import build_chain
from openrouter_assistant.config import Settings


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Consulta un modelo de IA mediante OpenRouter.")
    parser.add_argument("question", help="Pregunta entre comillas")
    args = parser.parse_args(argv)
    question = args.question.strip()
    if not question:
        parser.error("la pregunta no puede estar vacía")

    try:
        settings = Settings()
    except ValidationError as exc:
        fields = sorted({str(error["loc"][0]) for error in exc.errors()})
        print(
            f"Configuración inválida: {', '.join(fields)}. Revisa las variables de entorno o .env.",
            file=sys.stderr,
        )
        return 2

    try:
        print("Consultando OpenRouter…", file=sys.stderr, flush=True)
        response = build_chain(settings).invoke({"question": question})
    except KeyboardInterrupt:
        print("Consulta cancelada.", file=sys.stderr)
        return 130
    except Exception:
        # El SDK puede incluir datos de la petición en la excepción. No se muestran.
        print(
            "No se pudo consultar OpenRouter. Revisa conexión, credenciales, saldo y modelo.",
            file=sys.stderr,
        )
        return 1

    if not response.strip():
        print(
            "OpenRouter no devolvió texto. Usa un modelo de chat en OPENROUTER_MODEL "
            "y revisa AI_MAX_TOKENS si el modelo consume tokens de razonamiento.",
            file=sys.stderr,
        )
        return 1

    print(response)
    return 0
