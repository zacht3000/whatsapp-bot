"""Construcción de la cadena y acceso al proveedor."""

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import Runnable
from langchain_openrouter import ChatOpenRouter

from openrouter_assistant.config import Settings


def build_chain(settings: Settings, *, model: BaseChatModel | None = None) -> Runnable:
    """Permite inyectar un modelo local para probar sin red ni credenciales reales."""
    if model is None:
        model = ChatOpenRouter(
            api_key=settings.api_key.get_secret_value(),
            model=settings.model,
            temperature=settings.temperature,
            max_tokens=settings.max_tokens,
            timeout=settings.timeout_ms,
            max_retries=settings.max_retries,
        )
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", "{system_prompt}"),
            ("human", "{question}"),
        ]
    ).partial(system_prompt=settings.system_prompt)
    return prompt | model | StrOutputParser()
