"""
Chat View - Interfaccia conversazione con scaffolding graduale
"""

import streamlit as st
import asyncio
from datetime import datetime

from services.session_manager import SessionManager
from services.todo_manager import TodoManager
from core.models import LLMRole


def render_progress_bar(session_manager: SessionManager, student_id: str):
    """Mostra barra di progresso con fase attuale"""
    progress_info = session_manager.get_progress_info(student_id)
    role_info = progress_info.get("role_info", {})

    if role_info.get("progresso"):
        # Barra di progresso visiva
        col1, col2 = st.columns([3, 1])
        with col1:
            st.progress(role_info["progresso"] / 100)
        with col2:
            st.caption(f"{role_info['progresso']}%")

        # Info sulla fase
        st.caption(
            f"{role_info.get('icona', '')} **{role_info.get('fase', '')}** - "
            f"{role_info.get('descrizione', '')}"
        )
    else:
        # Ruolo Filosofo (fuori dal flusso lineare)
        st.info(f"🤔 **Modalità Riflessione** - {role_info.get('descrizione', '')}")


def render_session_controls(session_manager: SessionManager, student_id: str):
    """Mostra controlli per navigare tra le fasi"""
    progress_info = session_manager.get_progress_info(student_id)

    st.markdown("---")
    st.caption("⚙️ **Controlli sessione**")

    col1, col2 = st.columns(2)

    with col1:
        # Pulsante per tornare indietro
        if progress_info.get("can_go_back"):
            prev_role = progress_info.get("previous_role")
            if prev_role:
                prev_info = session_manager.llm_service.get_role_progress_info(prev_role)
                if st.button(
                    f"⬅️ Torna a: {prev_info.get('fase', 'Fase precedente')}",
                    help="Se ti senti perso, puoi tornare alla fase precedente",
                    use_container_width=True
                ):
                    st.session_state.go_back_requested = True
                    st.rerun()

    with col2:
        # Pulsante per attivare il Filosofo
        current_role = progress_info.get("current_role")
        if current_role != LLMRole.FILOSOFO:
            if st.button(
                "🤔 Ho bisogno di riflettere",
                help="Attiva una sessione di riflessione per gestire dubbi o confusione",
                use_container_width=True
            ):
                st.session_state.filosofo_requested = True
                st.rerun()


def render_chat(session_manager: SessionManager, todo_manager: TodoManager, student_id: str):
    """Render chat interface con scaffolding graduale"""

    st.title("💬 Conversazione")

    # Mostra sempre la barra di progresso
    render_progress_bar(session_manager, student_id)
    st.divider()

    # Check if there's an active session
    if "current_session_id" not in st.session_state or not st.session_state.current_session_id:
        # No active session - show start options
        last_session = session_manager.get_last_session(student_id)

        # Mostra recap delle sessioni precedenti
        recap = session_manager.generate_session_recap(student_id)
        if recap:
            with st.expander("📋 Recap del tuo percorso", expanded=True):
                st.markdown(recap)

        if last_session and last_session.ended_at and last_session.insights:
            with st.expander("💡 Insight dall'ultima sessione"):
                for insight in last_session.insights:
                    st.write(f"- {insight}")

        st.info("👋 Pronto per continuare il tuo percorso?")

        # Controlli per la nuova sessione
        col1, col2, col3 = st.columns([2, 1, 1])

        with col1:
            if st.button("🚀 Continua il percorso", type="primary", use_container_width=True):
                # Gestisci richieste speciali
                go_back = st.session_state.get("go_back_requested", False)
                filosofo = st.session_state.get("filosofo_requested", False)

                if filosofo:
                    session = session_manager.create_session_with_role_override(
                        student_id, role_override=LLMRole.FILOSOFO
                    )
                    st.session_state.filosofo_requested = False
                elif go_back:
                    session = session_manager.create_session_with_role_override(
                        student_id, go_back=True
                    )
                    st.session_state.go_back_requested = False
                else:
                    session = session_manager.create_session(student_id)

                st.session_state.current_session_id = session.id

                # Add welcome message with recap context
                welcome_msg = get_welcome_message(session.role.value, recap)
                session_manager.add_message(session.id, "assistant", welcome_msg)

                st.rerun()

        with col2:
            progress_info = session_manager.get_progress_info(student_id)
            if progress_info.get("can_go_back"):
                if st.button("⬅️ Torna indietro", use_container_width=True):
                    st.session_state.go_back_requested = True
                    st.rerun()

        with col3:
            if st.button("🤔 Rifletti", use_container_width=True):
                st.session_state.filosofo_requested = True
                st.rerun()

        # Mostra stato delle richieste speciali
        if st.session_state.get("go_back_requested"):
            st.warning("⬅️ La prossima sessione tornerà alla fase precedente. Clicca 'Continua il percorso' per confermare.")
        if st.session_state.get("filosofo_requested"):
            st.warning("🤔 La prossima sessione sarà dedicata alla riflessione. Clicca 'Continua il percorso' per confermare.")

    else:
        # Active session exists
        session_id = st.session_state.current_session_id
        session = session_manager.get_session(session_id)

        if not session:
            st.error("Sessione non trovata")
            st.session_state.current_session_id = None
            st.rerun()
            return

        # Session header
        col1, col2, col3 = st.columns([3, 1, 1])

        with col1:
            role_name = session.role.value.replace("_", " ").title()
            st.caption(f"🎭 Modalità: **{role_name}**")

        with col2:
            duration = (datetime.now() - session.started_at).seconds // 60
            st.caption(f"⏱️ {duration} min")

        with col3:
            if st.button("🏁 Termina", type="secondary"):
                # End session
                insights = ["Sessione completata"]  # Could be generated by LLM
                session_manager.end_session(session_id, insights)
                st.session_state.current_session_id = None
                st.success("Sessione terminata! Ottimo lavoro 💪")
                st.rerun()

        st.divider()

        # Messages container
        messages = session_manager.get_messages(session_id)

        # Display messages
        for msg in messages:
            with st.chat_message(msg.role.value):
                st.write(msg.content)

        # User input
        user_input = st.chat_input("Scrivi qui il tuo messaggio...")

        if user_input:
            # Display user message immediately
            with st.chat_message("user"):
                st.write(user_input)

            # Get LLM response
            with st.chat_message("assistant"):
                with st.spinner("Sto pensando..."):
                    try:
                        response = asyncio.run(
                            session_manager.get_llm_response(session_id, user_input)
                        )
                        st.write(response)
                    except Exception as e:
                        st.error(f"Errore: {str(e)}")

            # Check if session should close
            if session_manager.should_close_session(session_id):
                st.info("💡 Abbiamo parlato per un po'! Vuoi terminare la sessione?")

            st.rerun()


