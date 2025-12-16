# Livello 5: Lock Espliciti e Condizioni Avanzate

## Obiettivo
Esplorare i meccanismi di sincronizzazione avanzati offerti dal package
`java.util.concurrent.locks`, che offrono più controllo rispetto a `synchronized`.

---

## Concetti Chiave

### Perché Lock Espliciti?
`synchronized` è semplice ma limitato:
- Non puoi interrompere un thread in attesa del lock
- Non puoi provare a acquisire il lock senza bloccarti
- Non puoi avere timeout sull'attesa
- Non puoi avere condizioni multiple separate

### ReentrantLock
```java
import java.util.concurrent.locks.ReentrantLock;

ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // Sezione critica
} finally {
    lock.unlock();  // SEMPRE nel finally!
}
```

### Metodi Utili di ReentrantLock

| Metodo | Descrizione |
|--------|-------------|
| `lock()` | Acquisisce il lock (blocca se non disponibile) |
| `unlock()` | Rilascia il lock |
| `tryLock()` | Prova a acquisire, ritorna subito (true/false) |
| `tryLock(time, unit)` | Prova a acquisire con timeout |
| `lockInterruptibly()` | Acquisisce, ma può essere interrotto |
| `isHeldByCurrentThread()` | Verifica se il thread corrente ha il lock |

### Condition (Sostituto di wait/notify)
```java
ReentrantLock lock = new ReentrantLock();
Condition nonVuoto = lock.newCondition();
Condition nonPieno = lock.newCondition();

// Attesa su condizione specifica
lock.lock();
try {
    while (buffer.isEmpty()) {
        nonVuoto.await();
    }
    // ...
    nonPieno.signal();
} finally {
    lock.unlock();
}
```

### ReadWriteLock
Permette accesso concorrente in lettura, ma esclusivo in scrittura.

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();

// Lettura (multipli thread possono leggere insieme)
rwLock.readLock().lock();
try {
    // Leggi dati
} finally {
    rwLock.readLock().unlock();
}

// Scrittura (un solo thread alla volta)
rwLock.writeLock().lock();
try {
    // Modifica dati
} finally {
    rwLock.writeLock().unlock();
}
```

### synchronized vs Lock Espliciti

| Caratteristica | synchronized | Lock |
|---------------|--------------|------|
| Rilascio automatico | Sì | No (devi usare finally) |
| Interrompibile | No | Sì (lockInterruptibly) |
| Try-lock | No | Sì (tryLock) |
| Timeout | No | Sì (tryLock con timeout) |
| Condizioni multiple | No | Sì (newCondition) |
| Fairness | No | Opzionale |
| Complessità | Bassa | Media |

---

## Esercizi

### Esercizio 5.1: ReentrantLock e TryLock
Apri `Esercizio01_ReentrantLock.java` - usa lock espliciti con tryLock.

### Esercizio 5.2: Condizioni Multiple
Apri `Esercizio02_Conditions.java` - implementa Producer-Consumer con Condition.

### Esercizio 5.3: ReadWriteLock per Cache
Apri `Esercizio03_ReadWriteLock.java` - ottimizza l'accesso a una cache.

---

## Domande di Riflessione

1. Perché è importante usare unlock() in un blocco finally?
2. Quando preferiresti tryLock() a lock()?
3. In quali scenari il ReadWriteLock offre vantaggi?
4. Cosa succede se dimentichi di chiamare unlock()?

---

## Da Ricordare

- **SEMPRE** unlock() nel finally!
- Lock espliciti danno più controllo ma più responsabilità
- Preferisci synchronized per casi semplici
- ReadWriteLock utile quando le letture sono molto più frequenti delle scritture
