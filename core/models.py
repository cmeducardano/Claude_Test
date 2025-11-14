"""
Pydantic models for type safety and validation
"""

from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum


class StudentStatus(str, Enum):
    """Status dello studente nel percorso"""
    EXPLORING = "exploring"
    DECIDING = "deciding"
    PREPARING = "preparing"
    COMPLETED = "completed"


class LLMRole(str, Enum):
    """Ruoli LLM disponibili"""
    ESPLORATORE = "esploratore_curioso"
    ANALISTA = "analista_empatico"
    MENTORE = "mentore_pragmatico"
    COACH = "coach_operativo"
    FILOSOFO = "filosofo_riflessivo"


class MessageRole(str, Enum):
    """Ruoli messaggi conversazione"""
    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"


class Student(BaseModel):
    """Modello studente"""
    id: str
    pseudonym: str
    created_at: datetime
    last_session_at: Optional[datetime] = None
    status: StudentStatus = StudentStatus.EXPLORING
    settings: Dict[str, Any] = Field(default_factory=dict)


class Message(BaseModel):
    """Modello messaggio"""
    id: str
    session_id: str
    role: MessageRole
    content: str
    created_at: datetime
    metadata: Dict[str, Any] = Field(default_factory=dict)


class Session(BaseModel):
    """Modello sessione conversazionale"""
    id: str
    student_id: str
    role: LLMRole
    started_at: datetime
    ended_at: Optional[datetime] = None
    duration_seconds: Optional[int] = None
    message_count: int = 0
    searches_count: int = 0
    insights: List[str] = Field(default_factory=list)


class TodoItem(BaseModel):
    """Modello todo item"""
    id: str
    student_id: str
    session_id: Optional[str] = None
    categoria: str  # esplorazione, preparazione, riflessione, admin
    priorita: str  # alta, media, bassa, nice-to-have
    status: str  # proposto, accettato, in_progress, completato, scartato
    deadline_type: str  # flessibile, rigida
    deadline_date: Optional[datetime] = None
    content: str
    motivazione: Optional[str] = None
    created_at: datetime
    completed_at: Optional[datetime] = None


class Path(BaseModel):
    """Modello percorso esplorato"""
    id: str
    student_id: str
    type: str  # university, its, certification, work, other
    name: str
    description: str
    match_percentage: int = 0
    pros: List[str] = Field(default_factory=list)
    cons: List[str] = Field(default_factory=list)
    status: str  # explored, shortlisted, chosen, discarded
    explored_at: datetime


class StudentProfile(BaseModel):
    """Profilo studente"""
    student_id: str
    interests: Dict[str, int] = Field(default_factory=dict)  # interesse: livello (1-3)
    skills: Dict[str, int] = Field(default_factory=dict)
    values: Dict[str, int] = Field(default_factory=dict)
    constraints: Dict[str, Any] = Field(default_factory=dict)
    learning_style: Optional[str] = None
    updated_at: datetime