def get_welcome_message(role: str, recap: str = "") -> str:
    """
    Get welcome message based on role.
    Include recap context for transitional roles.
    """
    messages = {
        # Ruoli principali
        "esploratore_curioso": "Ciao! 👋 Sono qui per aiutarti a scoprire cosa ti appassiona davvero. Non c'è fretta e non ci sono risposte giuste o sbagliate. Raccontami: c'è stato un progetto o un'attività che hai fatto a scuola che ti ha proprio preso?",

        "analista_empatico": "Bentornato! 🌟 Abbiamo già iniziato a scoprire alcuni tuoi interessi. Oggi vorrei approfondire un po' di più. Cosa rende una giornata 'bella' per te?",

        "mentore_pragmatico": "Ciao! 👔 Basandomi su quello che abbiamo scoperto finora, oggi possiamo iniziare a parlare di percorsi concreti. Non ti sto vendendo niente, solo esplorando opzioni insieme. Sei pronto?",

        "coach_operativo": "Hey! 💪 È il momento di passare all'azione! Vediamo insieme come realizzare i tuoi piani. Hai già qualche idea di cosa vorresti fare per primo?",

        "filosofo_riflessivo": "Ciao 🧘 Vedo che magari hai qualche dubbio o confusione, ed è completamente normale. Prenditi il tempo che serve. Di cosa vuoi parlare?",

        # Ruoli di transizione (scaffolding graduale)
        "esploratore_analizzante": """Bentornato! 🔍→📊

Nelle nostre ultime chiacchierate ho iniziato a notare alcuni pattern interessanti. Oggi vorrei condividerli con te e vedere se ti ritrovi.

Non sono diagnosi, solo osservazioni da esplorare insieme. Correggimi se sbaglio!

Da quello che hai raccontato, mi sembra che ti appassioni quando... [continueremo insieme a scoprirlo]. Ti va di approfondire?""",

        "analista_orientante": """Bentornato! 📊→🎯

Abbiamo fatto un bel percorso insieme! Ora abbiamo un quadro più chiaro di:
- Cosa ti appassiona
- Quali sono i tuoi punti di forza
- Cosa è importante per te

Oggi vorrei fare una sintesi insieme e iniziare a pensare ai CRITERI che userai per scegliere il tuo percorso. Non ancora opzioni concrete, ma i parametri di scelta.

Che ne dici, partiamo?""",

        "mentore_attivante": """Bentornato! 🎯→🚀

Wow, abbiamo esplorato un sacco di cose insieme! Hai valutato diverse opzioni e dovresti avere un'idea più chiara.

Oggi è il momento di fare il punto:
- Hai già scelto? O sei ancora tra alcune opzioni?
- Cosa ti aiuterebbe a decidere?
- Quali sono i primi passi concreti?

Dalla prossima sessione diventerò il tuo "Coach Operativo" e ti aiuterò a realizzare il piano. Ma prima... come ti senti riguardo alle tue scelte?"""
    }

    base_message = messages.get(role, "Ciao! Come posso aiutarti oggi?")

    # Per i ruoli di transizione, il messaggio già include il contesto
    # Per gli altri ruoli, aggiungi un breve riferimento al recap se presente
    if recap and role not in ["esploratore_analizzante", "analista_orientante", "mentore_attivante"]:
        if role == "esploratore_curioso":
            return base_message  # Prima sessione, nessun recap
        else:
            return f"{base_message}\n\n*(Ricordo le nostre sessioni precedenti e continuo da lì)*"

    return base_message
