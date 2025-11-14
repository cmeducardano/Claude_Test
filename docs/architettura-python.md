# Architettura Python - ITIS Orientation Coach (Concept)

## Executive Summary

Applicazione Python autocontenuta per prototipazione rapida del concept. Focus su velocità di sviluppo, semplicità e validazione dell'idea prima di investire in architettura complessa.

---

## Stack Tecnologico Python

### Opzione A: Streamlit (Raccomandato per Concept)

**Vantaggi:**
- Sviluppo rapidissimo (giorni non settimane)
- UI già bella out-of-the-box
- Perfetto per chat interface
- Session state integrato
- Hot reload durante sviluppo
- Deploy locale semplicissimo

**Stack completo:**
```
Core:
- Python 3.11+
- Streamlit (UI framework)
- SQLite3 (database, built-in)
- python-dotenv (config)

LLM:
- openai (OpenAI API)
- anthropic (Claude API)
- ollama (locale opzionale)

Utilities:
- pydantic (validazione dati)
- python-dateutil (date handling)
- rich (logging colorato)
- pytest (testing)
```

### Opzione B: Gradio (Alternativa)

**Vantaggi:**
- Simile a Streamlit
- Ottimo per chat interfaces
- Condivisione demo facile

**Quando preferirlo:**
- Se serve interfaccia più customizzabile
- Se si vuole condividere demo online facilmente

### Opzione C: Flask + HTML (Per più controllo)

**Vantaggi:**
- Massimo controllo UI
- REST API riutilizzabile
- Più flessibile

**Svantaggi:**
- Più lento da sviluppare
- Serve scrivere frontend

**Raccomandazione per concept: Streamlit** (velocità massima)

---

## Struttura Progetto

```
itis-orientation-coach/
├── README.md
├── requirements.txt
├── .env.example
├── .gitignore
│
├── app.py                      # Entry point Streamlit
├── config.py                   # Configurazione applicazione
│
├── core/
│   ├── __init__.py
│   ├── database.py            # SQLite setup e queries
│   ├── models.py              # Pydantic models
│   └── session_state.py       # Gestione stato applicazione
│
├── services/
│   ├── __init__.py
│   ├── llm_service.py         # LLM providers abstraction
│   ├── session_manager.py     # Gestione sessioni conversazionali
│   ├── todo_manager.py        # Gestione todo-list
│   ├── search_service.py      # Ricerche web/API
│   ├── document_service.py    # Documento evolutivo studente
│   └── privacy_filter.py      # Privacy filters
│
├── ui/
│   ├── __init__.py
│   ├── components.py          # Componenti UI riusabili
│   ├── chat_view.py           # Vista conversazione
│   ├── todo_view.py           # Vista todo-list
│   ├── profile_view.py        # Vista profilo studente
│   └── settings_view.py       # Impostazioni
│
├── data/
│   ├── prompts/               # System prompts per ruoli LLM
│   │   ├── esploratore.txt
│   │   ├── analista.txt
│   │   ├── mentore.txt
│   │   ├── coach.txt
│   │   └── filosofo.txt
│   ├── paths/                 # Database percorsi
│   │   ├── universita.json
│   │   ├── its.json
│   │   └── altro.json
│   └── orientation.db         # SQLite database (generato)
│
├── tests/
│   ├── __init__.py
│   ├── test_llm_service.py
│   ├── test_privacy_filter.py
│   └── test_session_manager.py
│
└── docs/
    ├── specifiche-funzionali.md
    ├── architettura-tecnica.md
    └── architettura-python.md
```

---

## Database Schema (SQLite)

