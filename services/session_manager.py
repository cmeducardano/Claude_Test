"""
Session Manager - Gestisce sessioni conversazionali
"""

from typing import Optional, List, Dict
from datetime import datetime, timedelta

from core.database import Database
from core.models import Session, Message, LLMRole, MessageRole, StudentStatus
from services.llm_service import LLMService


class SessionManager:
    """Gestisce sessioni conversazionali"""

    def __init__(self, db: Database, llm_service: LLMService):
        self.db = db
        self.llm_service = llm_service

    def create_session(self, student_id: str, role: Optional[LLMRole] = None) -> Session:
        """
        Crea nuova sessione per lo studente
        Se role non specificato, viene determinato automaticamente
        """
        # Determine role if not specified
        if role is None:
            session_count = self.db.count_sessions(student_id)
            student = self.db.get_student(student_id)
            role = self.llm_service.determine_role(
                session_count,
                student.status.value if student else "exploring"
            )

        # Create session
        session = self.db.create_session(student_id, role)
        return session

    def get_session(self, session_id: str) -> Optional[Session]:
        """Recupera sessione"""
        return self.db.get_session(session_id)

    def get_last_session(self, student_id: str) -> Optional[Session]:
        """Recupera ultima sessione dello studente"""
        return self.db.get_last_session(student_id)

    def count_sessions(self, student_id: str) -> int:
        """Conta sessioni dello studente"""
        return self.db.count_sessions(student_id)

    def end_session(self, session_id: str, insights: List[str] = None):
        """Termina sessione corrente"""
        self.db.end_session(session_id, insights or [])

    def add_message(
        self,
        session_id: str,
        role: str,  # "user" or "assistant"
        content: str,
        metadata: Optional[Dict] = None
    ) -> Message:
        """Aggiunge messaggio alla sessione"""
        message_role = MessageRole.USER if role == "user" else MessageRole.ASSISTANT
        return self.db.add_message(session_id, message_role, content, metadata)

    def get_messages(self, session_id: str) -> List[Message]:
        """Recupera tutti i messaggi della sessione"""
        return self.db.get_messages(session_id)

    async def get_llm_response(
        self,
        session_id: str,
        user_message: str
    ) -> str:
        """
        Ottiene risposta LLM per il messaggio utente
        1. Aggiunge messaggio utente al database
        2. Recupera contesto conversazione
        3. Chiama LLM con ruolo appropriato
        4. Salva risposta nel database
        5. Ritorna risposta
        """
        # Get session
        session = self.db.get_session(session_id)
        if not session:
            return "Errore: sessione non trovata"

        # Add user message
        self.add_message(session_id, "user", user_message)

        # Get conversation history
        messages = self.db.get_messages(session_id)

        # Build student context
        student = self.db.get_student(session.student_id)
        todos = self.db.get_todos(session.student_id, status="accettato")

        student_context = {
            "status": student.status.value if student else "exploring",
            "session_count": self.db.count_sessions(session.student_id),
            "pending_todos": todos
        }

        # Get LLM response
        response = await self.llm_service.get_response(
            messages,
            session.role,
            student_context
        )

        # Save assistant message
        self.add_message(session_id, "assistant", response)

        return response

    def should_close_session(self, session_id: str) -> bool:
        """
        Determina se la sessione dovrebbe essere chiusa
        Basato su durata (15-20 minuti) o numero messaggi
        """
        session = self.db.get_session(session_id)
        if not session:
            return False

        # Check duration
        duration = datetime.now() - session.started_at
        if duration > timedelta(minutes=20):
            return True

        # Check message count (suggerimento soft dopo 10-12 messaggi)
        if session.message_count > 12:
            return True

        return False

    def get_session_summary(self, session_id: str) -> Dict:
        """Genera riassunto della sessione"""
        session = self.db.get_session(session_id)
        if not session:
            return {}

        messages = self.db.get_messages(session_id)

        return {
            "role": session.role.value,
            "duration_minutes": session.duration_seconds // 60 if session.duration_seconds else 0,
            "message_count": session.message_count,
            "insights": session.insights,
            "started_at": session.started_at,
            "ended_at": session.ended_at
        }
