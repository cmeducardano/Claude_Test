"""
Basic functionality tests
"""

from core.database import Database
from core.models import LLMRole, StudentStatus
from services.todo_manager import TodoManager

print("🧪 Testing ITIS Orientation Coach - Basic Functionality\n")

# Test 1: Database initialization
print("1️⃣ Testing database initialization...")
try:
    db = Database("data/test.db")
    print("   ✅ Database created successfully")
except Exception as e:
    print(f"   ❌ Database error: {e}")
    exit(1)

# Test 2: Create student
print("\n2️⃣ Testing student creation...")
try:
    student = db.create_student("TestStudent123")
    print(f"   ✅ Student created: {student.pseudonym} (ID: {student.id[:8]}...)")
    print(f"   ✅ Status: {student.status.value}")
except Exception as e:
    print(f"   ❌ Student creation error: {e}")
    exit(1)

# Test 3: Create session
print("\n3️⃣ Testing session creation...")
try:
    session = db.create_session(student.id, LLMRole.ESPLORATORE)
    print(f"   ✅ Session created with role: {session.role.value}")
    print(f"   ✅ Session ID: {session.id[:8]}...")
except Exception as e:
    print(f"   ❌ Session creation error: {e}")
    exit(1)

# Test 4: Add messages
print("\n4️⃣ Testing message storage...")
try:
    from core.models import MessageRole

    msg1 = db.add_message(session.id, MessageRole.USER, "Ciao, sono pronto a iniziare!")
    print(f"   ✅ User message stored")

    msg2 = db.add_message(session.id, MessageRole.ASSISTANT, "Ciao! Benvenuto nel percorso di orientamento!")
    print(f"   ✅ Assistant message stored")

    # Retrieve messages
    messages = db.get_messages(session.id)
    print(f"   ✅ Retrieved {len(messages)} messages")
except Exception as e:
    print(f"   ❌ Message storage error: {e}")
    exit(1)

# Test 5: Todo management
print("\n5️⃣ Testing todo management...")
try:
    todo_manager = TodoManager(db)

    todo = todo_manager.create_todo(
        student_id=student.id,
        content="Partecipare all'open day del Polimi",
        categoria="esplorazione",
        priorita="alta",
        session_id=session.id,
        motivazione="Voglio capire meglio l'ambiente universitario"
    )
    print(f"   ✅ Todo created: {todo.content[:50]}...")
    print(f"   ✅ Priority: {todo.priorita}, Status: {todo.status}")

    # Accept todo
    todo_manager.accept_todo(todo.id)
    print(f"   ✅ Todo accepted")

    # Complete todo
    todo_manager.complete_todo(todo.id)
    print(f"   ✅ Todo completed")

    # Get stats
    completed = todo_manager.count_completed(student.id)
    print(f"   ✅ Completed todos: {completed}")
except Exception as e:
    print(f"   ❌ Todo management error: {e}")
    exit(1)

# Test 6: Session statistics
print("\n6️⃣ Testing session statistics...")
try:
    session_count = db.count_sessions(student.id)
    print(f"   ✅ Total sessions: {session_count}")

    last_session = db.get_last_session(student.id)
    print(f"   ✅ Last session role: {last_session.role.value}")
except Exception as e:
    print(f"   ❌ Statistics error: {e}")
    exit(1)

# Test 7: Data retrieval
print("\n7️⃣ Testing data retrieval...")
try:
    retrieved_student = db.get_student(student.id)
    print(f"   ✅ Student retrieved: {retrieved_student.pseudonym}")

    retrieved_by_name = db.get_student_by_pseudonym("TestStudent123")
    print(f"   ✅ Student found by pseudonym")

    todos = todo_manager.get_todos(student.id)
    print(f"   ✅ Retrieved {len(todos)} todos")
except Exception as e:
    print(f"   ❌ Data retrieval error: {e}")
    exit(1)

# Test 8: Models validation
print("\n8️⃣ Testing Pydantic models validation...")
try:
    from core.models import Student, Session, TodoItem
    from datetime import datetime

    # This should work
    valid_student = Student(
        id="test-123",
        pseudonym="ValidStudent",
        created_at=datetime.now(),
        status=StudentStatus.EXPLORING
    )
    print(f"   ✅ Valid student model created")

    # Test enum validation
    assert valid_student.status == StudentStatus.EXPLORING
    print(f"   ✅ Enum validation working")

except Exception as e:
    print(f"   ❌ Model validation error: {e}")
    exit(1)

# Cleanup
print("\n🧹 Cleaning up test database...")
db.close()
import os
os.remove("data/test.db")
print("   ✅ Test database removed")

print("\n" + "="*60)
print("✅ ALL TESTS PASSED! Core functionality is working correctly.")
print("="*60)
print("\n📝 Next step: Configure .env with your OpenAI API key to test LLM integration")
