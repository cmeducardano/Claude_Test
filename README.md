# ITIS Orientation Coach

> Assistente conversazionale basato su LLM per l'orientamento post-diploma degli studenti ITIS

## Descrizione

Applicazione Python autocontenuta che accompagna gli studenti ITIS nella scoperta e preparazione del percorso post-diploma attraverso sessioni conversazionali guidate, con focus su:

- **Privacy-first**: Tutti i dati restano locali
- **Personalizzazione**: Ruoli LLM dinamici in base alla fase del percorso
- **Supporto concreto**: Todo-list condivise e tracciamento progressi
- **Zero bias**: Tutti i percorsi sono validi (tecnici e non)

## Caratteristiche

### Ruoli LLM Dinamici

Il sistema utilizza 5 ruoli che si alternano automaticamente:

1. **Esploratore Curioso** (sessioni 1-3): Discovery iniziale, mappatura interessi
2. **Analista Empatico** (sessioni 4-6): Approfondimento competenze e valori
3. **Mentore Pragmatico** (sessioni 7-10): Matching percorsi concreti
4. **Coach Operativo** (sessioni 11+): Supporto preparazione e azione
5. **Filosofo Riflessivo** (speciali): Gestione crisi decisionali

### Funzionalità Principali

- Conversazioni guidate con continuità tra sessioni
- Sistema todo-list con proposte collaborative
- Tracciamento progressi e insights
- Database SQLite locale
- Interfaccia Streamlit user-friendly
- Multi-provider LLM (OpenAI, Anthropic)

## Setup

### Prerequisiti

- Python 3.11+
- API Key OpenAI o Anthropic

### Installazione

1. **Clone repository**
```bash
git clone <repo-url>
cd itis-orientation-coach
```

2. **Crea virtual environment**
```bash
python -m venv venv

# Linux/Mac
source venv/bin/activate

# Windows
venv\Scripts\activate
```

3. **Installa dipendenze**
```bash
pip install -r requirements.txt
```

4. **Configura API keys**
```bash
cp .env.example .env
# Modifica .env con la tua API key
```

File `.env`:
```
OPENAI_API_KEY=sk-your-api-key-here
LLM_PROVIDER=openai
```

### Avvio

```bash
streamlit run app.py
```

L'applicazione si aprirà automaticamente nel browser su `http://localhost:8501`

## Struttura Progetto

```
itis-orientation-coach/
├── app.py                      # Entry point Streamlit
├── config.py                   # Configurazione
├── requirements.txt
├── .env.example
│
├── core/                       # Moduli core
│   ├── database.py            # SQLite database
│   └── models.py              # Pydantic models
│
├── services/                   # Business logic
│   ├── llm_service.py         # LLM provider abstraction
│   ├── session_manager.py     # Gestione sessioni
│   └── todo_manager.py        # Gestione todo
│
├── ui/                        # Streamlit views
│   ├── chat_view.py           # Interfaccia chat
│   ├── todo_view.py           # Todo list UI
│   └── profile_view.py        # Profilo studente
│
├── data/
│   ├── prompts/               # System prompts per ruoli
│   │   ├── esploratore_curioso.txt
│   │   ├── analista_empatico.txt
│   │   ├── mentore_pragmatico.txt
│   │   ├── coach_operativo.txt
│   │   └── filosofo_riflessivo.txt
│   └── orientation.db         # Database SQLite (generato)
│
└── docs/
    ├── specifiche-funzionali.md
    ├── architettura-tecnica.md
    └── architettura-python.md
```

## Utilizzo

### Onboarding

1. Scegli un nickname (non serve nome vero)
2. Accetta privacy policy
3. Inizia il percorso

### Conversazione

1. Clicca "Inizia Nuova Sessione"
2. Il sistema sceglie automaticamente il ruolo appropriato
3. Conversa liberamente - il sistema ti guida
4. Le sessioni durano 15-20 minuti circa

### Todo List

- Visualizza attività proposte durante conversazioni
- Accetta/rifiuta proposte
- Traccia progressi
- Aggiungi todo personalizzate

### Profilo

- Visualizza statistiche
- Storico sessioni
- Gestisci dati e privacy

## Configurazione Avanzata

### Cambiare Provider LLM

Nel file `.env`:

```bash
# OpenAI
LLM_PROVIDER=openai
OPENAI_API_KEY=sk-...

# Anthropic
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-...
```

### Personalizzare Prompts

Modifica i file in `data/prompts/` per personalizzare il comportamento dei ruoli LLM.

### Database

Il database SQLite viene creato automaticamente in `data/orientation.db`. Contiene:

- Studenti e profili
- Sessioni e messaggi
- Todo items
- Percorsi esplorati

## Privacy e Sicurezza

- **Dati locali**: Tutto resta sul computer dell'utente
- **No dati sensibili**: Sistema rifiuta e redirecta info personali
- **GDPR compliant**: Design privacy-first
- **Export/Delete**: Utente ha pieno controllo

## Development

### Testing

```bash
pytest tests/
```

### Code Style

```bash
# Format code
black .

# Lint
ruff check .
```

## Roadmap

### MVP (Completato)
- [x] Core LLM integration
- [x] Sistema sessioni con ruoli
- [x] Todo management
- [x] Database SQLite
- [x] UI Streamlit base
- [x] Privacy filters

### Next Steps
- [ ] Search integration (web, università, ITS)
- [ ] Document service (export PDF/Markdown)
- [ ] Analytics dashboard
- [ ] Test suite completo
- [ ] Deploy guide

### Future
- [ ] Ollama support (LLM locale)
- [ ] Multi-scuola support
- [ ] Mobile app nativa
- [ ] Community features

## Contribuire

Questo è un progetto in fase concept. Contributi benvenuti!

## License

MIT License - Vedi LICENSE file

## Credits

Sviluppato per supportare studenti ITIS nel percorso post-diploma con approccio:
- Privacy-first
- Personalizzato
- Senza bias precostituiti
- Supporto concreto

---

**Versione**: 1.0.0 (MVP)
**Status**: Concept Phase
**Data**: Novembre 2024
