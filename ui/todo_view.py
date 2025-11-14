"""
Todo View - Interfaccia todo list
"""

import streamlit as st
from datetime import datetime

from services.todo_manager import TodoManager
from core.models import TodoItem


def render_todos(todo_manager: TodoManager, student_id: str):
    """Render todo list interface"""

    st.title("✅ Le Tue Attività")

    st.markdown("""
    Qui trovi tutte le attività che abbiamo deciso insieme di esplorare.
    Ricorda: non c'è pressure, puoi modificare o scartare qualsiasi cosa!
    """)

    # Get todos
    pending_todos = todo_manager.get_pending_todos(student_id)
    proposed_todos = todo_manager.get_proposed_todos(student_id)
    completed_todos = todo_manager.get_todos(student_id, status="completato")

    # Tabs
    tab1, tab2, tab3, tab4 = st.tabs([
        f"📋 Da Fare ({len(pending_todos)})",
        f"💡 Proposte ({len(proposed_todos)})",
        f"✓ Completate ({len(completed_todos)})",
        "➕ Nuova"
    ])

    with tab1:
        render_pending_todos(todo_manager, pending_todos)

    with tab2:
        render_proposed_todos(todo_manager, proposed_todos)

    with tab3:
        render_completed_todos(completed_todos)

    with tab4:
        render_new_todo_form(todo_manager, student_id)


def render_pending_todos(todo_manager: TodoManager, todos: list):
    """Render pending todos"""
    if not todos:
        st.info("📭 Nessuna attività in corso. Vai al tab 'Nuova' per aggiungerne!")
        return

    # Group by priority
    todos_by_priority = {
        "alta": [],
        "media": [],
        "bassa": [],
        "nice-to-have": []
    }

    for todo in todos:
        if todo.priorita in todos_by_priority:
            todos_by_priority[todo.priorita].append(todo)

    # Display by priority
    priority_config = {
        "alta": ("🔥", "Priorità Alta", "#ff4444"),
        "media": ("⭐", "Priorità Media", "#ffaa00"),
        "bassa": ("💡", "Priorità Bassa", "#44aaff"),
        "nice-to-have": ("✨", "Nice to Have", "#aaaaaa")
    }

    for priority, (emoji, label, color) in priority_config.items():
        priority_todos = todos_by_priority[priority]

        if priority_todos:
            st.subheader(f"{emoji} {label}")

            for todo in priority_todos:
                render_todo_card(todo_manager, todo)


def render_todo_card(todo_manager: TodoManager, todo: TodoItem):
    """Render single todo card"""
    with st.container():
        col1, col2, col3 = st.columns([6, 2, 2])

        with col1:
            st.markdown(f"**{todo.content}**")
            if todo.motivazione:
                st.caption(f"💭 {todo.motivazione}")

        with col2:
            if todo.deadline_date:
                deadline_str = todo.deadline_date.strftime("%d/%m/%Y")
                st.caption(f"📅 {deadline_str}")

        with col3:
            if todo.status == "accettato":
                if st.button("🚀 Inizia", key=f"start_{todo.id}"):
                    todo_manager.start_todo(todo.id)
                    st.rerun()
            elif todo.status == "in_progress":
                if st.button("✓ Fatto!", key=f"complete_{todo.id}", type="primary"):
                    todo_manager.complete_todo(todo.id)
                    st.success(todo_manager.get_celebration_message(todo))
                    st.rerun()

        st.divider()


def render_proposed_todos(todo_manager: TodoManager, todos: list):
    """Render proposed todos waiting for acceptance"""
    if not todos:
        st.info("📬 Nessuna nuova proposta al momento")
        return

    st.markdown("Ecco alcune attività proposte durante le conversazioni. Cosa ne pensi?")

    for todo in todos:
        with st.container():
            st.markdown(f"### 💡 {todo.content}")

            if todo.motivazione:
                st.info(f"**Perché:** {todo.motivazione}")

            col1, col2 = st.columns(2)

            with col1:
                if st.button("✅ Mi piace, facciamolo!", key=f"accept_{todo.id}", type="primary"):
                    todo_manager.accept_todo(todo.id)
                    st.success("Aggiunto alla tua lista! 🎉")
                    st.rerun()

            with col2:
                if st.button("❌ No grazie", key=f"reject_{todo.id}"):
                    todo_manager.reject_todo(todo.id)
                    st.info("Ok, nessun problema!")
                    st.rerun()

            st.divider()


def render_completed_todos(todos: list):
    """Render completed todos"""
    if not todos:
        st.info("Nessuna attività completata ancora. Forza! 💪")
        return

    st.success(f"🎉 Hai completato {len(todos)} attività! Grande!")

    for todo in todos:
        with st.expander(f"✓ {todo.content}"):
            if todo.completed_at:
                st.caption(f"Completato il: {todo.completed_at.strftime('%d/%m/%Y alle %H:%M')}")
            if todo.motivazione:
                st.write(f"**Motivazione:** {todo.motivazione}")


def render_new_todo_form(todo_manager: TodoManager, student_id: str):
    """Render form to create new todo"""
    st.markdown("### ➕ Aggiungi Nuova Attività")

    with st.form("new_todo_form"):
        content = st.text_input(
            "Cosa vuoi fare?",
            placeholder="es: Partecipare all'open day del Politecnico"
        )

        col1, col2 = st.columns(2)

        with col1:
            categoria = st.selectbox(
                "Categoria",
                ["esplorazione", "preparazione", "riflessione", "admin"]
            )

        with col2:
            priorita = st.selectbox(
                "Priorità",
                ["media", "alta", "bassa", "nice-to-have"]
            )

        deadline_type = st.radio(
            "Tipo scadenza",
            ["flessibile", "rigida"]
        )

        deadline_date = st.date_input(
            "Data scadenza (opzionale)",
            value=None
        )

        motivazione = st.text_area(
            "Perché vuoi farlo? (opzionale)",
            placeholder="es: Voglio capire meglio com'è l'ambiente universitario"
        )

        submitted = st.form_submit_button("➕ Aggiungi", type="primary")

        if submitted:
            if not content:
                st.error("Inserisci una descrizione dell'attività")
            else:
                todo = todo_manager.create_todo(
                    student_id=student_id,
                    content=content,
                    categoria=categoria,
                    priorita=priorita,
                    deadline_type=deadline_type,
                    deadline_date=datetime.combine(deadline_date, datetime.min.time()) if deadline_date else None,
                    motivazione=motivazione if motivazione else None
                )

                # Auto-accept self-created todos
                todo_manager.accept_todo(todo.id)

                st.success("✅ Attività aggiunta alla tua lista!")
                st.rerun()
