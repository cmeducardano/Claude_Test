# Architettura Tecnica - ITIS Orientation Coach

## Executive Summary

Applicazione desktop standalone cross-platform con focus su privacy, performance e user experience. Storage completamente locale, integrazione LLM via API, interfaccia moderna e accessibile.

---

## Stack Tecnologico Proposto

### Opzione A: Tauri + React (Raccomandato)

**Vantaggi:**
- Dimensioni ridotte (bundle ~3-5 MB vs Electron ~50-100 MB)
- Performance native (Rust backend)
- Sicurezza integrata
- Cross-platform (Windows, macOS, Linux)
- Consumo memoria ridotto
- Community attiva e in crescita

**Stack completo:**
```
Frontend:
- React 18+ (UI framework)
- TypeScript (type safety)
- Tailwind CSS (styling, mobile-first)
- Zustand (state management leggero)
- React Query (data fetching/caching)
- Framer Motion (animazioni fluide)

Backend (Rust):
- Tauri 2.0 (framework desktop)
- SQLite (database locale)
- Tokio (async runtime)
- Reqwest (HTTP client per API calls)

LLM Integration:
- OpenAI SDK / Anthropic SDK
- Fallback su Ollama locale (opzionale)

Build & Dev:
- Vite (bundler veloce)
- Vitest (testing)
- ESLint + Prettier
```

### Opzione B: Electron + React (Alternativa)

**Vantaggi:**
- Maturità e stabilità comprovata
- Documentazione estesa
- Ecosystem ricchissimo
- Familiarità per molti developer

**Svantaggi:**
- Dimensioni bundle maggiori
- Consumo RAM più alto
- Performance inferiore a Tauri

**Stack completo:**
```
Frontend: [stesso di Opzione A]

Backend (Node.js):
- Electron 28+
- Better-SQLite3 (database)
- Axios (HTTP client)

Electron specifics:
- electron-builder (packaging)
- electron-updater (auto-update)
```

**Raccomandazione: Opzione A (Tauri)** per dimensioni ridotte, performance e sicurezza migliori.

---

## Architettura Applicazione

### Struttura a Layer

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (React Components + UI Logic)        │
├─────────────────────────────────────────┤
│         Application Layer               │
│   (Business Logic + State Management)  │
├─────────────────────────────────────────┤
│         Service Layer                   │
│  (LLM, Search, Storage, Privacy)       │
├─────────────────────────────────────────┤
│         Data Layer                      │
│      (SQLite + File System)            │
└─────────────────────────────────────────┘
```

### Componenti Principali

#### 1. LLM Service
```typescript
interface LLMService {
  // Gestione conversazioni con ruoli dinamici
  chat(messages: Message[], role: LLMRole): Promise<Response>

  // Streaming per UX migliore
  streamChat(messages: Message[], role: LLMRole): AsyncIterator<string>

  // Tool calling per ricerche
  executeToolCall(tool: Tool, params: any): Promise<any>

  // Switch provider (OpenAI, Anthropic, Ollama)
  setProvider(provider: LLMProvider): void
}
```

**Providers supportati:**
- **OpenAI GPT-4** (primario)
- **Anthropic Claude** (alternativo)
- **Ollama locale** (privacy massima, no internet)

#### 2. Session Manager
```typescript
interface SessionManager {
  // CRUD sessioni
  createSession(): Session
  saveSession(session: Session): void
  loadSession(id: string): Session

  // Gestione continuità
  getLastSession(): Session | null
  resumeSession(id: string): void

  // Timing e limiti
  shouldCloseSession(session: Session): boolean // dopo 15-20 min
}
```

#### 3. Search Service
```typescript
interface SearchService {
  // Ricerche multiple con cache
  webSearch(query: string): Promise<SearchResult[]>
  universityAPI(query: UniversityQuery): Promise<UniversityData>
  jobMarketAPI(query: JobQuery): Promise<JobMarketData>
  itsDatabase(query: ITSQuery): Promise<ITSData>

  // Cache management
  getCached(key: string): CachedData | null
  setCached(key: string, data: any, ttl: number): void
  invalidateCache(pattern: string): void
}
```

**Cache Strategy:**
- Scadenze: 6 ore TTL
- Programmi università: 30 giorni TTL
- Dati mercato lavoro: 7 giorni TTL
- LRU eviction quando > 100MB

#### 4. Todo Manager
```typescript
interface TodoManager {
  // CRUD todos
  createTodo(todo: TodoItem): void
  updateTodo(id: string, updates: Partial<TodoItem>): void
  completeTodo(id: string): void
  deleteTodo(id: string): void

