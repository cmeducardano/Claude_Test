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
        """Get default prompt for a role (including transitional roles)"""
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
            LLMRole.ESPLORATORE_ANALIZZANTE: """Tu sei un "Esploratore Analizzante" - fase di transizione (sessione 3).

OBIETTIVO: Continuare a esplorare MA iniziando a far notare pattern emersi.

PRINCIPI:
- Inizia con un breve recap delle sessioni precedenti
- Fai notare connessioni tra interessi emersi
- Chiedi conferma: "Ti ritrovi in questa osservazione?"
- NON suggerire ancora percorsi specifici

STILE: Riflessivo ma ancora curioso. Usa "Mi sembra di notare...", "Potrebbe essere che..."
""",
            LLMRole.ANALISTA: """Tu sei un "Analista Empatico" - approfondisci competenze e valori dello studente.

OBIETTIVO: Validare competenze e identificare valori profondi.

PRINCIPI:
- Ascolta i pattern emersi nelle sessioni precedenti
- Fai domande per validare competenze reali
- Esplora i "perché" dietro le scelte

STILE: Riflessivo ma sempre positivo, mai giudicante.
""",
            LLMRole.ANALISTA_ORIENTANTE: """Tu sei un "Analista Orientante" - fase di transizione (sessione 6).

OBIETTIVO: Consolidare il profilo e preparare per i percorsi concreti.

PRINCIPI:
- Inizia con una sintesi del profilo emerso
- Esplora i CRITERI di scelta (distanza, durata, pratico vs teorico)
- NON presentare ancora percorsi specifici
- Anticipa la prossima fase: "Dalla prossima sessione vedremo percorsi concreti"

STILE: Riflessivo e sintetico. Celebra il percorso fatto finora.
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
            LLMRole.MENTORE_ATTIVANTE: """Tu sei un "Mentore Attivante" - fase di transizione (sessione 10).

OBIETTIVO: Verificare decisione e preparare il piano d'azione.

PRINCIPI:
- Verifica: ha scelto o è ancora indeciso?
- Se ha scelto: celebra e prepara i primi passi concreti
- Se indeciso: proponi esperimenti (open day, parlare con qualcuno)
- Anticipa: "Dalla prossima sessione divento il tuo Coach Operativo"

STILE: Energico ma rispettoso dei tempi. Bilancia entusiasmo e pazienza.
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

    def determine_role(
        self,
        session_count: int,
        student_status: str,
        force_filosofo: bool = False
    ) -> LLMRole:
        """
        Determine appropriate LLM role based on progress with micro-transitions.

        Scaffolding pedagogico graduale:
        - Sessioni 1-2: ESPLORATORE (scoperta pura)
        - Sessione 3: ESPLORATORE_ANALIZZANTE (transizione)
        - Sessioni 4-5: ANALISTA (analisi profonda)
        - Sessione 6: ANALISTA_ORIENTANTE (transizione)
        - Sessioni 7-9: MENTORE (presenta opzioni)
        - Sessione 10: MENTORE_ATTIVANTE (transizione)
        - Sessioni 11+: COACH (esecuzione)
        - Sempre: FILOSOFO (su richiesta o crisi)
        """
        # Il Filosofo può essere attivato in qualsiasi momento
        if force_filosofo:
            return LLMRole.FILOSOFO

        # Scaffolding graduale con micro-transizioni
        if session_count <= 2:
            # Sessioni 1-2: Esplorazione pura
            return LLMRole.ESPLORATORE
        elif session_count == 3:
            # Sessione 3: Transizione - inizia a vedere pattern
            return LLMRole.ESPLORATORE_ANALIZZANTE
        elif session_count <= 5:
            # Sessioni 4-5: Analisi profonda
            return LLMRole.ANALISTA
        elif session_count == 6:
            # Sessione 6: Transizione - prepara per i percorsi
            return LLMRole.ANALISTA_ORIENTANTE
        elif session_count <= 9:
            # Sessioni 7-9: Mentoring con opzioni concrete
            return LLMRole.MENTORE
        elif session_count == 10:
            # Sessione 10: Transizione - prepara per l'azione
            return LLMRole.MENTORE_ATTIVANTE
        else:
            # Sessioni 11+: Coach operativo
            return LLMRole.COACH

    def get_role_progress_info(self, role: LLMRole) -> dict:
        """
        Restituisce informazioni sul progresso per la UI.
        Utile per mostrare allo studente dove si trova nel percorso.
        """
        role_info = {
            LLMRole.ESPLORATORE: {
                "fase": "Esplorazione",
                "descrizione": "Scopriamo i tuoi interessi",
                "progresso": 15,
                "icona": "🔍",
                "prossimo": "Presto inizieremo a vedere i pattern"
            },
            LLMRole.ESPLORATORE_ANALIZZANTE: {
                "fase": "Esplorazione → Analisi",
                "descrizione": "Iniziamo a vedere i pattern",
                "progresso": 25,
                "icona": "🔍→📊",
                "prossimo": "Nella prossima sessione approfondiremo le tue competenze"
            },
            LLMRole.ANALISTA: {
                "fase": "Analisi",
                "descrizione": "Approfondiamo competenze e valori",
                "progresso": 40,
                "icona": "📊",
                "prossimo": "Presto esploreremo i percorsi possibili"
            },
            LLMRole.ANALISTA_ORIENTANTE: {
                "fase": "Analisi → Orientamento",
                "descrizione": "Prepariamo i criteri di scelta",
                "progresso": 50,
                "icona": "📊→🎯",
                "prossimo": "Dalla prossima sessione vedremo percorsi concreti"
            },
            LLMRole.MENTORE: {
                "fase": "Orientamento",
                "descrizione": "Esploriamo i percorsi possibili",
                "progresso": 65,
                "icona": "🎯",
                "prossimo": "Presto passeremo all'azione"
            },
            LLMRole.MENTORE_ATTIVANTE: {
                "fase": "Orientamento → Azione",
                "descrizione": "Prepariamo il piano d'azione",
                "progresso": 80,
                "icona": "🎯→🚀",
                "prossimo": "Dalla prossima sessione si parte!"
            },
            LLMRole.COACH: {
                "fase": "Azione",
                "descrizione": "Realizziamo il tuo piano",
                "progresso": 90,
                "icona": "🚀",
                "prossimo": "Continuiamo verso il traguardo"
            },
            LLMRole.FILOSOFO: {
                "fase": "Riflessione",
                "descrizione": "Prendiamoci un momento per riflettere",
                "progresso": None,  # Fuori dal flusso lineare
                "icona": "🤔",
                "prossimo": "Riprenderemo il percorso quando ti sentirai pronto"
            }
        }
        return role_info.get(role, {
            "fase": "Sconosciuta",
            "descrizione": "",
            "progresso": 0,
            "icona": "❓",
            "prossimo": ""
        })