```python
# core/database.py

import sqlite3
from pathlib import Path
from typing import Optional
import json
from datetime import datetime

class Database:
    def __init__(self, db_path: str = "data/orientation.db"):
        self.db_path = Path(db_path)
        self.db_path.parent.mkdir(exist_ok=True)
        self.conn = sqlite3.connect(str(self.db_path), check_same_thread=False)
        self.conn.row_factory = sqlite3.Row
        self._init_schema()

    def _init_schema(self):
        """Crea schema database se non esiste"""
        cursor = self.conn.cursor()

        # Studenti
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id TEXT PRIMARY KEY,
                pseudonym TEXT UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_session_at TIMESTAMP,
                status TEXT CHECK(status IN ('exploring', 'deciding', 'preparing', 'completed')),
                settings_json TEXT
            )
        """)

        # Sessioni
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY,
                student_id TEXT NOT NULL,
                role TEXT NOT NULL,
                started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ended_at TIMESTAMP,
                duration_seconds INTEGER,
                message_count INTEGER DEFAULT 0,
                searches_count INTEGER DEFAULT 0,
                insights_json TEXT,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            )
        """)

        # Messaggi
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY,
                session_id TEXT NOT NULL,
                role TEXT CHECK(role IN ('user', 'assistant', 'system')),
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                metadata_json TEXT,
                FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
            )
        """)

        # Todo items
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS todos (
                id TEXT PRIMARY KEY,
                student_id TEXT NOT NULL,
                session_id TEXT,
                categoria TEXT CHECK(categoria IN ('esplorazione', 'preparazione', 'riflessione', 'admin')),
                priorita TEXT CHECK(priorita IN ('alta', 'media', 'bassa', 'nice-to-have')),
                status TEXT CHECK(status IN ('proposto', 'accettato', 'in_progress', 'completato', 'scartato')),
                deadline_type TEXT CHECK(deadline_type IN ('flessibile', 'rigida')),
                deadline_date DATE,
                content TEXT NOT NULL,
                motivazione TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                completed_at TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            )
        """)

        # Profilo studente
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS student_profile (
                student_id TEXT PRIMARY KEY,
                interests_json TEXT,
                skills_json TEXT,
                values_json TEXT,
                constraints_json TEXT,
                learning_style TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            )
        """)

        # Percorsi esplorati
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS paths (
                id TEXT PRIMARY KEY,
                student_id TEXT NOT NULL,
                type TEXT,
                name TEXT NOT NULL,
                description TEXT,
                match_percentage INTEGER,
                pros_json TEXT,
                cons_json TEXT,
                status TEXT CHECK(status IN ('explored', 'shortlisted', 'chosen', 'discarded')),
                explored_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            )
        """)

        # Cache ricerche
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS search_cache (
                key TEXT PRIMARY KEY,
                type TEXT,
                query TEXT NOT NULL,
                result_json TEXT NOT NULL,
                cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL,
                hit_count INTEGER DEFAULT 0
            )
        """)

        # Indici
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sessions_student ON sessions(student_id, started_at DESC)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_messages_session ON messages(session_id, created_at)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_todos_student_status ON todos(student_id, status)")

        self.conn.commit()
```

---

## Core Models (Pydantic)

```python
# core/models.py

from pydantic import BaseModel, Field
from typing import Optional, List, Literal
from datetime import datetime
from enum import Enum

class StudentStatus(str, Enum):
    EXPLORING = "exploring"
    DECIDING = "deciding"
    PREPARING = "preparing"
    COMPLETED = "completed"

class LLMRole(str, Enum):
    ESPLORATORE = "esploratore_curioso"
    ANALISTA = "analista_empatico"
    MENTORE = "mentore_pragmatico"
    COACH = "coach_operativo"
    FILOSOFO = "filosofo_riflessivo"

class MessageRole(str, Enum):
    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"

class Student(BaseModel):
    id: str
    pseudonym: str
    created_at: datetime
    last_session_at: Optional[datetime] = None
    status: StudentStatus = StudentStatus.EXPLORING
    settings: dict = {}

class Message(BaseModel):
    id: str
    session_id: str
    role: MessageRole
    content: str
    created_at: datetime
    metadata: dict = {}

class Session(BaseModel):
    id: str
    student_id: str
    role: LLMRole
    started_at: datetime
    ended_at: Optional[datetime] = None
    duration_seconds: Optional[int] = None
    message_count: int = 0
    searches_count: int = 0
    insights: List[str] = []

class TodoItem(BaseModel):
    id: str
    student_id: str
    session_id: Optional[str] = None
    categoria: Literal["esplorazione", "preparazione", "riflessione", "admin"]
    priorita: Literal["alta", "media", "bassa", "nice-to-have"]
    status: Literal["proposto", "accettato", "in_progress", "completato", "scartato"]
    deadline_type: Literal["flessibile", "rigida"]
    deadline_date: Optional[datetime] = None
    content: str
    motivazione: Optional[str] = None
    created_at: datetime
    completed_at: Optional[datetime] = None

class Path(BaseModel):
    id: str
    student_id: str
    type: str
    name: str
    description: str
    match_percentage: int
    pros: List[str] = []
    cons: List[str] = []
    status: Literal["explored", "shortlisted", "chosen", "discarded"]
    explored_at: datetime
```

