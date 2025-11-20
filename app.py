"""
ITIS Orientation Coach - Main Streamlit Application
"""

import streamlit as st
import asyncio
from pathlib import Path

from core.database import Database
from core.models import StudentStatus
from services.llm_service import LLMService, OpenAIProvider, AnthropicProvider
from services.session_manager import SessionManager
from services.todo_manager import TodoManager
from config import load_config


# Page configuration
st.set_page_config(
    page_title="ITIS Orientation Coach",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="expanded"
)


# Custom CSS
st.markdown("""
<style>
    .main-header {
        font-size: 2.5rem;
        font-weight: bold;
        color: #1f77b4;
    }
    .stat-card {
        padding: 1rem;
        border-radius: 0.5rem;
        background-color: #f0f2f6;
    }
</style>
""", unsafe_allow_html=True)


class MockLLMProvider:
    """Provider LLM simulato per demo senza API key"""
    async def chat(self, messages, tools=None):
        last_msg = messages[-1]["content"] if messages else ""

        if "ciao" in last_msg.lower() or not last_msg:
            return """Ciao! 👋 Sono qui per aiutarti a scoprire cosa ti appassiona davvero.
Non c'è fretta e non ci sono risposte giuste o sbagliate.

Raccontami: c'è stato un progetto o un'attività che hai fatto a scuola che ti ha davvero preso?"""

        elif any(word in last_msg.lower() for word in ["progetto", "arduino", "coding", "programma"]):
            return """Interessante! 🤔 Mi piace sentire che ti sei appassionato a un progetto concreto.

Dimmi: quando lavoravi a questo progetto, cosa ti piaceva di più?
Era il vedere i risultati subito, risolvere problemi, o lavorare con altre persone?"""

        return """Capisco. Ogni esperienza ci insegna qualcosa su noi stessi.

C'è qualcos'altro che ti viene in mente quando pensi a cosa ti piace fare?
Anche attività fuori dalla scuola vanno benissimo!"""


@st.cache_resource
def init_services():
    """Initialize database and services"""
    config = load_config()

    # Database
    db = Database(config.database_path)

    # LLM Provider - usa Mock se non c'è API key
    if config.llm_provider == "openai" and config.openai_api_key:
        provider = OpenAIProvider(api_key=config.openai_api_key)
        st.sidebar.success("🤖 LLM: OpenAI GPT-4")
    elif config.llm_provider == "anthropic" and config.anthropic_api_key:
        provider = AnthropicProvider(api_key=config.anthropic_api_key)
        st.sidebar.success("🤖 LLM: Anthropic Claude")
    else:
        provider = MockLLMProvider()
        st.sidebar.warning("🤖 LLM: Modalità DEMO (simulato)")
        st.sidebar.info("💡 Aggiungi OPENAI_API_KEY in .env per usare LLM reale")

    # Services
    llm_service = LLMService(provider)
    session_manager = SessionManager(db, llm_service)
    todo_manager = TodoManager(db)

    return db, session_manager, todo_manager, config


def init_session_state():
    """Initialize Streamlit session state"""
    if "student_id" not in st.session_state:
        st.session_state.student_id = None
    if "current_session" not in st.session_state:
        st.session_state.current_session = None
    if "page" not in st.session_state:
        st.session_state.page = "chat"


