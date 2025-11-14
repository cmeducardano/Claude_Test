# Specifiche Funzionali - ITIS Orientation Coach

## Executive Summary

### Visione del Prodotto
Applicazione di orientamento post-diploma per studenti ITIS basata su sessioni conversazionali guidate da LLM, che accompagna lo studente nella scoperta e preparazione del percorso futuro attraverso un approccio personalizzato e privacy-first.

### Obiettivi Principali
- Mappare interessi e competenze senza bias precostituiti
- Presentare tutti i percorsi possibili (tecnici e non)
- Fornire supporto concreto attraverso todo-list condivise
- Documentare il percorso evolutivo dello studente
- Garantire privacy totale e ownership delle scelte

### Target Utenti
- Studenti ITIS triennio informatica e telecomunicazioni
- Età: 17-19 anni
- Utilizzo: da classe 4° secondo quadrimestre a post-diploma

---

## Architettura Funzionale

### 1. Sistema Conversazionale LLM con Ricerca Real-Time

#### 1.1 Architettura Ruoli Dinamici

Il sistema utilizza diversi "ruoli" LLM che si alternano in base al tipo di sessione e agli obiettivi:

| Ruolo | Sessioni | Obiettivo | Caratteristiche |
|-------|----------|-----------|-----------------|
| **Esploratore Curioso** | 1-3 | Discovery iniziale | Domande aperte, zero giudizio, mappatura interessi |
| **Analista Empatico** | 4-6 | Approfondimento | Validazione competenze, identificazione valori |
| **Mentore Pragmatico** | 7-10 | Matching percorsi | Proposta concreta percorsi, gestione obiezioni |
| **Coach Operativo** | 11+ | Preparazione | Supporto esecuzione, monitoraggio progressi |
| **Filosofo Riflessivo** | Speciali | Crisi/riflessione | Prospettiva ampia, normalizzazione dubbi |

#### 1.2 Capacità di Ricerca Integrata

**Tools disponibili per informazioni aggiornate:**
- **Web Search**: Per scadenze, news, informazioni recenti
- **University API**: Database corsi, requisiti, test ingresso
- **Job Market API**: Statistiche occupazione, stipendi, skill richieste
- **ITS Database**: Corsi ITS, placement, aziende partner

**Trigger automatici ricerca:**
- Domande su date/scadenze ("quando iscriversi", "test ingresso")
- Richieste dati attuali ("quest'anno", "ultimi dati", "statistiche 2024/2025")
- Info mercato lavoro ("stipendi attuali", "aziende che assumono")
- Requisiti aggiornati ("punteggio minimo", "crediti necessari")

**Gestione freshness:**
- Cache intelligente con durate variabili (6h per scadenze, 30gg per programmi)
- Verifica automatica obsolescenza informazioni
- Fallback su ricerca se cache scaduta

#### 1.3 Gestione Sessioni

**Struttura Sessione Standard:**
- Durata: 15-20 minuti
- Flow: Apertura → Corpo (3-5 scambi con ricerche se necessario) → Sintesi → Salvataggio
- Continuità: Ogni sessione riprende da dove si era lasciato
- Flessibilità: Studente può sempre cambiare direzione
- Ricerche: Max 10 per sessione per mantenere fluidità

### 2. Sistema Todo-List Condivise

#### 2.1 Struttura Todo Item

```javascript
{
  categoria: "esplorazione|preparazione|riflessione|admin",
  priorità: "alta|media|bassa|nice-to-have",
  status: "proposto|accettato|in_progress|completato|scartato",
  deadline: "flessibile|rigida",
  motivazione: "emerso da sessione X perché..."
}
```

#### 2.2 Meccaniche di Engagement
- Co-creazione: Todo sempre negoziate, mai imposte
- Tracking gentile: Check-in senza pressure
- Celebrazione progressi: Riconoscimento achievement
- Flessibilità totale: Sempre possibile modificare/cancellare