---

## LLM Service

```python
# services/llm_service.py

from abc import ABC, abstractmethod
from typing import List, Dict, AsyncIterator, Optional
from core.models import Message, LLMRole
import openai
import anthropic
from pathlib import Path

class LLMProvider(ABC):
    @abstractmethod
    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        pass

    @abstractmethod
    async def stream_chat(self, messages: List[Dict]) -> AsyncIterator[str]:
        pass

class OpenAIProvider(LLMProvider):
    def __init__(self, api_key: str):
        self.client = openai.AsyncOpenAI(api_key=api_key)
        self.model = "gpt-4-turbo-preview"

    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        response = await self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            tools=tools,
            temperature=0.7
        )
        return response.choices[0].message.content

    async def stream_chat(self, messages: List[Dict]) -> AsyncIterator[str]:
        stream = await self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            stream=True
        )
        async for chunk in stream:
            if chunk.choices[0].delta.content:
                yield chunk.choices[0].delta.content

class AnthropicProvider(LLMProvider):
    def __init__(self, api_key: str):
        self.client = anthropic.AsyncAnthropic(api_key=api_key)
        self.model = "claude-3-opus-20240229"

    async def chat(self, messages: List[Dict], tools: Optional[List] = None) -> str:
        response = await self.client.messages.create(
            model=self.model,
            messages=messages,
            tools=tools or [],
            max_tokens=4096
        )
        return response.content[0].text

class LLMService:
    def __init__(self, provider: LLMProvider):
        self.provider = provider
        self.prompts = self._load_prompts()

    def _load_prompts(self) -> Dict[LLMRole, str]:
        """Carica system prompts per ogni ruolo"""
        prompts = {}
        prompts_dir = Path("data/prompts")

        for role in LLMRole:
            prompt_file = prompts_dir / f"{role.value}.txt"
            if prompt_file.exists():
                prompts[role] = prompt_file.read_text()

        return prompts

    async def get_response(
        self,
        messages: List[Message],
        role: LLMRole,
        student_context: Optional[Dict] = None
    ) -> str:
        """Ottiene risposta da LLM con ruolo specifico"""

        # Costruisci system prompt
        system_prompt = self.prompts.get(role, "")

        # Aggiungi contesto studente se disponibile
        if student_context:
            system_prompt += f"\n\nCONTESTO STUDENTE:\n{self._format_context(student_context)}"

        # Converti messaggi a formato provider
        formatted_messages = [
            {"role": "system", "content": system_prompt}
        ] + [
            {"role": msg.role.value, "content": msg.content}
            for msg in messages
        ]

        return await self.provider.chat(formatted_messages)

    def _format_context(self, context: Dict) -> str:
        """Formatta contesto studente in modo leggibile"""
        parts = []

        if context.get("interests"):
            parts.append(f"Interessi: {', '.join(context['interests'])}")

        if context.get("session_count"):
            parts.append(f"Sessioni completate: {context['session_count']}")

        if context.get("pending_todos"):
            parts.append(f"Todo pendenti: {len(context['pending_todos'])}")

        return "\n".join(parts)
```

