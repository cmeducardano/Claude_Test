# CLAUDE.md - AI Assistant Guide for ITIS Orientation Coach

## Project Overview

**ITIS Orientation Coach** is a conversational AI application designed to help Italian ITIS (technical high school) students navigate post-diploma career and education decisions. Built with Streamlit and Python, it uses LLM-powered conversations across distinct coaching phases.

### Core Principles
- **Privacy-first**: All data stored locally in SQLite
- **Zero bias**: All career paths treated equally (university, ITS, work, certifications)
- **Student ownership**: Decisions belong to the student, not the AI
- **Progressive discovery**: 5 distinct LLM roles that evolve with the student's journey

## Project Structure

```
Claude_Test/
├── app.py                      # Main Streamlit entry point
├── config.py                   # Pydantic settings configuration
├── requirements.txt            # Python dependencies
├── .env.example                # Environment template
├── start_app.sh               # Local testing script
│
├── core/                       # Core domain layer
│   ├── database.py            # SQLite database operations
│   └── models.py              # Pydantic data models
│
├── services/                   # Business logic layer
│   ├── llm_service.py         # LLM abstraction (OpenAI/Anthropic)
│   ├── session_manager.py     # Conversation session handling
│   └── todo_manager.py        # Todo list management
│
├── ui/                         # Streamlit UI components
│   ├── chat_view.py           # Chat interface
│   ├── todo_view.py           # Todo list interface
│   └── profile_view.py        # Student profile view
│
├── data/
│   ├── prompts/               # System prompts for LLM roles
│   │   ├── esploratore_curioso.txt    # Sessions 1-3
│   │   ├── analista_empatico.txt      # Sessions 4-6
│   │   ├── mentore_pragmatico.txt     # Sessions 7-10
│   │   ├── coach_operativo.txt        # Sessions 11+
│   │   └── filosofo_riflessivo.txt    # Crisis moments
│   └── orientation.db         # SQLite database (generated)
│
├── docs/                       # Technical documentation (Italian)
├── tests/                      # Test directory
├── test_basic.py              # Basic functionality tests
└── test_interactive.py        # Interactive demo tests
```

## Key Architecture Concepts

### LLM Role System
The application uses 5 distinct LLM personas that activate based on session count:

| Role | Sessions | Purpose |
|------|----------|---------|
| `esploratore_curioso` | 1-3 | Discovery phase - map interests without suggesting paths |
| `analista_empatico` | 4-6 | Deep dive into skills and values |
| `mentore_pragmatico` | 7-10 | Present concrete path options |
| `coach_operativo` | 11+ | Action-oriented preparation support |
| `filosofo_riflessivo` | Special | Handle decision crises and doubts |

Role selection is automatic via `LLMService.determine_role()` in `services/llm_service.py:223`.

### Data Models (core/models.py)
- `Student`: User profile with pseudonym and status
- `Session`: Conversational session with role and insights
- `Message`: Individual chat messages (user/assistant/system)
- `TodoItem`: Tasks with categories (esplorazione/preparazione/riflessione/admin)
- `StudentStatus`: EXPLORING -> DECIDING -> PREPARING -> COMPLETED

### Database Schema
SQLite tables: `students`, `sessions`, `messages`, `todos`, `student_profile`, `paths`
Database auto-initializes on first run in `data/orientation.db`.

## Development Workflow

### Setup
```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
pip install -r requirements.txt
cp .env.example .env
# Add API key to .env
```

### Running the Application
```bash
streamlit run app.py
```
App runs at `http://localhost:8501`

### Running Tests
```bash
# Basic functionality tests
python test_basic.py

# Interactive demo
python test_interactive.py

# Pytest suite
pytest tests/
```

### Code Quality
```bash
black .                  # Format code
ruff check .            # Lint
```

## Key Conventions

### Language
- **Code**: English for all code, comments, and technical documentation
- **User-facing content**: Italian (prompts, UI text, error messages)
- **System prompts**: Italian (in `data/prompts/`)

### Code Style
- Python 3.11+ required
- Pydantic v2 for data validation and settings
- Type hints throughout
- Async/await for LLM calls
- SQLite for local persistence (no external database)