### 3. Sistema Documentazione Progressiva

#### 3.1 Documento Evolutivo Studente

Il sistema mantiene un documento che evolve dopo ogni sessione contenente:
- Profilo attuale (interessi, competenze, valori)
- Percorsi esplorati con pro/contro personalizzati
- Todo-list con stati e progressi
- Timeline decisionale
- Pattern e insight emersi

#### 3.2 Formato Dual-Layer
- **JSON strutturato**: Per elaborazione sistema
- **Markdown leggibile**: Per consultazione studente

### 4. Database Percorsi Comprensivo

#### 4.1 Percorsi Tecnici
- Università STEM (Informatica, Ingegneria, etc.)
- ITS Academy (2 anni, forte componente pratica)
- Certificazioni professionali
- Inserimento lavorativo diretto

#### 4.2 Percorsi Non Tecnici
- Area economico-gestionale
- Area umanistica-sociale
- Area creativa e design
- Forze armate e sicurezza
- Anno sabbatico strutturato

**Principio chiave**: L'ITIS è preparazione valida per QUALSIASI percorso

### 5. Sistema Privacy e Compliance

#### 5.1 Dati Mai Raccolti
- Nome/cognome reale
- Dati sensibili GDPR (salute, religione, orientamento)
- Informazioni economiche dettagliate
- Indirizzi specifici

#### 5.2 Dati Raccolti (solo questi)
- Pseudonimo scelto
- Interessi di studio/lavoro
- Competenze e progetti
- Preferenze logistiche generiche
- Progress orientamento

#### 5.3 Diritti Garantiti
- Accesso completo ai propri dati
- Cancellazione immediata su richiesta
- Export dati in formato portabile
- Modifica/correzione informazioni

### 6. Sistema Ricerca e Attualizzazione Contenuti

#### 6.1 Architettura Tool Use

**Tools disponibili:**
```javascript
{
  web_search: {
    uso: "Info generali, news, scadenze",
    rate_limit: 10_per_sessione,
    cache: "6-24 ore base contenuto"
  },
  university_api: {
    uso: "Corsi, requisiti, test ingresso",
    fonte: "Database MIUR + API atenei",
    cache: "30 giorni per programmi"
  },
  job_market_api: {
    uso: "Statistiche occupazione, stipendi",
    fonti: ["AlmaLaurea", "InfoJobs", "LinkedIn"],
    cache: "7 giorni"
  },
  its_database: {
    uso: "Corsi ITS, placement, partner",
    fonte: "Database Indire + ITS Academy",
    cache: "7 giorni"
  }
}
```

#### 6.2 Trigger Automatici Ricerca

**Pattern che attivano ricerca:**
- Riferimenti temporali: "quest'anno", "prossimo", "2024/2025"
- Richieste scadenze: "quando", "entro quando", "deadline"
- Dati mercato: "stipendi attuali", "occupazione oggi"
- Requisiti: "serve", "punteggio minimo", "crediti necessari"

#### 6.3 Privacy nelle Ricerche

**Anonimizzazione query:**
- Mai includere pseudonimo/dati studente
- Generalizzare contesto ("ITIS" non nome scuola)
- Query generiche non personalizzate
- Log solo tipo ricerca, non contesto personale

#### 6.4 Gestione Risultati

**Integrazione nella conversazione:**
- Citare sempre quando si usa ricerca ("Ho verificato che...")
- Evidenziare se info contraddittorie
- Suggerire verifica diretta per info critiche
- Fallback elegante se ricerca fallisce

---

## Use Cases Dettagliati

### UC1: Onboarding Iniziale

**Attore**: Nuovo studente
**Precondizioni**: Prima volta che accede all'app
**Trigger**: Apertura applicazione