  // Query e filtri
  getPendingTodos(): TodoItem[]
  getTodosByPriority(priority: Priority): TodoItem[]
  getTodosByDeadline(beforeDate: Date): TodoItem[]

  // Engagement
  celebrateCompletion(todo: TodoItem): Celebration
}
```

#### 5. Document Service
```typescript
interface DocumentService {
  // Documento evolutivo studente
  getStudentDocument(studentId: string): StudentDocument
  updateDocument(updates: DocumentUpdate): void

  // Export
  exportJSON(studentId: string): string
  exportMarkdown(studentId: string): string
  exportPDF(studentId: string): Buffer

  // Insights
  extractInsights(document: StudentDocument): Insight[]
  detectPatterns(document: StudentDocument): Pattern[]
}
```

#### 6. Privacy Filter
```typescript
interface PrivacyFilter {
  // Rilevamento dati sensibili
  detectSensitiveData(text: string): SensitiveDataMatch[]

  // Sanitizzazione
  sanitizeForStorage(text: string): string
  sanitizeForSearch(query: string): string

  // Validazione
  isDataCompliant(data: any): boolean

  // Redirect costruttivi
  getSafeRedirect(sensitiveType: string): string
}
```

---

## Database Schema (SQLite)

### Tabelle Principali

```sql
-- Studenti (minimal data)
CREATE TABLE students (
  id TEXT PRIMARY KEY,
  pseudonym TEXT UNIQUE NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_session_at DATETIME,
  status TEXT CHECK(status IN ('exploring', 'deciding', 'preparing', 'completed')),
  settings_json TEXT -- preferenze UI, provider LLM, etc.
);

