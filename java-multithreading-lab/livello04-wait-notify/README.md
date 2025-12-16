# Livello 4: Wait, Notify e il Pattern Producer-Consumer

## Obiettivo
Imparare a far comunicare i thread tra loro usando `wait()` e `notify()`
per implementare pattern di coordinazione come Producer-Consumer.

---

## Concetti Chiave

### Il Problema
Finora abbiamo visto come proteggere i dati con `synchronized`.
Ma come facciamo se un thread deve **aspettare** che un altro thread faccia qualcosa?

Esempio: un thread consuma dati da un buffer, ma il buffer è vuoto.
Deve aspettare che un altro thread produca qualcosa.

### Busy Waiting (MALE!)
```java
// NON FARE COSÌ!
while (buffer.isEmpty()) {
    // Spreco di CPU! Il thread gira a vuoto.
}
// Usa il dato
```

### Wait e Notify (BENE!)
```java
synchronized(lock) {
    while (buffer.isEmpty()) {
        lock.wait();  // Rilascia il lock e si mette in attesa
    }
    // Usa il dato
}

// Un altro thread:
synchronized(lock) {
    buffer.add(dato);
    lock.notify();  // Sveglia un thread in attesa
}
```

### I Metodi di Object

| Metodo | Descrizione |
|--------|-------------|
| `wait()` | Rilascia il lock e si mette in attesa |
| `wait(timeout)` | Come wait(), ma con timeout in ms |
| `notify()` | Sveglia UN thread in attesa sul lock |
| `notifyAll()` | Sveglia TUTTI i thread in attesa |

### Regole Importanti

1. **Devi avere il lock**: `wait()` e `notify()` devono essere chiamati
   dentro un blocco `synchronized` sullo stesso oggetto.

2. **Usa while, non if**: Dopo il risveglio, ricontrolla sempre la condizione!

```java
// SBAGLIATO (usa if)
synchronized(lock) {
    if (buffer.isEmpty()) {
        lock.wait();
    }
    // ERRORE: la condizione potrebbe non essere più vera!
    buffer.get();
}

// CORRETTO (usa while)
synchronized(lock) {
    while (buffer.isEmpty()) {
        lock.wait();
    }
    // Sicuro: la condizione è stata riverificata
    buffer.get();
}
```

3. **Spurious wakeup**: Un thread può svegliarsi anche senza un `notify()`!
   Per questo il `while` è obbligatorio.

### Il Pattern Producer-Consumer

```
┌─────────────┐        ┌───────────┐        ┌─────────────┐
│  Producer   │ ─────► │  Buffer   │ ─────► │  Consumer   │
│             │  put() │  (coda)   │  get() │             │
└─────────────┘        └───────────┘        └─────────────┘
                            │
                      wait/notify
```

- **Producer**: Produce dati e li mette nel buffer
- **Buffer**: Memorizza i dati temporaneamente
- **Consumer**: Preleva e consuma i dati

---

## Esercizi

### Esercizio 4.1: Il Buffer Semplice
Apri `Esercizio01_BufferSemplice.java` - implementa wait/notify di base.

### Esercizio 4.2: Producer-Consumer
Apri `Esercizio02_ProducerConsumer.java` - il pattern completo.

### Esercizio 4.3: La Pizzeria
Apri `Esercizio03_Pizzeria.java` - simulazione realistica con più produttori e consumatori.

---

## Domande di Riflessione

1. Perché wait() rilascia il lock?
2. Cosa succederebbe se usassimo `if` invece di `while`?
3. Quando conviene usare `notify()` vs `notifyAll()`?
4. Come si può limitare la dimensione del buffer?

---

## Da Ricordare

- `wait()` e `notify()` sono metodi di `Object`, non di `Thread`!
- Devono essere chiamati tenendo il lock sull'oggetto
- Usa sempre `while` per controllare la condizione, mai `if`
- `notifyAll()` è più sicuro ma meno efficiente di `notify()`