**Flow principale:**
1. Sistema presenta privacy policy semplificata
2. Studente accetta termini
3. Sistema richiede pseudonimo
4. Studente sceglie pseudonimo
5. Sistema propone prima sessione esplorativa
6. Studente accetta o rimanda

**Flow alternativo:**
- 2a. Studente richiede più info → Sistema mostra dettagli privacy
- 5a. Studente non pronto → Sistema salva profilo per dopo

**Postcondizioni**: Profilo base creato, pronto per sessioni

---

### UC2: Sessione Esplorativa (Sessioni 1-3)

**Attore**: Studente in fase discovery
**Precondizioni**: Profilo creato
**Trigger**: Avvio sessione

**Flow principale:**
1. Sistema assume ruolo "Esploratore Curioso"
2. Recap sessione precedente (se esistente)
3. Domande aperte su interessi/esperienze
   - "Racconta un progetto ITIS che ti ha preso"
   - "Quando il tempo vola senza accorgertene?"
4. Studente risponde liberamente
5. Sistema approfondisce senza giudicare
6. Dopo 15-20 min, proposta chiusura
7. Sintesi insights emersi
8. Salvataggio automatico progressi

**Elementi chiave:**
- Zero suggerimenti di percorsi
- Focus su scoperta non su decisione
- Linguaggio informale e curioso

**Output**: Prime ipotesi su interessi/valori nel documento

---

### UC3: Generazione Todo Durante Sessione

**Attore**: Studente in qualsiasi sessione
**Precondizioni**: Conversazione attiva
**Trigger**: Emerge necessità/interesse specifico

**Flow principale:**
1. Durante dialogo emerge spunto actionable
2. Sistema propone todo correlata
   ```
   "Che ne dici se aggiungiamo alla lista:
   📝 Partecipare all'open day ITS del 15 marzo?"
   ```
3. Studente valuta proposta
4. Negoziazione formulazione/deadline
5. Todo aggiunta a lista condivisa
6. Sistema collega todo a insight sessione

**Flow alternativo:**
- 3a. Studente rifiuta → Sistema: "Ok, nessun problema"
- 4a. Studente modifica → Sistema adatta proposta

**Output**: Todo condivisa e accettata nella lista

---

### UC4: Check-in Todo Inizio Sessione

**Attore**: Studente con todo pendenti
**Precondizioni**: Esistono todo da sessioni precedenti
**Trigger**: Inizio nuova sessione

**Flow principale:**
1. Sistema mostra todo prioritarie
2. Check rapido stato:
   - ✅ Completata
   - 🔄 In corso
   - 😅 Non iniziata
3. Per completate: celebrazione + estrazione learning
4. Per bloccate: esplorazione ostacoli senza giudizio
5. Aggiornamento stati nel documento
6. Proseguimento sessione normale

**Principi:**
- Mai shame per non completamento
- Sempre exit option ("parliamo d'altro")
- Focus su learning non su performance

---

### UC5: Matching Percorsi (Sessioni 7-10)

**Attore**: Studente pronto per esplorare opzioni
**Precondizioni**: Profilo consolidato da sessioni precedenti
**Trigger**: Sistema identifica readiness

**Flow principale:**
1. Sistema assume ruolo "Mentore Pragmatico"
2. Presentazione 3-5 percorsi basati su profilo
3. Per ogni percorso:
   - Realtà concreta (giornata tipo, difficoltà)
   - Collegamenti a caratteristiche emerse
   - Proposta "test di realtà" (todo verificabili)
4. Gestione obiezioni specifiche
5. Studente esprime preferenze
6. Sistema affina suggerimenti
7. Generazione todo esplorative

**Output**:
- Percorsi ranked con pro/contro personalizzati
- Todo concrete per approfondimento

---

### UC6: Gestione Crisi Decisionale

**Attore**: Studente in difficoltà/confusione
**Precondizioni**: Qualsiasi momento del percorso
**Trigger**: Sistema rileva loop/stress/blocco

