"""
Application configuration
"""

from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional


class Settings(BaseSettings):
    """Application settings"""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

    # LLM
    openai_api_key: Optional[str] = None
    anthropic_api_key: Optional[str] = None
    llm_provider: str = "openai"  # openai, anthropic, ollama

    # Database
    database_path: str = "data/orientation.db"

    # Cache TTL (seconds)
    cache_ttl_scadenze: int = 6 * 3600  # 6 ore
    cache_ttl_programmi: int = 30 * 86400  # 30 giorni
    cache_ttl_mercato: int = 7 * 86400  # 7 giorni

    # Session
    session_duration_min: int = 15
    session_duration_max: int = 20
    max_searches_per_session: int = 10

    # Privacy
    enable_privacy_filters: bool = True


def load_config() -> Settings:
    """Load application configuration"""
    return Settings()
