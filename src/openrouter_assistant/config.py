"""Configuración validada, sin cargar secretos al importar el módulo."""

from pydantic import Field, SecretStr, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        hide_input_in_errors=True,
        str_strip_whitespace=True,
        frozen=True,
    )

    api_key: SecretStr = Field(validation_alias="OPENROUTER_API_KEY")
    model: str = Field(min_length=1, validation_alias="OPENROUTER_MODEL")
    system_prompt: str = Field(
        default="Eres un asistente útil. Responde en español de forma clara y concisa.",
        min_length=1,
        validation_alias="AI_SYSTEM_PROMPT",
    )
    temperature: float = Field(default=0.7, ge=0, le=2, validation_alias="AI_TEMPERATURE")
    max_tokens: int = Field(default=512, gt=0, validation_alias="AI_MAX_TOKENS")
    timeout_ms: int = Field(default=30000, gt=0, validation_alias="AI_TIMEOUT_MS")
    max_retries: int = Field(default=2, ge=0, le=5, validation_alias="AI_MAX_RETRIES")

    @field_validator("api_key")
    @classmethod
    def validate_api_key(cls, value: SecretStr) -> SecretStr:
        key = value.get_secret_value().strip()
        if not key:
            raise ValueError("OPENROUTER_API_KEY no puede estar vacía")
        return SecretStr(key)
