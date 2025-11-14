"""
LLM Service - Abstraction layer for different LLM providers
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Optional
from pathlib import Path
import asyncio

from openai import AsyncOpenAI
from anthropic import AsyncAnthropic

from core.models import Message, LLMRole, MessageRole


class LLMProvider(ABC):
    """Abstract base class for LLM providers"""

    @abstractmethod
    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        """Get chat completion"""
        pass


class OpenAIProvider(LLMProvider):
    """OpenAI GPT provider"""

    def __init__(self, api_key: str):
        self.client = AsyncOpenAI(api_key=api_key)
        self.model = "gpt-4-turbo-preview"

    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        """Get chat completion from OpenAI"""
        try:
            response = await self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                tools=tools,
                temperature=0.7,
                max_tokens=2000
            )
            return response.choices[0].message.content
        except Exception as e:
            return f"Errore nella comunicazione con il modello: {str(e)}"


class AnthropicProvider(LLMProvider):
    """Anthropic Claude provider"""

    def __init__(self, api_key: str):
        self.client = AsyncAnthropic(api_key=api_key)
        self.model = "claude-3-opus-20240229"

    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        """Get chat completion from Anthropic"""
        try:
            # Separate system message from other messages
            system_msg = None
            user_messages = []

            for msg in messages:
                if msg["role"] == "system":
                    system_msg = msg["content"]
                else:
                    user_messages.append(msg)

            response = await self.client.messages.create(
                model=self.model,
                messages=user_messages,
                system=system_msg,
                tools=tools or [],
                max_tokens=4096
            )
            return response.content[0].text
        except Exception as e:
            return f"Errore nella comunicazione con il modello: {str(e)}"


class LLMService:
    """Main LLM service"""

    def __init__(self, provider: LLMProvider):
        self.provider = provider
        self.prompts = self._load_prompts()

    def _load_prompts(self) -> Dict[LLMRole, str]:
        """Load system prompts for each role"""
        prompts = {}
        prompts_dir = Path("data/prompts")

        # Create prompts directory if it doesn't exist
        prompts_dir.mkdir(parents=True, exist_ok=True)

        for role in LLMRole:
            prompt_file = prompts_dir / f"{role.value}.txt"
            if prompt_file.exists():
                prompts[role] = prompt_file.read_text(encoding="utf-8")
            else:
                # Default prompt if file doesn't exist
                prompts[role] = self._get_default_prompt(role)

        return prompts

    def _get_default_prompt(self, role: LLMRole) -> str:
        """Get default prompt for a role"""
        defaults = {
            LLMRole.ESPLORATORE: """Tu sei un "Esploratore Curioso" - un assistente all'orientamento per studenti ITIS.

OBIETTIVO: Scoprire interessi autentici dello studente attraverso domande aperte, senza giudizio.

PRINCIPI:
- Mai suggerire percorsi in questa fase
- Focus su esperienze concrete, non su aspirazioni vaghe
- Validare ogni risposta senza giudicare
- Cercare pattern ma non dire "sei fatto per X"

STILE:
- Informale, curioso, amichevole
- Domande specifiche su progetti/momenti concreti
- Approfondire con "come ti sei sentito?" "cosa ti è piaciuto?"
""",
            LLMRole.ANALISTA: """Tu sei un "Analista Empatico" - approfondisci competenze e valori dello studente.

OBIETTIVO: Validare competenze e identificare valori profondi.

PRINCIPI:
- Ascolta i pattern emersi nelle sessioni precedenti
- Fai domande per validare competenze reali
- Esplora i "perché" dietro le scelte

STILE: Riflessivo ma sempre positivo, mai giudicante.
""",
            LLMRole.MENTORE: """Tu sei un "Mentore Pragmatico" - presenti percorsi concreti basati sul profilo studente.

OBIETTIVO: Matching percorsi e gestione obiezioni.

PRINCIPI:
- Presentare 3-5 percorsi concreti
- Collegamenti chiari al profilo studente
- Gestire obiezioni con empatia
- Proporre "test di realtà" verificabili

STILE: Pratico, onesto, supportivo.
""",
            LLMRole.COACH: """Tu sei un "Coach Operativo" - supporti la preparazione concreta.

OBIETTIVO: Aiutare l'esecuzione del piano scelto.

PRINCIPI:
- Focus su azioni concrete
- Problem solving ostacoli
- Celebrazione progressi
- Alternative se bloccato

STILE: Energico, motivante, operativo.
""",
            LLMRole.FILOSOFO: """Tu sei un "Filosofo Riflessivo" - gestisci crisi decisionali.

OBIETTIVO: Dare prospettiva ampia, normalizzare dubbi.

PRINCIPI:
- Normalizzare confusione e dubbi
- Prospettiva temporale ampia
- "Non esiste scelta irreversibile"
- Mai forzare decisioni

STILE: Calmo, saggio, rassicurante.
"""
        }
        return defaults.get(role, "Tu sei un assistente per l'orientamento post-diploma.")

    async def get_response(
        self,
        messages: List[Message],
        role: LLMRole,
        student_context: Optional[Dict] = None
    ) -> str:
        """Get response from LLM with specific role"""

        # Build system prompt
        system_prompt = self.prompts.get(role, "")

        # Add student context if available
        if student_context:
            system_prompt += f"\n\nCONTESTO STUDENTE:\n{self._format_context(student_context)}"

        # Convert messages to provider format
        formatted_messages = [
            {"role": "system", "content": system_prompt}
        ]

        # Add conversation history
        for msg in messages:
            if msg.role != MessageRole.SYSTEM:  # Skip system messages
                formatted_messages.append({
                    "role": msg.role.value,
                    "content": msg.content
                })

        # Get response from provider
        response = await self.provider.chat(formatted_messages)
        return response

    def _format_context(self, context: Dict) -> str:
        """Format student context in readable way"""
        parts = []

        if context.get("interests"):
            interests_str = ", ".join(context["interests"])
            parts.append(f"Interessi emersi: {interests_str}")

        if context.get("session_count"):
            parts.append(f"Sessioni completate: {context['session_count']}")

        if context.get("pending_todos"):
            parts.append(f"Todo pendenti: {len(context['pending_todos'])}")

        if context.get("status"):
            parts.append(f"Fase attuale: {context['status']}")

        return "\n".join(parts)

    def determine_role(self, session_count: int, student_status: str) -> LLMRole:
        """Determine appropriate LLM role based on progress"""
        if session_count < 4:
            return LLMRole.ESPLORATORE
        elif session_count < 7:
            return LLMRole.ANALISTA
        elif session_count < 11:
            return LLMRole.MENTORE
        else:
            return LLMRole.COACH
