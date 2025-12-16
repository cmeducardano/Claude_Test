# Laboratorio di Multithreading in Java

## Esercitazione per il 4° Anno

Benvenuto in questo laboratorio pratico sulla programmazione concorrente in Java!
Attraverso 6 livelli progressivi, scoprirai come gestire i thread e le tecniche di sincronizzazione.

---

## Obiettivi di Apprendimento

Al termine di questa esercitazione sarai in grado di:

1. Creare e gestire thread in Java (estendendo `Thread` o implementando `Runnable`)
2. Identificare e comprendere le **race condition**
3. Utilizzare `synchronized` per proteggere le sezioni critiche
4. Implementare pattern di comunicazione con `wait()` e `notify()`
5. Usare i lock espliciti (`ReentrantLock`, `ReadWriteLock`)
6. Sfruttare l'`ExecutorService` per gestire pool di thread

---

## Struttura del Laboratorio

```
java-multithreading-lab/
├── livello01-basi-thread/       # Creazione e ciclo di vita dei thread
├── livello02-race-conditions/   # Scoprire i problemi di concorrenza
├── livello03-synchronized/      # Sincronizzazione con synchronized
├── livello04-wait-notify/       # Pattern Producer-Consumer
├── livello05-locks-avanzati/    # ReentrantLock, ReadWriteLock
├── livello06-executor/          # ExecutorService e ThreadPool
└── soluzioni/                   # Soluzioni complete (da consultare dopo!)
```

---

## Come Procedere

### Per ogni livello:

1. **Leggi** il file `README.md` del livello per comprendere i concetti
2. **Analizza** il codice di partenza nei file `.java`
3. **Completa** i `TODO` indicati nel codice
4. **Esegui** il programma e osserva il comportamento
5. **Rispondi** alle domande di riflessione
6. **Confronta** con la soluzione solo dopo aver provato!

### Compilazione ed Esecuzione

```bash
# Compila un file Java
javac NomeFile.java

# Esegui
java NomeFile
```

---

## Livelli

### Livello 1: Basi dei Thread
Impara a creare thread, avviarli e osservare il loro ciclo di vita.
- Estendere la classe `Thread`
- Implementare l'interfaccia `Runnable`
- Metodi `start()`, `join()`, `sleep()`

### Livello 2: Race Conditions
Scopri cosa succede quando più thread accedono alla stessa risorsa senza sincronizzazione.
- Condizioni di gara
- Risultati non deterministici
- Perché serve la sincronizzazione

### Livello 3: Synchronized
Impara a proteggere le sezioni critiche del codice.
- Keyword `synchronized`
- Metodi sincronizzati
- Blocchi sincronizzati
- Monitor e lock intrinseci

### Livello 4: Wait e Notify
Implementa la comunicazione tra thread con il pattern Producer-Consumer.
- `wait()`, `notify()`, `notifyAll()`
- Buffer condiviso
- Coordinazione tra produttori e consumatori

### Livello 5: Lock Avanzati
Esplora i meccanismi di lock espliciti per scenari complessi.
- `ReentrantLock`
- `ReadWriteLock`
- `Condition`
- Try-lock e timeout

### Livello 6: Executor Framework
Gestisci pool di thread in modo professionale.
- `ExecutorService`
- `ThreadPoolExecutor`
- `Future` e `Callable`
- Shutdown corretto

---

## Prerequisiti

- Java JDK 11 o superiore
- Conoscenza base di Java (classi, interfacce, ereditarietà)
- Un IDE o editor di testo

---

## Suggerimenti

- **Non sbirciare le soluzioni!** Prova sempre prima da solo
- **Esegui più volte** lo stesso programma: i bug di concorrenza sono intermittenti
- **Usa i breakpoint** del debugger per osservare lo stato dei thread
- **Discuti con i compagni**: la concorrenza è un argomento complesso!

---

## Glossario

| Termine | Definizione |
|---------|-------------|
| **Thread** | Flusso di esecuzione indipendente all'interno di un processo |
| **Race Condition** | Bug che si verifica quando il risultato dipende dall'ordine di esecuzione dei thread |
| **Sezione Critica** | Porzione di codice che accede a risorse condivise |
| **Deadlock** | Situazione in cui due o più thread si bloccano a vicenda |
| **Monitor** | Meccanismo di sincronizzazione intrinseco di Java |
| **Mutex** | Mutual Exclusion - garantisce accesso esclusivo a una risorsa |

---

Buon lavoro e buon divertimento con i thread!