**Flow principale:**
1. Sistema switcha a ruolo "Filosofo Riflessivo"
2. Normalizzazione della situazione
   ```
   "È normalissimo sentirti così.
   Facciamo un passo indietro?"
   ```
3. Opzioni offerte:
   - Pausa dal percorso
   - Riflessione più ampia
   - Parlare d'altro
4. Se studente vuole continuare:
   - Domande su valori profondi
   - Prospettiva temporale ampia
   - Focus su "non esiste scelta irreversibile"
5. Chiusura soft senza pressure

**Salvaguardie:**
- Mai forzare decisioni
- Sempre validare emozioni
- Reminder che può tornare quando vuole

---

### UC7: Visualizzazione Documento Progressivo

**Attore**: Studente
**Precondizioni**: Almeno una sessione completata
**Trigger**: Richiesta visualizzazione progressi

**Flow principale:**
1. Studente richiede "mostra i miei progressi"
2. Sistema genera vista Markdown del documento
3. Visualizzazione strutturata:
   - Profilo attuale
   - Percorsi esplorati
   - Todo list con stati
   - Timeline decisionale
4. Possibilità di export (JSON/PDF)
5. Opzione modifica/correzione dati

**Informazioni mostrate:**
```markdown
## Il Tuo Percorso - Alex_2024

### Interessi Emersi
- Problem solving in team ⭐⭐⭐
- Progetti con impatto sociale ⭐⭐

### Percorsi in Considerazione
1. ITS Cybersecurity (80% match)
2. Ingegneria Gestionale (65% match)

### Todo List
- [✓] Test orientamento online
- [→] Open day ITS (15 marzo)
- [ ] Colloquio con ex-studente
```

---

### UC8: Preparazione Specifica (Sessioni 11+)

**Attore**: Studente con percorso scelto
**Precondizioni**: Decisione presa o quasi
**Trigger**: Focus su preparazione

**Flow principale:**
1. Sistema assume ruolo "Coach Operativo"
2. Check todo preparazione esistenti
3. Generazione piano dettagliato:
   - Milestone temporali
   - Risorse di studio
   - Simulazioni/test
4. Supporto esecuzione:
   - Problem solving blocchi
   - Celebrazione progressi
   - Alternative se necessario
5. Tracking advancement
6. Aggiustamenti based on feedback

**Output**: Piano azione concreto con supporto continuo

---

### UC9: Gestione Privacy e Oversharing

**Attore**: Studente che condivide dati sensibili
**Precondizioni**: Durante qualsiasi conversazione
**Trigger**: Sistema detecta informazioni sensibili

**Flow principale:**
1. Studente condivide dato sensibile
   ```
   "Ho l'ADHD quindi studio male"
   ```
2. Sistema non salva dato specifico
3. Redirect immediato e gentile:
   ```
   "Capisco che hai il tuo modo di apprendere.
   Quali metodi funzionano meglio per te?"
   ```
4. Conversazione continua su aspetti non sensibili
5. Se insistenza: reminder privacy gentle

**Principi:**
- Mai salvare dati sensibili
- Mai riferirsi a dati sensibili in futuro
- Sempre redirect costruttivo

---

### UC10: Export e Cancellazione Dati

**Attore**: Studente
**Precondizioni**: Dati esistenti nel sistema
**Trigger**: Richiesta gestione dati

**Flow principale per Export:**
1. Studente richiede export dati
2. Sistema genera file JSON/PDF
3. Download immediato
4. Conferma completamento

**Flow principale per Cancellazione:**
1. Studente richiede cancellazione
2. Sistema chiede conferma
3. Warning "azione irreversibile"
4. Se confermato: cancellazione totale
5. Conferma avvenuta cancellazione

**Garanzie:**
- Cancellazione immediata e completa
- Nessun backup dopo cancellazione
- Export in formato standard

---

### UC11: Ricerca Informazioni Aggiornate

