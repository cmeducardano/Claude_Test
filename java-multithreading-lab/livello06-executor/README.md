# Livello 6: Executor Framework e Thread Pool

## Obiettivo
Imparare a gestire i thread in modo professionale usando l'ExecutorService,
evitando la gestione manuale dei thread.

---

## Concetti Chiave

### Perché Executor Framework?
Creare thread manualmente ha problemi:
- Overhead di creazione/distruzione thread
- Difficile limitare il numero di thread
- Gestione del ciclo di vita complicata

### ExecutorService

```java
import java.util.concurrent.*;

// Crea un pool con numero fisso di thread
ExecutorService executor = Executors.newFixedThreadPool(4);

// Sottometti task
executor.submit(() -> System.out.println("Task eseguito!"));

// IMPORTANTE: shutdown quando hai finito!
executor.shutdown();
```

### Tipi di Executor

| Factory Method | Descrizione |
|---------------|-------------|
| `newFixedThreadPool(n)` | Pool con N thread fissi |
| `newCachedThreadPool()` | Pool che crea thread on-demand |
| `newSingleThreadExecutor()` | Un solo thread (coda sequenziale) |
| `newScheduledThreadPool(n)` | Per task programmati/periodici |
| `newWorkStealingPool()` | Usa tutti i core disponibili (Java 8+) |

### Runnable vs Callable

```java
// Runnable: nessun valore di ritorno
Runnable task = () -> System.out.println("Fatto!");

// Callable: ritorna un valore
Callable<Integer> task = () -> {
    return 42;
};
```

### Future: Risultati Asincroni

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);
    return 42;
});

// Altre operazioni mentre il task esegue...

// Ottieni il risultato (blocca se non pronto)
Integer risultato = future.get();

// Con timeout
Integer ris = future.get(5, TimeUnit.SECONDS);

// Verifica stato
future.isDone();      // Completato?
future.isCancelled(); // Cancellato?
future.cancel(true);  // Cancella
```

### Shutdown Corretto

```java
executor.shutdown();              // Non accetta nuovi task
executor.awaitTermination(60, TimeUnit.SECONDS);  // Aspetta

// Oppure shutdown forzato
executor.shutdownNow();           // Interrompe i task in esecuzione
```

### Pattern Comune

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
try {
    // Sottometti task...
    List<Future<Result>> futures = executor.invokeAll(tasks);

    // Processa risultati...
    for (Future<Result> f : futures) {
        Result r = f.get();
        // ...
    }
} finally {
    executor.shutdown();
}
```

---

## Esercizi

### Esercizio 6.1: Primo ExecutorService
Apri `Esercizio01_ExecutorBase.java` - usa un pool di thread.

### Esercizio 6.2: Future e Callable
Apri `Esercizio02_FutureCallable.java` - lavora con risultati asincroni.

### Esercizio 6.3: Elaborazione Parallela
Apri `Esercizio03_ElaborazioneParallela.java` - processa dati in parallelo.

---

## Domande di Riflessione

1. Perché è importante chiamare shutdown()?
2. Quanti thread dovrebbe avere il pool per task CPU-bound? E per I/O-bound?
3. Cosa succede se sottometti un task dopo shutdown()?
4. Qual è la differenza tra shutdown() e shutdownNow()?

---

## Da Ricordare

- Preferisci ExecutorService alla gestione manuale dei thread
- **SEMPRE** chiama shutdown() o usa try-with-resources
- Scegli la dimensione del pool in base al tipo di task
- Future.get() blocca: usalo con cautela o con timeout