---

## Streamlit UI (App Principale)

```python
# app.py

import streamlit as st
from pathlib import Path
import asyncio
from core.database import Database
from services.llm_service import LLMService, OpenAIProvider
from services.session_manager import SessionManager
from services.todo_manager import TodoManager
from ui.chat_view import render_chat_view
from ui.todo_view import render_todo_view
from ui.profile_view import render_profile_view
from config import load_config

# Configurazione pagina
st.set_page_config(
    page_title="ITIS Orientation Coach",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Inizializzazione
@st.cache_resource
def init_services():
    """Inizializza database e servizi"""
    config = load_config()

    # Database
    db = Database()

    # LLM Service
    provider = OpenAIProvider(api_key=config.openai_api_key)
    llm_service = LLMService(provider)

    # Managers
    session_manager = SessionManager(db, llm_service)
    todo_manager = TodoManager(db)

    return db, session_manager, todo_manager

# Inizializza session state
if "student_id" not in st.session_state:
    st.session_state.student_id = None
if "current_session" not in st.session_state:
    st.session_state.current_session = None

# Main app
def main():
    db, session_manager, todo_manager = init_services()

    # Sidebar
    with st.sidebar:
        st.title("🎓 ITIS Orientation")

        # Student info
        if st.session_state.student_id:
            student = db.get_student(st.session_state.student_id)
            st.success(f"Ciao {student.pseudonym}!")

            # Navigation
            page = st.radio(
                "Navigazione",
                ["💬 Chat", "✅ Todo List", "👤 Profilo", "⚙️ Impostazioni"]
            )

            # Stats rapide
            st.divider()
            st.metric("Sessioni", session_manager.count_sessions(student.id))
            st.metric("Todo Completate", todo_manager.count_completed(student.id))

        else:
            # Onboarding
            page = "onboarding"

    # Main content
    if page == "onboarding" or not st.session_state.student_id:
        render_onboarding(db)
    elif page == "💬 Chat":
        render_chat_view(session_manager, todo_manager)
    elif page == "✅ Todo List":
        render_todo_view(todo_manager)
    elif page == "👤 Profilo":
        render_profile_view(db)
    elif page == "⚙️ Impostazioni":
        render_settings(db)

def render_onboarding(db):
    """Schermata onboarding iniziale"""
    st.title("Benvenuto! 👋")

    st.markdown("""
    Questo è il tuo assistente personale per l'orientamento post-diploma.

    **Privacy first:**
    - I tuoi dati restano sul tuo computer
    - Non raccogliamo informazioni personali sensibili
    - Puoi cancellare tutto in qualsiasi momento

    **Come funziona:**
    - Conversazioni guidate per scoprire i tuoi interessi
    - Todo-list condivise per esplorare percorsi
    - Nessuna fretta, nessun giudizio
    """)

    with st.form("onboarding_form"):
        st.subheader("Iniziamo!")

        pseudonym = st.text_input(
            "Scegli un nickname",
            placeholder="es. Alex_2024",
            help="Non serve il tuo nome vero, scegli quello che vuoi!"
        )

        accept_privacy = st.checkbox(
            "Ho letto e accetto l'informativa privacy",
            help="Tutto resta locale, zero cloud"
        )

        submitted = st.form_submit_button("Inizia il percorso 🚀")

        if submitted:
            if not pseudonym:
                st.error("Scegli un nickname per continuare")
            elif not accept_privacy:
                st.error("Devi accettare la privacy policy")
            else:
                # Crea studente
                student = db.create_student(pseudonym)
                st.session_state.student_id = student.id
                st.success(f"Perfetto {pseudonym}! Iniziamo 🎉")
                st.rerun()

if __name__ == "__main__":
    main()
```

---

## Chat View (Interfaccia Conversazione)