**Attore**: Studente in qualsiasi sessione
**Precondizioni**: Sessione attiva
**Trigger**: Domanda su informazioni che cambiano frequentemente

**Flow principale:**
1. Studente chiede info attuale (es: "Quando sono i test per il Polimi?")
2. Sistema detecta necessità ricerca real-time
3. LLM attiva tool ricerca appropriato:
   - Web search per info generali
   - University API per dati specifici atenei
4. Sistema processa e verifica risultati
5. Integrazione naturale nella risposta:
   ```
   "Ho verificato ora: i test TOLC-I sono in 3 sessioni:
   aprile, luglio e settembre 2025.
   Vuoi che aggiungiamo un reminder?"
   ```
6. Proposta todo correlata se appropriato

**Flow alternativo:**
- 4a. Ricerca senza risultati → Sistema informa e suggerisce fonti dirette
- 4b. Info contraddittorie → Sistema evidenzia incertezza e consiglia verifica

**Privacy:** Query anonimizzate, mai dati personali nelle ricerche

---

### UC12: Confronto Dati Mercato Lavoro

**Attore**: Studente in fase matching percorsi
**Precondizioni**: Valutazione percorsi attiva
**Trigger**: Domande su occupazione/stipendi

**Flow principale:**
1. Studente chiede dati occupazionali attuali
2. Sistema attiva ricerche multiple:
   - AlmaLaurea per università
   - Indire per ITS
   - Job portals per mercato real-time
3. Aggregazione e confronto dati
4. Presentazione comparativa contestualizzata:
   ```
   "Dati 2024: ITS 85% occupato in 2 mesi,
   Università 75% in 4 mesi. Ma considera anche
   che l'università offre più flessibilità futura..."
   ```
5. Discussione implicazioni per scelta studente

**Metriche raccolte:**
- Effectiveness ricerche (% successo)
- Relevance per studente
- Cache hit rate per ottimizzazione

---

### UC13: Verifica Requisiti e Scadenze

**Attore**: Studente in preparazione
**Precondizioni**: Percorsi identificati
**Trigger**: Necessità info amministrative precise

**Flow principale:**
1. Studente chiede requisiti specifici/scadenze
2. Sistema identifica fonti ufficiali necessarie
3. Ricerca mirata su:
   - Siti ufficiali università/ITS
   - Portali MIUR/Regione
   - Bandi e documenti ufficiali
4. Estrazione info verificate
5. Presentazione strutturata con:
   - Date chiave
   - Requisiti specifici
   - Link fonti ufficiali
6. Generazione todo con deadline reali

**Gestione cache:**
- Scadenze: cache 6 ore
- Requisiti: cache 30 giorni
- Force refresh su richiesta studente

---

## Requisiti Non Funzionali

### Performance
- Tempo risposta LLM: < 3 secondi (senza ricerca)
- Tempo risposta con ricerca: < 5 secondi totali
- Salvataggio sessione: real-time
- Disponibilità: 99.5% uptime
- Cache hit rate target: > 30%

### Scalabilità
- Supporto concorrente: 1000+ studenti
- Storage: 10MB per studente max
- Sessioni simultanee: illimitate
- Ricerche totali: 10K/giorno sistema

### Sicurezza
- Crittografia dati at-rest
- HTTPS per tutte le comunicazioni
- Autenticazione pseudonimo + token
- No dati sensibili in log
- Query ricerca anonimizzate

### Usabilità
- Mobile-first responsive design
- Accessibilità WCAG 2.1 AA
- Linguaggio comprensibile 17-19 anni
- Zero formazione richiesta
- Trasparenza su uso ricerche

### Privacy e Compliance
- GDPR compliant by design
- Data minimization principio
- Privacy-first architettura
- Consenso granulare e revocabile
- Anonimizzazione query ricerca