### LLM Provider Abstraction
All LLM calls go through `LLMService` which wraps provider-specific implementations:
- `OpenAIProvider`: GPT-4 Turbo
- `AnthropicProvider`: Claude 3 Opus
- `MockLLMProvider`: Demo mode (no API key needed)

To add a new provider, implement `LLMProvider` abstract base class in `services/llm_service.py`.

### Session State Management
Streamlit session state keys:
- `student_id`: Current logged-in student UUID
- `current_session_id`: Active conversation session UUID
- `page`: Current view (chat/todo/profilo)

### Privacy Considerations
- **NEVER** store personally identifiable information
- Redirect sensitive disclosures (health, religion, family issues)
- Use pseudonyms only - no real names
- All data stays local in SQLite
- Privacy filters enabled by default (`ENABLE_PRIVACY_FILTERS=true`)

## Common Tasks

### Adding a New LLM Role
1. Create prompt file in `data/prompts/<role_name>.txt`
2. Add enum value in `core/models.py:LLMRole`
3. Add default prompt in `services/llm_service.py:_get_default_prompt()`
4. Update role selection logic in `determine_role()` if needed

### Adding a New UI View
1. Create view file in `ui/<view_name>.py`
2. Add render function following `render_<view>()` pattern
3. Import and call from `app.py` main routing

### Modifying Database Schema
1. Update `core/database.py:_init_schema()` with new table/columns
2. Add corresponding Pydantic model in `core/models.py`
3. Add CRUD methods to `Database` class
4. Test with `python test_basic.py`

### Adding New Todo Categories
1. Update CHECK constraint in `core/database.py` todos table
2. Add to UI dropdown in `ui/todo_view.py:render_new_todo_form()`

## Testing Guidelines

### Test Files
- `test_basic.py`: Core functionality validation (database, models, services)
- `test_interactive.py`: Demo script with mock LLM responses
- `tests/`: Pytest-compatible test suite (WIP)

### Running Tests Without API Key
The application includes `MockLLMProvider` which returns scripted responses. This enables testing all functionality without an API key.

### Manual Testing Workflow
1. Run `python test_basic.py` to verify core functionality
2. Start app with `streamlit run app.py`
3. Create test student with any nickname
4. Verify chat, todo, and profile views work
5. Database stored in `data/orientation.db` (gitignored)

## Configuration Reference

### Environment Variables (.env)
```
OPENAI_API_KEY=sk-...           # OpenAI API key
ANTHROPIC_API_KEY=sk-ant-...    # Anthropic API key
LLM_PROVIDER=openai             # openai, anthropic, or ollama
DATABASE_PATH=data/orientation.db
SESSION_DURATION_MIN=15
SESSION_DURATION_MAX=20
ENABLE_PRIVACY_FILTERS=true
```

### Session Duration
Sessions auto-suggest closing after:
- 20 minutes elapsed, OR
- 12+ messages exchanged

Logic in `SessionManager.should_close_session()`.

## Important Notes for AI Assistants

### When Modifying Code
1. Maintain Italian for all user-facing strings
2. Keep prompts in separate `.txt` files, not hardcoded
3. Use Pydantic models for data validation
4. Preserve async patterns for LLM calls
5. Test database changes with `test_basic.py`

### When Adding Features
1. Check if it fits existing architecture layers (core/services/ui)
2. Follow the existing patterns for similar features
3. Add to roadmap in README.md if significant
4. Consider privacy implications

### When Debugging
- Check `data/orientation.db` for database state
- MockLLMProvider helps isolate LLM issues
- Streamlit errors often require `st.rerun()` handling
- Session state persists until browser refresh

### Files to Never Modify
- `.env` (contains secrets - only `.env.example`)
- `data/*.db` (runtime generated)
- `data/*.db-journal` (SQLite temp files)

## Status & Roadmap

**Current Version**: 1.0.0 (MVP)

### Completed
- Core LLM integration with role system
- Session management with automatic role progression
- Todo list with proposals from conversations
- SQLite local storage
- Basic Streamlit UI
- Privacy filters
- Mock provider for testing

### Planned
- Web search integration (university/ITS info)
- PDF/Markdown export
- Analytics dashboard
- Complete test suite
- Ollama support (local LLM)
- Multi-school deployment support