```python
# ui/chat_view.py

import streamlit as st
from services.session_manager import SessionManager
from services.todo_manager import TodoManager
import asyncio

def render_chat_view(session_manager: SessionManager, todo_manager: TodoManager):
    """Renderizza interfaccia chat"""

    st.title("💬 Conversazione")

    # Check se c'è sessione attiva
    if not st.session_state.current_session:
        # Mostra riassunto sessione precedente se esiste
        last_session = session_manager.get_last_session(st.session_state.student_id)

        if last_session:
            with st.expander("📋 Ultima volta abbiamo parlato di..."):
                st.write(last_session.insights)

        # Pulsante avvia nuova sessione
        if st.button("Inizia nuova sessione", type="primary"):
            st.session_state.current_session = session_manager.create_session(
                st.session_state.student_id
            )
            st.rerun()
    else:
        # Sessione attiva
        session = st.session_state.current_session

        # Header sessione
        col1, col2, col3 = st.columns([2, 1, 1])
        with col1:
            st.caption(f"Ruolo: {session.role.value.replace('_', ' ').title()}")
        with col2:
            st.caption(f"Messaggi: {session.message_count}")
        with col3:
            if st.button("Termina sessione"):
                session_manager.end_session(session.id)
                st.session_state.current_session = None
                st.rerun()

        st.divider()

        # Container messaggi
        messages_container = st.container()

        # Recupera messaggi sessione
        messages = session_manager.get_messages(session.id)

        with messages_container:
            for msg in messages:
                with st.chat_message(msg.role.value):
                    st.write(msg.content)

                    # Se ci sono todo proposte nel messaggio
                    if msg.metadata.get("proposed_todo"):
                        render_todo_proposal(
                            msg.metadata["proposed_todo"],
                            todo_manager
                        )

        # Input utente
        user_input = st.chat_input("Scrivi qui...")

        if user_input:
            # Aggiungi messaggio utente
            session_manager.add_message(
                session.id,
                "user",
                user_input
            )

            # Mostra messaggio utente
            with st.chat_message("user"):
                st.write(user_input)

            # Ottieni risposta LLM (con spinner)
            with st.chat_message("assistant"):
                with st.spinner("Sto pensando..."):
                    response = asyncio.run(
                        session_manager.get_llm_response(
                            session.id,
                            user_input
                        )
                    )
                    st.write(response)

            st.rerun()

def render_todo_proposal(todo_data: dict, todo_manager: TodoManager):
    """Renderizza proposta todo nell'interfaccia"""
    st.info(f"💡 **Proposta:** {todo_data['content']}")

    col1, col2 = st.columns(2)
    with col1:
        if st.button("✅ Accetta", key=f"accept_{todo_data['id']}"):
            todo_manager.accept_todo(todo_data['id'])
            st.success("Aggiunto alla tua lista!")
    with col2:
        if st.button("❌ No grazie", key=f"reject_{todo_data['id']}"):
            todo_manager.reject_todo(todo_data['id'])
            st.info("Ok, nessun problema!")
```

---

## Configuration

```python
# config.py

from pydantic_settings import BaseSettings
from pathlib import Path
from typing import Optional

class Settings(BaseSettings):
    # LLM
    openai_api_key: Optional[str] = None
    anthropic_api_key: Optional[str] = None
    llm_provider: str = "openai"  # openai, anthropic, ollama

    # Database
    database_path: str = "data/orientation.db"

    # Cache
    cache_ttl_scadenze: int = 6 * 3600  # 6 ore
    cache_ttl_programmi: int = 30 * 86400  # 30 giorni
    cache_ttl_mercato: int = 7 * 86400  # 7 giorni

    # Session
    session_duration_min: int = 15
    session_duration_max: int = 20
    max_searches_per_session: int = 10

    # Privacy
    enable_privacy_filters: bool = True

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

def load_config() -> Settings:
    return Settings()
```

---

## Requirements.txt