### Affidabilità Informazioni
- Citazione fonti quando da ricerca
- Verifica incrociata info critiche
- Fallback su "verifica direttamente" se incerto
- Distinzione chiara info cached vs real-time

---

## Metriche di Successo

### Metriche Quantitative
- % studenti con percorso definito entro gennaio classe 5°
- % todo completate vs proposte
- Numero medio sessioni per decisione
- Tempo medio per sessione
- Success rate ricerche (target > 90%)
- Tempo medio risposta con ricerca (< 5 sec)

### Metriche Qualitative
- Soddisfazione percorso scelto (follow-up +6 mesi)
- Riduzione ansia da scelta (self-reported)
- Qualità delle conversazioni (profondità risposte)
- Senso di ownership delle decisioni
- Percezione affidabilità informazioni fornite

### KPI Sistema
- Zero data breach
- 100% compliance privacy
- < 5% abbandono durante sessioni
- > 70% todo acceptance rate
- > 90% ricerche con risultati utili
- > 30% cache hit rate per ottimizzazione

### KPI Ricerca e Attualizzazione
- Effectiveness: % ricerche con risultati (target 91%)
- Relevance: % info utilizzate nelle risposte (target 60%)
- Performance: tempo medio ricerca < 1.5 secondi
- Freshness: % info aggiornate ultimi 30gg (target 95%)

---

## Piano di Implementazione

### Fase 1: MVP (2 mesi)
- [ ] Core LLM integration
- [ ] Sistema sessioni base
- [ ] Todo management
- [ ] Documento progressivo
- [ ] Privacy filters

### Fase 2: Enhancement (4 mesi)
- [ ] Tutti i ruoli LLM
- [ ] Database percorsi completo
- [ ] Analytics dashboard
- [ ] Export/import features

### Fase 3: Scale (6 mesi)
- [ ] Multi-tenant per più scuole
- [ ] API per integrazioni
- [ ] Mobile app nativa
- [ ] Community features

---

## Rischi e Mitigazioni

| Rischio | Probabilità | Impatto | Mitigazione |
|---------|------------|---------|-------------|
| LLM genera contenuto inappropriato | Bassa | Alto | Filtri output + test estensivi |
| Studenti non completano percorso | Media | Medio | Engagement mechanics + reminder gentili |
| Violazione privacy | Bassa | Alto | Privacy by design + audit continui |
| Bias verso percorsi tecnici | Media | Medio | Training ruoli + monitoring decisioni |
| Dipendenza da LLM provider | Alta | Medio | Abstraction layer + provider multipli |

---

## Appendici

### A. Esempi Conversazioni per Ruolo

#### Esploratore Curioso
```
Sistema: "Racconta di quella volta che hai risolto un problema che sembrava impossibile"
Studente: "Beh, c'era questo bug nel progetto Arduino..."
Sistema: "Wow, e come ti sei sentito quando l'hai risolto?"
```

#### Mentore Pragmatico con Ricerca
```
Sistema: "Basandomi su quello che mi hai detto, l'ITS potrebbe essere perfetto perché ami le cose pratiche. Ma parliamone: cosa ti spaventa dell'università?"
Studente: "5 anni sono tanti... e poi trovo lavoro?"
Sistema: [RICERCA: statistiche occupazione laureati informatica 2024]
Sistema: "Capisco. Ho verificato i dati AlmaLaurea 2024: 92% dei laureati in informatica trova lavoro entro 6 mesi, tempo medio 3 mesi. Però sai che dopo la triennale molti iniziano a lavorare part-time mentre fanno la magistrale?"
```

#### Coach con Info Aggiornate
```
Studente: "Quando devo iscrivermi al TOLC per ingegneria?"
Sistema: [RICERCA: date TOLC ingegneria 2025]
Sistema: "Ho controllato ora sul CISIA: per il 2025 ci sono sessioni TOLC-I in:
- Febbraio (early bird, meno affollato)
- Aprile-Maggio (principale)
- Luglio (recupero)
- Settembre (ultima chance)

📝 Aggiungiamo un reminder per gennaio così ti iscrivi alla sessione di febbraio?"
```

