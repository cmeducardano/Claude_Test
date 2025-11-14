"""
Todo Manager - Gestisce todo-list condivise
"""

from typing import List, Optional
from datetime import datetime

from core.database import Database
from core.models import TodoItem


class TodoManager:
    """Gestisce todo items"""

    def __init__(self, db: Database):
        self.db = db

    def create_todo(
        self,
        student_id: str,
        content: str,
        categoria: str,
        priorita: str = "media",
        session_id: Optional[str] = None,
        deadline_type: str = "flessibile",
        deadline_date: Optional[datetime] = None,
        motivazione: Optional[str] = None
    ) -> TodoItem:
        """Crea nuovo todo"""
        return self.db.create_todo(
            student_id=student_id,
            content=content,
            categoria=categoria,
            priorita=priorita,
            session_id=session_id,
            deadline_type=deadline_type,
            deadline_date=deadline_date,
            motivazione=motivazione
        )

    def get_todos(
        self,
        student_id: str,
        status: Optional[str] = None
    ) -> List[TodoItem]:
        """
        Recupera todos dello studente
        Se status specificato, filtra per quello status
        """
        return self.db.get_todos(student_id, status)

    def get_pending_todos(self, student_id: str) -> List[TodoItem]:
        """Recupera todos non completati (accettati o in progress)"""
        all_todos = self.db.get_todos(student_id)
        return [
            todo for todo in all_todos
            if todo.status in ["accettato", "in_progress"]
        ]

    def get_proposed_todos(self, student_id: str) -> List[TodoItem]:
        """Recupera todos proposti ma non ancora accettati"""
        return self.db.get_todos(student_id, status="proposto")

    def accept_todo(self, todo_id: str):
        """Accetta un todo proposto"""
        self.db.update_todo_status(todo_id, "accettato")

    def reject_todo(self, todo_id: str):
        """Rifiuta un todo proposto"""
        self.db.update_todo_status(todo_id, "scartato")

    def start_todo(self, todo_id: str):
        """Inizia a lavorare su un todo"""
        self.db.update_todo_status(todo_id, "in_progress")

    def complete_todo(self, todo_id: str):
        """Completa un todo"""
        self.db.update_todo_status(todo_id, "completato")

    def count_completed(self, student_id: str) -> int:
        """Conta todos completati"""
        return self.db.count_completed_todos(student_id)

    def get_acceptance_rate(self, student_id: str) -> float:
        """
        Calcola tasso di accettazione dei todo proposti
        Utile per capire se le proposte sono appropriate
        """
        all_todos = self.db.get_todos(student_id)
        if not all_todos:
            return 0.0

        proposed = [t for t in all_todos if t.status in ["proposto", "accettato", "in_progress", "completato"]]
        if not proposed:
            return 0.0

        accepted = [t for t in proposed if t.status != "scartato"]
        return len(accepted) / len(proposed)

    def get_completion_rate(self, student_id: str) -> float:
        """
        Calcola tasso di completamento dei todo accettati
        """
        accepted_todos = self.db.get_todos(student_id)
        accepted_todos = [t for t in accepted_todos if t.status in ["accettato", "in_progress", "completato"]]

        if not accepted_todos:
            return 0.0

        completed = [t for t in accepted_todos if t.status == "completato"]
        return len(completed) / len(accepted_todos)

    def get_todos_by_priority(self, student_id: str) -> dict:
        """Raggruppa todos per priorità"""
        todos = self.get_pending_todos(student_id)

        grouped = {
            "alta": [],
            "media": [],
            "bassa": [],
            "nice-to-have": []
        }

        for todo in todos:
            if todo.priorita in grouped:
                grouped[todo.priorita].append(todo)

        return grouped

    def get_celebration_message(self, todo: TodoItem) -> str:
        """Genera messaggio di celebrazione per todo completato"""
        messages = [
            f"🎉 Fantastico! Hai completato '{todo.content}'!",
            f"✅ Grande! '{todo.content}' fatto!",
            f"💪 Ottimo lavoro su '{todo.content}'!",
            f"🌟 Perfetto! Hai concluso '{todo.content}'!"
        ]

        # Scegli messaggio basato su priorità
        if todo.priorita == "alta":
            return f"🔥 WOW! Hai completato un task importante: '{todo.content}'. Sei un grande!"
        else:
            import random
            return random.choice(messages)
