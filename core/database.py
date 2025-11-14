"""
SQLite database management
"""

import sqlite3
from pathlib import Path
from typing import Optional, List, Dict, Any
import json
from datetime import datetime
import uuid

from core.models import (
    Student, Session, Message, TodoItem, StudentProfile,
    StudentStatus, LLMRole, MessageRole
)
# Note: We import Path from pathlib above, not from core.models
# core.models.Path is the Pydantic model for educational paths


class Database:
    """Gestione database SQLite"""

    def __init__(self, db_path: str = "data/orientation.db"):
        self.db_path = Path(db_path)
        self.db_path.parent.mkdir(exist_ok=True, parents=True)
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

        # Percorsi
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

        # Indici
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sessions_student ON sessions(student_id, started_at DESC)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_messages_session ON messages(session_id, created_at)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_todos_student_status ON todos(student_id, status)")

        self.conn.commit()

    # ===== STUDENT OPERATIONS =====

    def create_student(self, pseudonym: str) -> Student:
        """Crea nuovo studente"""
        student_id = str(uuid.uuid4())
        now = datetime.now()

        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO students (id, pseudonym, created_at, status, settings_json)
               VALUES (?, ?, ?, ?, ?)""",
            (student_id, pseudonym, now, StudentStatus.EXPLORING.value, json.dumps({}))
        )
        self.conn.commit()

        return Student(
            id=student_id,
            pseudonym=pseudonym,
            created_at=now,
            status=StudentStatus.EXPLORING,
            settings={}
        )

    def get_student(self, student_id: str) -> Optional[Student]:
        """Recupera studente per ID"""
        cursor = self.conn.cursor()
        row = cursor.execute("SELECT * FROM students WHERE id = ?", (student_id,)).fetchone()

        if not row:
            return None

        return Student(
            id=row["id"],
            pseudonym=row["pseudonym"],
            created_at=datetime.fromisoformat(row["created_at"]),
            last_session_at=datetime.fromisoformat(row["last_session_at"]) if row["last_session_at"] else None,
            status=StudentStatus(row["status"]),
            settings=json.loads(row["settings_json"]) if row["settings_json"] else {}
        )

    def get_student_by_pseudonym(self, pseudonym: str) -> Optional[Student]:
        """Recupera studente per pseudonimo"""
        cursor = self.conn.cursor()
        row = cursor.execute("SELECT * FROM students WHERE pseudonym = ?", (pseudonym,)).fetchone()

        if not row:
            return None

        return Student(
            id=row["id"],
            pseudonym=row["pseudonym"],
            created_at=datetime.fromisoformat(row["created_at"]),
            last_session_at=datetime.fromisoformat(row["last_session_at"]) if row["last_session_at"] else None,
            status=StudentStatus(row["status"]),
            settings=json.loads(row["settings_json"]) if row["settings_json"] else {}
        )

    # ===== SESSION OPERATIONS =====

    def create_session(self, student_id: str, role: LLMRole) -> Session:
        """Crea nuova sessione"""
        session_id = str(uuid.uuid4())
        now = datetime.now()

        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO sessions (id, student_id, role, started_at, insights_json)
               VALUES (?, ?, ?, ?, ?)""",
            (session_id, student_id, role.value, now, json.dumps([]))
        )

        # Aggiorna last_session_at dello studente
        cursor.execute(
            "UPDATE students SET last_session_at = ? WHERE id = ?",
            (now, student_id)
        )

        self.conn.commit()

        return Session(
            id=session_id,
            student_id=student_id,
            role=role,
            started_at=now,
            insights=[]
        )

    def get_session(self, session_id: str) -> Optional[Session]:
        """Recupera sessione per ID"""
        cursor = self.conn.cursor()
        row = cursor.execute("SELECT * FROM sessions WHERE id = ?", (session_id,)).fetchone()

        if not row:
            return None

        return Session(
            id=row["id"],
            student_id=row["student_id"],
            role=LLMRole(row["role"]),
            started_at=datetime.fromisoformat(row["started_at"]),
            ended_at=datetime.fromisoformat(row["ended_at"]) if row["ended_at"] else None,
            duration_seconds=row["duration_seconds"],
            message_count=row["message_count"],
            searches_count=row["searches_count"],
            insights=json.loads(row["insights_json"]) if row["insights_json"] else []
        )

    def get_last_session(self, student_id: str) -> Optional[Session]:
        """Recupera ultima sessione dello studente"""
        cursor = self.conn.cursor()
        row = cursor.execute(
            "SELECT * FROM sessions WHERE student_id = ? ORDER BY started_at DESC LIMIT 1",
            (student_id,)
        ).fetchone()

        if not row:
            return None

        return Session(
            id=row["id"],
            student_id=row["student_id"],
            role=LLMRole(row["role"]),
            started_at=datetime.fromisoformat(row["started_at"]),
            ended_at=datetime.fromisoformat(row["ended_at"]) if row["ended_at"] else None,
            duration_seconds=row["duration_seconds"],
            message_count=row["message_count"],
            searches_count=row["searches_count"],
            insights=json.loads(row["insights_json"]) if row["insights_json"] else []
        )

    def count_sessions(self, student_id: str) -> int:
        """Conta sessioni dello studente"""
        cursor = self.conn.cursor()
        count = cursor.execute(
            "SELECT COUNT(*) FROM sessions WHERE student_id = ?",
            (student_id,)
        ).fetchone()[0]
        return count

    def end_session(self, session_id: str, insights: List[str] = None):
        """Termina sessione"""
        now = datetime.now()
        session = self.get_session(session_id)

        if not session:
            return

        duration = int((now - session.started_at).total_seconds())

        cursor = self.conn.cursor()
        cursor.execute(
            """UPDATE sessions SET ended_at = ?, duration_seconds = ?, insights_json = ?
               WHERE id = ?""",
            (now, duration, json.dumps(insights or []), session_id)
        )
        self.conn.commit()

    # ===== MESSAGE OPERATIONS =====

    def add_message(self, session_id: str, role: MessageRole, content: str, metadata: Dict = None) -> Message:
        """Aggiunge messaggio a sessione"""
        message_id = str(uuid.uuid4())
        now = datetime.now()

        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO messages (id, session_id, role, content, created_at, metadata_json)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (message_id, session_id, role.value, content, now, json.dumps(metadata or {}))
        )

        # Incrementa message_count della sessione
        cursor.execute(
            "UPDATE sessions SET message_count = message_count + 1 WHERE id = ?",
            (session_id,)
        )

        self.conn.commit()

        return Message(
            id=message_id,
            session_id=session_id,
            role=role,
            content=content,
            created_at=now,
            metadata=metadata or {}
        )

    def get_messages(self, session_id: str) -> List[Message]:
        """Recupera tutti i messaggi di una sessione"""
        cursor = self.conn.cursor()
        rows = cursor.execute(
            "SELECT * FROM messages WHERE session_id = ? ORDER BY created_at",
            (session_id,)
        ).fetchall()

        return [
            Message(
                id=row["id"],
                session_id=row["session_id"],
                role=MessageRole(row["role"]),
                content=row["content"],
                created_at=datetime.fromisoformat(row["created_at"]),
                metadata=json.loads(row["metadata_json"]) if row["metadata_json"] else {}
            )
            for row in rows
        ]

    # ===== TODO OPERATIONS =====

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
        todo_id = str(uuid.uuid4())
        now = datetime.now()

        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO todos (id, student_id, session_id, categoria, priorita, status,
                                 deadline_type, deadline_date, content, motivazione, created_at)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (todo_id, student_id, session_id, categoria, priorita, "proposto",
             deadline_type, deadline_date, content, motivazione, now)
        )
        self.conn.commit()

        return TodoItem(
            id=todo_id,
            student_id=student_id,
            session_id=session_id,
            categoria=categoria,
            priorita=priorita,
            status="proposto",
            deadline_type=deadline_type,
            deadline_date=deadline_date,
            content=content,
            motivazione=motivazione,
            created_at=now
        )

    def get_todos(self, student_id: str, status: Optional[str] = None) -> List[TodoItem]:
        """Recupera todos dello studente"""
        cursor = self.conn.cursor()

        if status:
            rows = cursor.execute(
                "SELECT * FROM todos WHERE student_id = ? AND status = ? ORDER BY created_at DESC",
                (student_id, status)
            ).fetchall()
        else:
            rows = cursor.execute(
                "SELECT * FROM todos WHERE student_id = ? ORDER BY created_at DESC",
                (student_id,)
            ).fetchall()

        return [
            TodoItem(
                id=row["id"],
                student_id=row["student_id"],
                session_id=row["session_id"],
                categoria=row["categoria"],
                priorita=row["priorita"],
                status=row["status"],
                deadline_type=row["deadline_type"],
                deadline_date=datetime.fromisoformat(row["deadline_date"]) if row["deadline_date"] else None,
                content=row["content"],
                motivazione=row["motivazione"],
                created_at=datetime.fromisoformat(row["created_at"]),
                completed_at=datetime.fromisoformat(row["completed_at"]) if row["completed_at"] else None
            )
            for row in rows
        ]

    def update_todo_status(self, todo_id: str, status: str):
        """Aggiorna status di un todo"""
        cursor = self.conn.cursor()

        if status == "completato":
            cursor.execute(
                "UPDATE todos SET status = ?, completed_at = ? WHERE id = ?",
                (status, datetime.now(), todo_id)
            )
        else:
            cursor.execute(
                "UPDATE todos SET status = ? WHERE id = ?",
                (status, todo_id)
            )

        self.conn.commit()

    def count_completed_todos(self, student_id: str) -> int:
        """Conta todos completati"""
        cursor = self.conn.cursor()
        count = cursor.execute(
            "SELECT COUNT(*) FROM todos WHERE student_id = ? AND status = 'completato'",
            (student_id,)
        ).fetchone()[0]
        return count

    def close(self):
        """Chiude connessione database"""
        self.conn.close()
