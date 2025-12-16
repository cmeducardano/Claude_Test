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

    def generate_session_recap(self, student_id: str) -> str:
        """
        Genera un recap contestualizzato delle sessioni precedenti.
        Usato all'inizio di ogni nuova sessione per garantire continuità.
        """
        # Recupera le ultime sessioni (max 3)
        sessions = self.db.get_sessions(student_id, limit=3)
        if not sessions:
            return ""

        recap_parts = []
        session_count = self.count_sessions(student_id)

        # Header del recap
        if session_count == 1:
            return ""  # Prima sessione, nessun recap
        elif session_count == 2:
            recap_parts.append("📝 **Recap della sessione precedente:**")
        else:
            recap_parts.append(f"📝 **Recap delle ultime {min(len(sessions), 3)} sessioni:**")

        # Analizza ogni sessione precedente
        for i, session in enumerate(sessions[:3]):
            if session.insights:
                insights_str = "; ".join(session.insights[:3])  # Max 3 insight per sessione
                recap_parts.append(f"- Sessione {session_count - i - 1}: {insights_str}")

        # Aggiungi contesto sulla fase attuale
        current_role = self.llm_service.determine_role(session_count, "exploring")
        role_info = self.llm_service.get_role_progress_info(current_role)

        recap_parts.append("")
        recap_parts.append(f"🎯 **Fase attuale:** {role_info['fase']} - {role_info['descrizione']}")

        if role_info.get('prossimo'):
            recap_parts.append(f"➡️ **Prossimo passo:** {role_info['prossimo']}")

        return "\n".join(recap_parts)

    def get_progress_info(self, student_id: str) -> Dict:
        """
        Restituisce informazioni sul progresso dello studente.
        Utile per la UI per mostrare barra di avanzamento e controlli.
        """
        session_count = self.count_sessions(student_id)
        student = self.db.get_student(student_id)

        current_role = self.llm_service.determine_role(
            session_count,
            student.status.value if student else "exploring"
        )
        role_info = self.llm_service.get_role_progress_info(current_role)

        # Calcola se può tornare indietro
        can_go_back = session_count > 1

        # Calcola ruolo precedente (per "torna indietro")
        previous_role = None
        if session_count > 1:
            previous_role = self.llm_service.determine_role(
                session_count - 1,
                student.status.value if student else "exploring"
            )

        return {
            "session_count": session_count,
            "current_role": current_role,
            "role_info": role_info,
            "can_go_back": can_go_back,
            "previous_role": previous_role,
            "can_request_filosofo": True  # Sempre disponibile
        }

    def create_session_with_role_override(
        self,
        student_id: str,
        role_override: Optional[LLMRole] = None,
        go_back: bool = False
    ) -> Session:
        """
        Crea una nuova sessione con possibilità di override del ruolo.

        Args:
            student_id: ID dello studente
            role_override: Forza un ruolo specifico (es. FILOSOFO)
            go_back: Se True, usa il ruolo della fase precedente

        Returns:
            La nuova sessione creata
        """
        session_count = self.count_sessions(student_id)
        student = self.db.get_student(student_id)
        student_status = student.status.value if student else "exploring"

        if role_override:
            role = role_override
        elif go_back and session_count > 1:
            # Torna al ruolo della sessione precedente
            role = self.llm_service.determine_role(
                session_count - 1,
                student_status
            )
        else:
            role = self.llm_service.determine_role(
                session_count,
                student_status
            )

        return self.db.create_session(student_id, role)
