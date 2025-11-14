"""
Test Interattivo - Simula l'uso dell'applicazione
"""

import asyncio
from core.database import Database
from services.llm_service import LLMService, OpenAIProvider
from services.session_manager import SessionManager
from services.todo_manager import TodoManager
from core.models import LLMRole

print("=" * 70)
print("🎓 ITIS ORIENTATION COACH - DEMO INTERATTIVA")
print("=" * 70)
print()

# Crea database e servizi
print("📦 Inizializzazione...")
db = Database("data/demo.db")
print("   ✅ Database creato")

# Simulazione LLM (senza API key)
class MockLLMProvider:
    """Provider LLM simulato per test senza API key"""

    async def chat(self, messages, tools=None):
        # Simula una risposta basata sul ruolo
        last_msg = messages[-1]["content"] if messages else ""

        if "ciao" in last_msg.lower() or "inizio" in last_msg.lower():
            return """Ciao! 👋 Sono qui per aiutarti a scoprire cosa ti appassiona davvero.
Non c'è fretta e non ci sono risposte giuste o sbagliate.

Raccontami: c'è stato un progetto o un'attività che hai fatto a scuola
che ti ha proprio preso? Qualcosa dove il tempo è volato?"""

        elif "arduino" in last_msg.lower() or "progetto" in last_msg.lower():
            return """Interessante! 🤔 Il fatto che ti sia piaciuto lavorare con Arduino
mi dice che ti piacciono le cose pratiche, dove vedi subito il risultato.

Raccontami di più: quando hai risolto quel problema, come ti sei sentito?
E preferivi lavorare da solo o con i compagni?"""

        else:
            return """Capisco. È normale esplorare diverse possibilità.
Quello che mi dici è molto utile per capire meglio cosa ti interessa.

C'è altro che vorresti raccontarmi sui tuoi interessi o esperienze?"""

# Usa mock provider invece di vero LLM
mock_provider = MockLLMProvider()
llm_service = LLMService(mock_provider)
print("   ✅ LLM Service (modalità DEMO)")

session_manager = SessionManager(db, llm_service)
todo_manager = TodoManager(db)
print("   ✅ Servizi pronti")
print()

# STEP 1: Crea studente
print("=" * 70)
print("STEP 1: ONBOARDING - Creazione Profilo")
print("=" * 70)
print()

pseudonym = "DemoStudent_2024"
student = db.create_student(pseudonym)
print(f"✅ Studente creato: {student.pseudonym}")
print(f"   ID: {student.id[:16]}...")
print(f"   Status: {student.status.value}")
print(f"   Creato: {student.created_at.strftime('%d/%m/%Y %H:%M')}")
print()

# STEP 2: Prima sessione
print("=" * 70)
print("STEP 2: PRIMA SESSIONE - Esploratore Curioso")
print("=" * 70)
print()

session = session_manager.create_session(student.id)
print(f"✅ Sessione creata")
print(f"   Ruolo LLM: {session.role.value.replace('_', ' ').title()}")
print(f"   Sessione ID: {session.id[:16]}...")
print()

# Simulazione conversazione
print("🗨️  CONVERSAZIONE SIMULATA:")
print("-" * 70)

messages_demo = [
    ("Studente", "Ciao, sono pronto a iniziare!"),
    ("Studente", "Mi è piaciuto molto un progetto con Arduino dove dovevamo creare un sistema di allarme"),
    ("Studente", "Mi sono sentito soddisfatto quando ha funzionato! Lavoravo in gruppo")
]

for i, (speaker, msg) in enumerate(messages_demo, 1):
    print()
    print(f"[{speaker}]: {msg}")

    # Ottieni risposta LLM
    response = asyncio.run(
        session_manager.get_llm_response(session.id, msg)
    )

    print()
    print(f"[Assistente]: {response}")
    print("-" * 70)

print()

# STEP 3: Todo proposti
print("=" * 70)
print("STEP 3: TODO LIST - Attività Proposte")
print("=" * 70)
print()

# Crea alcuni todo
todo1 = todo_manager.create_todo(
    student_id=student.id,
    content="Esplorare altri progetti pratici che ti sono piaciuti",
    categoria="esplorazione",
    priorita="media",
    session_id=session.id,
    motivazione="Per capire meglio quali aspetti tecnici ti interessano"
)

todo2 = todo_manager.create_todo(
    student_id=student.id,
    content="Parlare con un ex-studente che lavora con l'elettronica",
    categoria="esplorazione",
    priorita="alta",
    session_id=session.id,
    motivazione="Per avere un'idea concreta di cosa significa lavorare in questo campo"
)

print("📝 Todo proposti durante la sessione:")
print()

for i, todo in enumerate([todo1, todo2], 1):
    print(f"{i}. {todo.content}")
    print(f"   Categoria: {todo.categoria} | Priorità: {todo.priorita}")
    print(f"   Status: {todo.status}")
    print(f"   💭 {todo.motivazione}")
    print()

# Simula accettazione
print("✅ Studente accetta le proposte...")
todo_manager.accept_todo(todo1.id)
todo_manager.accept_todo(todo2.id)
print("   Entrambi i todo sono stati accettati!")
print()

# STEP 4: Statistiche
print("=" * 70)
print("STEP 4: PROFILO E STATISTICHE")
print("=" * 70)
print()

session_count = session_manager.count_sessions(student.id)
pending_todos = len(todo_manager.get_pending_todos(student.id))
completed_todos = todo_manager.count_completed(student.id)

print(f"👤 Profilo: {student.pseudonym}")
print(f"   📊 Sessioni completate: {session_count}")
print(f"   ✅ Todo da fare: {pending_todos}")
print(f"   ✓  Todo completati: {completed_todos}")
print(f"   🎯 Fase attuale: {student.status.value}")
print()

# STEP 5: Dati salvati
print("=" * 70)
print("STEP 5: VERIFICA DATABASE")
print("=" * 70)
print()

messages = session_manager.get_messages(session.id)
print(f"💬 Messaggi salvati nel database: {len(messages)}")
for msg in messages[:3]:
    preview = msg.content[:60] + "..." if len(msg.content) > 60 else msg.content
    print(f"   [{msg.role.value}]: {preview}")

print()
print(f"📁 Database salvato in: data/demo.db")
print(f"   Dimensione: {db.conn.execute('SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()').fetchone()[0]} bytes")
print()

# STEP 6: Chiusura sessione
print("=" * 70)
print("STEP 6: CHIUSURA SESSIONE")
print("=" * 70)
print()

insights = [
    "Interesse per progetti pratici hands-on",
    "Preferenza per lavoro di gruppo",
    "Soddisfazione nel vedere risultati concreti"
]

session_manager.end_session(session.id, insights)
print("✅ Sessione terminata con successo")
print("   📝 Insights registrati:")
for insight in insights:
    print(f"      - {insight}")
print()

# Cleanup
db.close()
print("=" * 70)
print("✅ DEMO COMPLETATA CON SUCCESSO!")
print("=" * 70)
print()
print("📋 Riepilogo:")
print("   ✅ Database creato e popolato")
print("   ✅ Studente registrato")
print("   ✅ Sessione conversazionale simulata")
print("   ✅ Todo list gestita")
print("   ✅ Dati persistiti nel database")
print()
print("🎯 L'applicazione funziona correttamente!")
print("   Per usarla con LLM reale, serve API key OpenAI in .env")
print()