```txt
# Core
streamlit>=1.28.0
python-dotenv>=1.0.0
pydantic>=2.5.0
pydantic-settings>=2.1.0

# LLM
openai>=1.3.0
anthropic>=0.7.0

# Database
# sqlite3 è built-in

# Utilities
python-dateutil>=2.8.2
rich>=13.7.0

# Testing
pytest>=7.4.3
pytest-asyncio>=0.21.1

# Development
black>=23.12.0
ruff>=0.1.7
```

---

## Setup Rapido

### 1. Installazione

```bash
# Clone repository
git clone <repo-url>
cd itis-orientation-coach

# Virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# oppure
venv\Scripts\activate  # Windows

# Install dependencies
pip install -r requirements.txt
```

### 2. Configurazione

```bash
# Copia .env.example
cp .env.example .env

# Modifica .env con le tue API keys
# OPENAI_API_KEY=sk-...
# ANTHROPIC_API_KEY=sk-ant-...
```

### 3. Avvio

```bash
# Avvia applicazione
streamlit run app.py

# Apri browser su http://localhost:8501
```

---

## System Prompts per Ruoli

```txt
# data/prompts/esploratore_curioso.txt

Tu sei un "Esploratore Curioso" - un assistente all'orientamento per studenti ITIS.

OBIETTIVO:
Scoprire interessi autentici dello studente attraverso domande aperte, senza giudizio.

PRINCIPI:
- Mai suggerire percorsi in questa fase
- Focus su esperienze concrete, non su aspirazioni vaghe
- Validare ogni risposta senza giudicare
- Cercare pattern ma non dire "sei fatto per X"

STILE:
- Informale, curioso, amichevole
- Domande specifiche su progetti/momenti concreti
- Approfondire con "come ti sei sentito?" "cosa ti è piaciuto?"

QUANDO PASSARE AL RUOLO SUCCESSIVO:
Dopo 3-5 sessioni, quando hai mappato almeno 3-5 aree di interesse chiare.

ESEMPIO DOMANDE:
- "Raccontami di un progetto ITIS che ti ha preso davvero"
- "Quando il tempo vola senza che te ne accorgi?"
- "Quale problema risolto ti ha dato più soddisfazione?"
```

---

## Vantaggi Approccio Python

### Pro:
- **Sviluppo rapido**: giorni non settimane
- **Prototipo funzionante**: validazione concept veloce
- **Costi bassi**: nessun frontend complesso
- **Iterazione veloce**: test con utenti reali subito
- **Deploy facile**: `streamlit run app.py`

### Contro:
- UI meno personalizzabile
- Performance inferiore a desktop app nativa
- Streamlit non è vero "standalone" (serve Python installato)

### Percorso consigliato:
1. **Fase 1** (ora): Python + Streamlit per concept validation
2. **Fase 2**: Se concept funziona, migrate a Tauri/Electron
3. **Fase 3**: Scale con backend separato se serve

---

## Prossimi Step Sviluppo

### Sprint 1: Foundation (1 settimana)
- [ ] Setup progetto Python
- [ ] Database SQLite + schema
- [ ] OpenAI integration base
- [ ] UI Streamlit basic

### Sprint 2: Core Features (2 settimane)
- [ ] Session Manager con ruoli
- [ ] Chat interface completa
- [ ] Todo Manager
- [ ] Privacy filters

### Sprint 3: Enhancement (2 settimane)
- [ ] Search integration
- [ ] Document service
- [ ] Export funzionalità
- [ ] Testing

**Totale MVP: 5 settimane** (vs 15 settimane Tauri)

---

## Stima Costi

### Sviluppo:
- 5 settimane vs 15 settimane Tauri
- 70% tempo risparmiato

### Operativi:
- Identici a Tauri (LLM API costs)
- ~$1/studente per 10 sessioni

### Trade-off:
- Meno "professionale" come app
- Perfetto per validare idea
- Facile migrate dopo se funziona

---

*Documento versione 1.0 - Novembre 2024*
*Autore: Architettura Python ITIS Orientation Coach*
