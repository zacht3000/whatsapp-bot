"""Construcción de la cadena y acceso al proveedor."""

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import Runnable
from langchain_openai import ChatOpenAI

from openrouter_assistant.config import Settings


def build_chain(settings: Settings, *, model: BaseChatModel | None = None) -> Runnable:
    """Permite inyectar un modelo local para probar sin red ni credenciales reales."""
    if model is None:
        model = ChatOpenAI(
            api_key=settings.api_key.get_secret_value(),
            base_url="https://openrouter.ai/api/v1",
            model=settings.model,
            temperature=settings.temperature,
            max_tokens=settings.max_tokens,
            timeout=settings.timeout_ms / 1000,
            max_retries=settings.max_retries,
        )
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", "{system_prompt}"),
            ("human", "{question}"),
        ]
    ).partial(system_prompt=settings.system_prompt)
    return prompt | model | StrOutputParser()
