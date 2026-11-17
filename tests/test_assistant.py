import pytest
from langchain_core.language_models.fake_chat_models import FakeListChatModel
from pydantic import ValidationError

from openrouter_assistant.chat import build_chain
from openrouter_assistant.cli import main
from openrouter_assistant.config import Settings


@pytest.fixture(autouse=True)
def isolated_settings(monkeypatch, tmp_path):
    monkeypatch.chdir(tmp_path)
    monkeypatch.setenv("OPENROUTER_API_KEY", "test-secret-only")
    monkeypatch.setenv("OPENROUTER_MODEL", "test/model")
    for name in (
        "AI_SYSTEM_PROMPT",
        "AI_TEMPERATURE",
        "AI_MAX_TOKENS",
        "AI_TIMEOUT_MS",
        "AI_MAX_RETRIES",
    ):
        monkeypatch.delenv(name, raising=False)


def test_environment_overrides_dotenv(tmp_path):
    (tmp_path / ".env").write_text("OPENROUTER_MODEL=file/model\n", encoding="utf-8")
    assert Settings().model == "test/model"


@pytest.mark.parametrize(
    ("name", "value"),
    [
        ("OPENROUTER_API_KEY", " "),
        ("OPENROUTER_MODEL", ""),
        ("AI_TEMPERATURE", "3"),
        ("AI_MAX_TOKENS", "0"),
        ("AI_TIMEOUT_MS", "0"),
    ],
)
def test_invalid_configuration_is_rejected(monkeypatch, name, value):
    monkeypatch.setenv(name, value)
    with pytest.raises(ValidationError):
        Settings()


def test_secret_is_hidden():
    settings = Settings()
    assert "test-secret-only" not in repr(settings)
    assert "test-secret-only" not in settings.model_dump_json()


def test_chain_preserves_braces_and_message_roles(monkeypatch):
    monkeypatch.setenv("AI_SYSTEM_PROMPT", 'Responde con JSON: {"answer": "..."}')
    model = FakeListChatModel(responses=["Respuesta de prueba"])
    chain = build_chain(Settings(), model=model)
    prompts = []
    from langchain_core.callbacks import BaseCallbackHandler

    class Capture(BaseCallbackHandler):
        def on_chat_model_start(self, serialized, messages, **kwargs):
            prompts.extend(messages[0])

    assert chain.invoke({"question": "Hola {usuario}"}, {"callbacks": [Capture()]}) == (
        "Respuesta de prueba"
    )
    assert [message.type for message in prompts] == ["system", "human"]
    assert prompts[0].content == 'Responde con JSON: {"answer": "..."}'
    assert prompts[1].content == "Hola {usuario}"


def test_missing_key_reports_configuration_error(monkeypatch, capsys):
    monkeypatch.delenv("OPENROUTER_API_KEY")
    assert main(["Hola"]) == 2
    assert "OPENROUTER_API_KEY" in capsys.readouterr().err


def test_provider_error_does_not_expose_secrets(monkeypatch, capsys):
    def fail(settings):
        raise RuntimeError("request contained test-secret-only")

    monkeypatch.setattr("openrouter_assistant.cli.build_chain", fail)
    assert main(["Hola"]) == 1
    output = capsys.readouterr()
    assert "test-secret-only" not in output.err
    assert not output.out


def test_blank_question_is_rejected():
    with pytest.raises(SystemExit) as exc:
        main(["   "])
    assert exc.value.code == 2