-- Sessioni conversazionali
CREATE TABLE sessions (
  id TEXT PRIMARY KEY,
  student_id TEXT NOT NULL,
  role TEXT NOT NULL, -- esploratore, analista, mentore, coach, filosofo
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  ended_at DATETIME,
  duration_seconds INTEGER,
  message_count INTEGER DEFAULT 0,
  searches_count INTEGER DEFAULT 0,
  insights_json TEXT, -- insights emersi
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Messaggi (per continuità conversazioni)
CREATE TABLE messages (
  id TEXT PRIMARY KEY,
  session_id TEXT NOT NULL,
  role TEXT CHECK(role IN ('user', 'assistant', 'system')),
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  metadata_json TEXT, -- tool calls, citations, etc.
  FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

-- Todo items
CREATE TABLE todos (
  id TEXT PRIMARY KEY,
  student_id TEXT NOT NULL,
  session_id TEXT, -- sessione da cui è emersa
  categoria TEXT CHECK(categoria IN ('esplorazione', 'preparazione', 'riflessione', 'admin')),
  priorita TEXT CHECK(priorita IN ('alta', 'media', 'bassa', 'nice-to-have')),
  status TEXT CHECK(status IN ('proposto', 'accettato', 'in_progress', 'completato', 'scartato')),
  deadline_type TEXT CHECK(deadline_type IN ('flessibile', 'rigida')),
  deadline_date DATE,
  content TEXT NOT NULL,
  motivazione TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE SET NULL
);

-- Profilo studente (evolve nel tempo)
CREATE TABLE student_profile (
  student_id TEXT PRIMARY KEY,
  interests_json TEXT, -- {"problem_solving": 3, "team_work": 2, ...}
  skills_json TEXT,
  values_json TEXT,
  constraints_json TEXT, -- logistiche, geografiche, etc.
  learning_style TEXT,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Percorsi esplorati
CREATE TABLE paths (
  id TEXT PRIMARY KEY,
  student_id TEXT NOT NULL,
  type TEXT, -- university, its, certification, work, other
  name TEXT NOT NULL,
  description TEXT,
  match_percentage INTEGER, -- 0-100
  pros_json TEXT, -- personalizzati per studente
  cons_json TEXT,
  status TEXT CHECK(status IN ('explored', 'shortlisted', 'chosen', 'discarded')),
  explored_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Cache ricerche
CREATE TABLE search_cache (
  key TEXT PRIMARY KEY,
  type TEXT, -- web, university, job_market, its
  query TEXT NOT NULL,
  result_json TEXT NOT NULL,
  cached_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL,
  hit_count INTEGER DEFAULT 0
);

-- Insights e patterns
CREATE TABLE insights (
  id TEXT PRIMARY KEY,
  student_id TEXT NOT NULL,
  type TEXT, -- pattern, contradiction, evolution
  content TEXT NOT NULL,
  confidence REAL, -- 0.0-1.0
  detected_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  sessions_json TEXT, -- sessioni da cui è emerso
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Audit log (per debug e miglioramento)
CREATE TABLE audit_log (
  id TEXT PRIMARY KEY,
  event_type TEXT NOT NULL,
  event_data_json TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Indici per performance
CREATE INDEX idx_sessions_student ON sessions(student_id, started_at DESC);
CREATE INDEX idx_messages_session ON messages(session_id, created_at);
CREATE INDEX idx_todos_student_status ON todos(student_id, status);
CREATE INDEX idx_cache_expires ON search_cache(expires_at);
CREATE INDEX idx_paths_student ON paths(student_id, status);
```

---

## File System Structure

```
user-data/
├── database/
│   └── orientation.db          # SQLite database
├── exports/
│   ├── {student_id}/
│   │   ├── progress.json
│   │   ├── progress.md
│   │   └── progress.pdf
├── cache/
│   └── search/                 # File-based cache overflow
└── logs/
    ├── app.log                 # Application logs (no sensitive data)
    └── error.log               # Error tracking
```

---

## API Integration Architecture

### LLM Provider Integration

```typescript
// Abstraction layer per switch facile tra provider
abstract class LLMProvider {
  abstract chat(request: ChatRequest): Promise<ChatResponse>
  abstract stream(request: ChatRequest): AsyncIterator<string>
  abstract supportedTools(): Tool[]
}

class OpenAIProvider extends LLMProvider {
  private client: OpenAI

  async chat(request: ChatRequest): Promise<ChatResponse> {
    const response = await this.client.chat.completions.create({
      model: "gpt-4-turbo-preview",
      messages: request.messages,
      tools: request.tools,
      temperature: 0.7
    })
    return this.transformResponse(response)
  }
}

class AnthropicProvider extends LLMProvider {
  private client: Anthropic

  async chat(request: ChatRequest): Promise<ChatResponse> {
    const response = await this.client.messages.create({
      model: "claude-3-opus-20240229",
      messages: request.messages,
      tools: request.tools,
      max_tokens: 4096
    })
    return this.transformResponse(response)
  }
}

class OllamaProvider extends LLMProvider {
  // Locale, privacy massima, no internet required
  async chat(request: ChatRequest): Promise<ChatResponse> {
    const response = await fetch('http://localhost:11434/api/chat', {
      method: 'POST',
      body: JSON.stringify({
        model: 'llama2',
        messages: request.messages
      })
    })
    return this.transformResponse(await response.json())
  }
}
```

### Search & Data APIs

```typescript
interface SearchAPI {
  name: string
  rateLimit: number
  cache: CacheConfig
  execute(query: any): Promise<any>
}

// Web Search (es. Brave Search API)
class WebSearchAPI implements SearchAPI {
  async execute(query: string): Promise<SearchResult[]> {
    const response = await fetch(`https://api.search.brave.com/res/v1/web/search?q=${encodeURIComponent(query)}`, {
      headers: { 'X-Subscription-Token': process.env.BRAVE_API_KEY }
    })
    return this.parseResults(await response.json())
  }
}

// University Data (MIUR + Universitaly)
class UniversityAPI implements SearchAPI {
  async execute(query: UniversityQuery): Promise<UniversityData> {
    // Scraping or API se disponibile
    const data = await this.fetchFromUniversitaly(query)
    return this.parseAndEnrich(data)
  }
}

// Job Market (AlmaLaurea + InfoJobs + LinkedIn)
class JobMarketAPI implements SearchAPI {
  async execute(query: JobQuery): Promise<JobMarketData> {
    const [alma, infojobs] = await Promise.all([
      this.fetchAlmaLaurea(query),
      this.fetchInfoJobs(query)
    ])
    return this.aggregate([alma, infojobs])
  }
}

// ITS Academy Database
class ITSDatabase implements SearchAPI {
  async execute(query: ITSQuery): Promise<ITSData> {
    const data = await this.fetchFromIndire(query)
    return this.parseITSData(data)
  }
}
```

---

## Security & Privacy

### Crittografia Dati

```typescript
// Encryption at rest per dati sensibili (se necessario)
class DataEncryption {
  private key: CryptoKey

  async encrypt(data: string): Promise<string> {
    const encrypted = await crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: this.generateIV() },
      this.key,
      new TextEncoder().encode(data)
    )
    return this.toBase64(encrypted)
  }

  async decrypt(encrypted: string): Promise<string> {
    const decrypted = await crypto.subtle.decrypt(
      { name: 'AES-GCM', iv: this.extractIV(encrypted) },
      this.key,
      this.fromBase64(encrypted)
    )
    return new TextDecoder().decode(decrypted)
  }
}
```

### Privacy Filters

```typescript
class PrivacyGuard {
  private sensitivePatterns = {
    health: /\b(ADHD|DSA|dislessia|diabete|depressione|ansia|terapia)\b/gi,
    financial: /\b(ISEE|reddito|€\s*\d+|stipendio genitori)\b/gi,
    personal: /\b([A-Z][a-z]+ [A-Z][a-z]+|[A-Z]{16})\b/g, // nomi completi, CF
    location: /\b(via [^,]+\d+|abito a [A-Z][a-z]+)\b/gi
  }

  detectSensitive(text: string): SensitiveMatch[] {
    const matches: SensitiveMatch[] = []

    for (const [type, pattern] of Object.entries(this.sensitivePatterns)) {
      const found = text.match(pattern)
      if (found) {
        matches.push({ type, matches: found })
      }
    }

    return matches
  }

  sanitize(text: string): string {
    let sanitized = text

    // Replace with placeholders
    for (const pattern of Object.values(this.sensitivePatterns)) {
      sanitized = sanitized.replace(pattern, '[RIMOSSO]')
    }

    return sanitized
  }

  getRedirect(type: string): string {
    const redirects = {
      health: "Capisco che hai il tuo modo di apprendere. Quali metodi funzionano meglio per te?",
      financial: "Parliamo delle opzioni che ti interessano, senza entrare nei dettagli economici specifici.",
      personal: "Non serve che mi dia dati personali specifici. Parliamo invece di...",
      location: "Non mi servono indirizzi precisi. Parliamo in termini generali di zona o regione."
    }

    return redirects[type] || "Non mi servono questi dettagli. Continuiamo con..."
  }
}
```

---

## Performance Optimization

### Caching Strategy

```typescript
class CacheManager {
  private memoryCache: Map<string, CachedItem> = new Map()
  private maxMemorySize = 100 * 1024 * 1024 // 100 MB

  async get(key: string): Promise<any | null> {
    // Check memory first
    const memCached = this.memoryCache.get(key)
    if (memCached && !this.isExpired(memCached)) {
      memCached.hitCount++
      return memCached.data
    }

    // Check SQLite
    const dbCached = await db.query(
      'SELECT * FROM search_cache WHERE key = ? AND expires_at > datetime("now")',
      [key]
    )

    if (dbCached) {
      dbCached.hit_count++
      await db.update('search_cache', { hit_count: dbCached.hit_count }, { key })

      // Promote to memory if hot
      if (dbCached.hit_count > 5) {
        this.memoryCache.set(key, dbCached)
      }

      return JSON.parse(dbCached.result_json)
    }

    return null
  }

  async set(key: string, data: any, ttl: number): Promise<void> {
    const expiresAt = new Date(Date.now() + ttl * 1000)

    // Save to DB
    await db.insert('search_cache', {
      key,
      result_json: JSON.stringify(data),
      cached_at: new Date(),
      expires_at: expiresAt
    })

    // Add to memory if space available
    if (this.getCurrentMemorySize() < this.maxMemorySize) {
      this.memoryCache.set(key, { data, expiresAt, hitCount: 0 })
    }
  }
}
```

### Lazy Loading & Code Splitting

```typescript
// React lazy loading per routes
const ChatView = lazy(() => import('./views/ChatView'))
const TodoView = lazy(() => import('./views/TodoView'))
const ProfileView = lazy(() => import('./views/ProfileView'))
const SettingsView = lazy(() => import('./views/SettingsView'))

// Route-based code splitting
function App() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        <Route path="/chat" element={<ChatView />} />
        <Route path="/todos" element={<TodoView />} />
        <Route path="/profile" element={<ProfileView />} />
        <Route path="/settings" element={<SettingsView />} />
      </Routes>
    </Suspense>
  )
}
```

---

## UI/UX Architecture

### Design System

```
Components:
├── atoms/
│   ├── Button
│   ├── Input
│   ├── Avatar
│   ├── Badge
│   └── Icon
├── molecules/
│   ├── ChatMessage
│   ├── TodoCard
│   ├── PathCard
│   └── InsightBadge
├── organisms/
│   ├── ChatConversation
│   ├── TodoList
│   ├── PathComparison
│   └── SessionSummary
└── templates/
    ├── ChatLayout
    ├── DashboardLayout
    └── OnboardingLayout
```

### Accessibility (WCAG 2.1 AA)

```typescript
// Configurazione accessibilità
const a11yConfig = {
  focusVisible: true,
  keyboardNavigation: true,
  ariaLabels: true,
  colorContrast: {
    normal: 4.5,
    large: 3
  },
  textSize: {
    min: '16px',
    scalable: true
  },
  screenReader: true,
  reducedMotion: true // rispetta prefers-reduced-motion
}
```

### Responsive Breakpoints (Mobile-First)

```css
/* Tailwind config */
theme: {
  screens: {
    'sm': '640px',   // Mobile landscape
    'md': '768px',   // Tablet
    'lg': '1024px',  // Desktop
    'xl': '1280px',  // Large desktop
  }
}
```

---

## Build & Deployment

### Tauri Configuration

```json
{
  "build": {
    "beforeBuildCommand": "npm run build",
    "beforeDevCommand": "npm run dev",
    "devPath": "http://localhost:5173",
    "distDir": "../dist"
  },
  "package": {
    "productName": "ITIS Orientation Coach",
    "version": "1.0.0"
  },
  "tauri": {
    "bundle": {
      "active": true,
      "targets": ["msi", "deb", "dmg", "appimage"],
      "identifier": "com.itis.orientationcoach",
      "icon": [
        "icons/32x32.png",
        "icons/128x128.png",
        "icons/icon.icns",
        "icons/icon.ico"
      ]
    },
    "security": {
      "csp": "default-src 'self'; connect-src 'self' https://api.openai.com https://api.anthropic.com"
    },
    "allowlist": {
      "all": false,
      "fs": {
        "readFile": true,
        "writeFile": true,
        "scope": ["$APPDATA/orientation-coach/*"]
      },
      "http": {
        "request": true,
        "scope": ["https://api.openai.com/*", "https://api.anthropic.com/*"]
      }
    }
  }
}
```

### Auto-Update Strategy

```typescript
// Tauri updater
import { checkUpdate, installUpdate } from '@tauri-apps/api/updater'

async function checkForUpdates() {
  const { shouldUpdate, manifest } = await checkUpdate()

  if (shouldUpdate) {
    await showUpdateDialog(manifest)
    await installUpdate()
    await relaunch()
  }
}
```

---

## Testing Strategy

### Test Pyramid

```
E2E Tests (10%)
├── Onboarding completo
├── Sessione conversazionale
└── Export dati

Integration Tests (30%)
├── LLM + Session Manager
├── Search + Cache
├── Todo Manager + Database
└── Privacy Filter + Storage

Unit Tests (60%)
├── Tutti i service layer
├── Privacy filters
├── Cache logic
├── Utilities
└── Hooks/Components
```

### Test Tools

```json
{
  "vitest": "Unit & Integration",
  "playwright": "E2E",
  "testing-library/react": "Component testing",
  "msw": "API mocking"
}
```

---

## Monitoring & Analytics

### Privacy-Preserving Analytics

```typescript
// Solo metriche aggregate, zero dati personali
interface AppMetrics {
  sessions: {
    total: number
    avgDuration: number
    avgMessagesPerSession: number
  }
  todos: {
    created: number
    completed: number
    acceptanceRate: number
  }
  searches: {
    total: number
    successRate: number
    cacheHitRate: number
    avgResponseTime: number
  }
  llm: {
    provider: string
    avgResponseTime: number
    errorRate: number
  }
}
```

### Error Tracking

```typescript
class ErrorLogger {
  log(error: Error, context: any) {
    // Remove any sensitive data from context
    const sanitizedContext = this.sanitize(context)

    // Log to file (local only, no external services)
    fs.appendFile('logs/error.log', JSON.stringify({
      timestamp: new Date(),
      error: error.message,
      stack: error.stack,
      context: sanitizedContext
    }))
  }
}
```

---

## Scalabilità Futura

### Multi-Tenant Architecture (Fase 3)

Se in futuro si vuole supportare più scuole:

```typescript
// Aggiungere tenant_id a tutte le tabelle
CREATE TABLE tenants (
  id TEXT PRIMARY KEY,
  school_name TEXT NOT NULL,
  settings_json TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

// Row-level security
SELECT * FROM students WHERE tenant_id = current_tenant_id();
```

### API Backend (Fase 3)

Se serve sincronizzazione cloud:

```typescript
// REST API opzionale
interface OrientationAPI {
  POST /api/sync/upload    // Upload dati cifrati
  GET  /api/sync/download  // Download dati cifrati
  POST /api/analytics      // Aggregate analytics (anonime)
}
```

---

## Stima Risorse

### Sviluppo

| Fase | Durata | Componenti |
|------|--------|------------|
| Setup & Core | 3 settimane | Tauri setup, DB schema, base UI |
| LLM Integration | 2 settimane | Provider abstraction, tool calling |
| Session Management | 2 settimane | Roles, flow, continuità |
| Todo System | 1 settimana | CRUD, engagement mechanics |
| Search Integration | 2 settimane | API clients, cache, privacy |
| Privacy & Security | 1 settimana | Filters, encryption, compliance |
| UI/UX Polish | 2 settimane | Design system, accessibility |
| Testing | 2 settimane | Unit, integration, E2E |
| **TOTALE MVP** | **15 settimane** | ~3.5 mesi |

### Costi Operativi (per studente)

```
LLM API costs:
- OpenAI GPT-4: ~$0.10 per sessione (15-20 messaggi)
- Anthropic Claude: ~$0.08 per sessione
- Ollama locale: $0 (ma richiede GPU locale)

Totale per studente (10 sessioni): ~$1
Totale per 1000 studenti: ~$1000/anno

Search API:
- Brave Search: $5/1000 ricerche
- Stima: ~2 ricerche/sessione = $0.01/studente
- Totale 1000 studenti: ~$100/anno

TOTALE ANNUO (1000 studenti): ~$1100
```

---

## Decisioni Architetturali Chiave

### ADR-001: Tauri vs Electron

**Decisione**: Usare Tauri
**Motivazione**:
- Bundle 10x più piccolo (critico per distribuzione a studenti)
- Performance migliori (importante per UX fluida)
- Sicurezza nativa (privacy-first requirement)
- Consumo risorse ridotto (laptop studenti potrebbero non essere potenti)

**Trade-off**: Ecosystem meno maturo, ma compensato da benefici

### ADR-002: SQLite vs File JSON

**Decisione**: SQLite
**Motivazione**:
- Query complesse (filtri todo, ricerche)
- Performance migliori con crescita dati
- Integrità relazionale (sessions → messages)
- Backup/export più affidabili

**Trade-off**: Complessità setup, ma gestibile con librerie

### ADR-003: Multi-Provider LLM

**Decisione**: Abstraction layer con 3 provider
**Motivazione**:
- Resilienza (fallback se un provider down)
- Flessibilità costi (switch basato su budget)
- Privacy opzione (Ollama locale per chi vuole)

**Trade-off**: Complessità implementazione, ma valore alto

### ADR-004: Local-First Architecture

**Decisione**: Dati 100% locali, no cloud
**Motivazione**:
- Privacy requirement assoluto
- GDPR compliance by design
- Funziona offline
- Ownership totale studente

**Trade-off**: No sync multi-device, ma accettabile per use case

---

## Next Steps

1. **Setup progetto** con Tauri + React + TypeScript
2. **Implementare database schema** SQLite
3. **Creare LLM abstraction layer** con OpenAI provider
4. **Sviluppare Session Manager** base
5. **Build UI chat interface** MVP
6. **Iterare** con testing e feedback

---

*Documento versione 1.0 - Novembre 2024*
*Autore: Architettura Tecnica ITIS Orientation Coach*