def render_onboarding(db: Database):
    """Render onboarding screen"""
    st.markdown('<p class="main-header">🎓 Benvenuto!</p>', unsafe_allow_html=True)

    st.markdown("""
    Questo è il tuo assistente personale per l'orientamento post-diploma.

    ### Privacy First 🔒
    - I tuoi dati restano sul tuo computer
    - Non raccogliamo informazioni personali sensibili
    - Puoi cancellare tutto in qualsiasi momento

    ### Come Funziona 💬
    - Conversazioni guidate per scoprire i tuoi interessi
    - Todo-list condivise per esplorare percorsi
    - Nessuna fretta, nessun giudizio

    ### Il Percorso 🚀
    1. **Esplorazione** (3-4 sessioni): Scopriamo insieme cosa ti interessa
    2. **Analisi** (2-3 sessioni): Approfondiamo competenze e valori
    3. **Matching** (3-4 sessioni): Esploriamo percorsi concreti
    4. **Preparazione**: Ti supportiamo nella realizzazione
    """)

    st.divider()

    with st.form("onboarding_form"):
        st.subheader("Iniziamo! 🎯")

        pseudonym = st.text_input(
            "Scegli un nickname",
            placeholder="es. Alex_2024, CodeMaster, etc.",
            help="Non serve il tuo nome vero, scegli quello che vuoi!"
        )

        accept_privacy = st.checkbox(
            "Ho letto e accetto l'informativa privacy",
            help="I tuoi dati restano completamente locali sul tuo computer"
        )

        submitted = st.form_submit_button("Inizia il percorso 🚀", type="primary")

        if submitted:
            if not pseudonym:
                st.error("Scegli un nickname per continuare")
            elif not accept_privacy:
                st.error("Devi accettare la privacy policy")
            elif db.get_student_by_pseudonym(pseudonym):
                st.error("Questo nickname è già in uso, scegline un altro")
            else:
                # Create student
                student = db.create_student(pseudonym)
                st.session_state.student_id = student.id
                st.success(f"Perfetto {pseudonym}! Iniziamo 🎉")
                st.rerun()


def render_sidebar(db: Database, session_manager: SessionManager, todo_manager: TodoManager):
    """Render sidebar with navigation and stats"""
    with st.sidebar:
        st.title("🎓 ITIS Orientation")

        if st.session_state.student_id:
            student = db.get_student(st.session_state.student_id)

            if student:
                st.success(f"👋 Ciao **{student.pseudonym}**!")

                # Navigation
                st.divider()
                page = st.radio(
                    "Navigazione",
                    ["💬 Chat", "✅ Todo List", "👤 Profilo"],
                    key="nav_radio"
                )
                st.session_state.page = page.split()[1].lower()

                # Quick stats
                st.divider()
                st.subheader("📊 I tuoi progressi")

                col1, col2 = st.columns(2)
                with col1:
                    sessions_count = session_manager.count_sessions(student.id)
                    st.metric("Sessioni", sessions_count)
                with col2:
                    todos_completed = todo_manager.count_completed(student.id)
                    st.metric("Todo ✓", todos_completed)

                # Status
                status_emoji = {
                    StudentStatus.EXPLORING: "🔍",
                    StudentStatus.DECIDING: "🤔",
                    StudentStatus.PREPARING: "📚",
                    StudentStatus.COMPLETED: "🎯"
                }
                st.info(f"{status_emoji.get(student.status, '🔍')} Fase: **{student.status.value}**")


def render_chat_view(session_manager: SessionManager, todo_manager: TodoManager):
    """Render chat interface"""
    from ui.chat_view import render_chat
    render_chat(session_manager, todo_manager, st.session_state.student_id)


def render_todo_view(todo_manager: TodoManager):
    """Render todo list interface"""
    from ui.todo_view import render_todos
    render_todos(todo_manager, st.session_state.student_id)


def render_profile_view(db: Database, session_manager: SessionManager):
    """Render profile view"""
    from ui.profile_view import render_profile
    render_profile(db, session_manager, st.session_state.student_id)


def main():
    """Main application"""
    # Initialize
    init_session_state()
    db, session_manager, todo_manager, config = init_services()

    # Check if student logged in
    if not st.session_state.student_id:
        render_onboarding(db)
    else:
        # Render sidebar
        render_sidebar(db, session_manager, todo_manager)

        # Render main content based on selected page
        if st.session_state.page == "chat":
            render_chat_view(session_manager, todo_manager)
        elif st.session_state.page == "todo":
            render_todo_view(todo_manager)
        elif st.session_state.page == "profilo":
            render_profile_view(db, session_manager)


if __name__ == "__main__":
    main()