### B. Struttura Dati Completa con Search

```json
{
  "student": {
    "id": "uuid",
    "pseudonym": "string",
    "created": "date",
    "last_session": "date",
    "total_sessions": "number",
    "status": "exploring|deciding|preparing|completed"
  },
  "profile": {
    "interests": {},
    "skills": {},
    "values": {},
    "constraints": {},
    "learning_style": "string"
  },
  "paths": {
    "explored": [],
    "shortlisted": [],
    "chosen": null
  },
  "todos": [],
  "sessions": [
    {
      "id": "session_id",
      "searches_performed": [
        {
          "type": "web_search|university_api|job_market",
          "query_anonymous": "test ingresso polimi 2025",
          "results_found": true,
          "used_in_response": true
        }
      ],
      "insights": []
    }
  ],
  "insights": {
    "patterns": [],
    "contradictions": [],
    "evolution": []
  },
  "cache": {
    "university_info": {},
    "job_market_data": {},
    "deadlines": {}
  }
}
```

### C. Prompt Template con Search Integration

```
Tu sei un assistente all'orientamento per studenti ITIS ultimo anno.

PRINCIPI:
1. Mai giudicare scelte
2. Ogni percorso è valido
3. Privacy sempre protetta
4. Ownership allo studente

RUOLO ATTUALE: [INSERIRE]
OBIETTIVO SESSIONE: [INSERIRE]

CAPACITÀ RICERCA:
Puoi cercare informazioni aggiornate quando necessario usando:
- web_search: per info generali, news, scadenze
- university_api: per corsi, requisiti, test
- job_market_api: per dati occupazione, stipendi
- its_database: per info su ITS Academy

QUANDO CERCARE:
- Domande su date/scadenze specifiche
- Richieste dati mercato lavoro attuali
- Info su requisiti/test aggiornati
- Qualsiasi riferimento temporale recente

COME INTEGRARE RICERCHE:
- Sempre citare quando usi info da ricerca ("Ho verificato che...")
- Se info contraddittorie, evidenzia incertezza
- Suggerisci verifica diretta per info critiche
- Mai inventare se ricerca non trova risultati

CONTESTO STUDENTE:
[INSERIRE DATI SAFE]

CACHE DISPONIBILE:
[INSERIRE INFO CACHED SE RECENTI]

STILE COMUNICATIVO:
- Informale ma rispettoso
- Emoji parsimoniose
- Frasi brevi
- Mai prescrittivo
- Trasparente su fonti
```

### D. Esempi Search Query Anonimizzate

```python
# GIUSTO - Query anonime
"test medicina 2025 date"
"stipendio medio programmatore junior italia"
"requisiti ITS cybersecurity lombardia"
"università informatica senza test ingresso"

# SBAGLIATO - Query con dati personali
"test medicina Alex_2024"  # NO pseudonimo
"ITS vicino Pavia per Mario"  # NO nome reale
"università per studente con DSA"  # NO info sensibili
"corsi economici famiglia 30k reddito"  # NO dati economici
```

---

## Conclusioni

L'ITIS Orientation Coach rappresenta un approccio innovativo all'orientamento scolastico che:
- Rispetta l'autonomia dello studente
- Valorizza tutti i percorsi possibili post-ITIS
- Garantisce privacy totale
- Fornisce supporto concreto senza pressione
- Documenta il percorso di crescita

Il sistema è progettato per essere un **compagno di viaggio** nel percorso di scoperta e decisione, mai un navigatore che impone la strada.

---

*Documento versione 1.1 - Novembre 2024*
*Aggiornamento: Integrazione capacità di ricerca real-time per informazioni sempre attuali*
*Autore: Sistema di Specifiche per Orientamento ITIS*
