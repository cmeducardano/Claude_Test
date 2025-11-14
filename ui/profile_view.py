"""
Profile View - Visualizzazione profilo studente
"""

import streamlit as st
from datetime import datetime

from core.database import Database
from services.session_manager import SessionManager


def render_profile(db: Database, session_manager: SessionManager, student_id: str):
    """Render profile view"""

    st.title("👤 Il Tuo Profilo")

    student = db.get_student(student_id)

    if not student:
        st.error("Profilo non trovato")
        return

    # Header
    col1, col2, col3 = st.columns(3)

    with col1:
        st.metric("Nickname", student.pseudonym)

    with col2:
        days_active = (datetime.now() - student.created_at).days
        st.metric("Giorni attivi", days_active)

    with col3:
        session_count = session_manager.count_sessions(student_id)
        st.metric("Sessioni totali", session_count)

    st.divider()

    # Tabs
    tab1, tab2, tab3 = st.tabs(["📊 Statistiche", "📜 Storico Sessioni", "⚙️ Impostazioni"])

    with tab1:
        render_statistics(db, session_manager, student_id)

    with tab2:
        render_session_history(db, session_manager, student_id)

    with tab3:
        render_settings(db, student)


def render_statistics(db: Database, session_manager: SessionManager, student_id: str):
    """Render statistics"""
    st.subheader("📊 Le Tue Statistiche")

    # Session stats
    sessions = session_manager.count_sessions(student_id)

    # Todo stats
    from services.todo_manager import TodoManager
    todo_manager = TodoManager(db)

    total_todos = len(todo_manager.get_todos(student_id))
    completed_todos = todo_manager.count_completed(student_id)
    completion_rate = todo_manager.get_completion_rate(student_id)

    col1, col2 = st.columns(2)

    with col1:
        st.markdown("### 💬 Conversazioni")
        st.metric("Sessioni completate", sessions)

    with col2:
        st.markdown("### ✅ Attività")
        st.metric("Completate", f"{completed_todos}/{total_todos}")
        if total_todos > 0:
            st.progress(completion_rate)
            st.caption(f"{int(completion_rate * 100)}% tasso completamento")


def render_session_history(db: Database, session_manager: SessionManager, student_id: str):
    """Render session history"""
    st.subheader("📜 Storico Sessioni")

    # Get all sessions (would need to add this method to database)
    st.info("Funzionalità in arrivo: qui vedrai lo storico completo delle tue sessioni")


def render_settings(db: Database, student):
    """Render settings"""
    st.subheader("⚙️ Impostazioni")

    st.markdown("### Privacy e Dati")

    st.info(f"""
    **I tuoi dati:**
    - Nickname: {student.pseudonym}
    - Creato il: {student.created_at.strftime("%d/%m/%Y")}
    - Tutti i dati sono salvati localmente sul tuo computer
    - Nessuna informazione viene inviata a server esterni (tranne le chiamate al modello LLM)
    """)

    st.markdown("### Azioni")

    col1, col2 = st.columns(2)

    with col1:
        if st.button("📥 Esporta Dati", type="secondary", use_container_width=True):
            st.info("Funzionalità in arrivo: potrai esportare tutti i tuoi dati in formato JSON/PDF")

    with col2:
        if st.button("🗑️ Elimina Tutto", type="secondary", use_container_width=True):
            st.warning("⚠️ Questa azione è irreversibile!")

            if st.checkbox("Confermo di voler eliminare tutti i miei dati"):
                if st.button("Sì, elimina tutto", type="primary"):
                    # Would delete all student data
                    st.error("Funzionalità in arrivo: eliminazione dati")
