# Livello 1: Basi dei Thread

## Obiettivo
Comprendere come creare, avviare e gestire i thread in Java.

---

## Concetti Chiave

### Cos'è un Thread?
Un **thread** è un flusso di esecuzione indipendente all'interno di un programma.
Ogni programma Java ha almeno un thread: il **main thread**.

### Due modi per creare un Thread

#### 1. Estendere la classe Thread
```java
class MioThread extends Thread {
    @Override
    public void run() {
        // Codice eseguito dal thread
        System.out.println("Ciao dal thread!");
    }
}

// Utilizzo
MioThread t = new MioThread();
t.start(); // NON usare run() direttamente!
```

#### 2. Implementare Runnable (consigliato)
```java
class MioTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Ciao dal task!");
    }
}

// Utilizzo
Thread t = new Thread(new MioTask());
t.start();

// Oppure con lambda (Java 8+)
Thread t2 = new Thread(() -> System.out.println("Ciao con lambda!"));
t2.start();
```

### Metodi Fondamentali

| Metodo | Descrizione |
|--------|-------------|
| `start()` | Avvia il thread (chiama run() in un nuovo flusso) |
| `run()` | Contiene il codice da eseguire (NON chiamarlo direttamente!) |
| `join()` | Attende che il thread termini |
| `sleep(ms)` | Mette in pausa il thread per ms millisecondi |
| `isAlive()` | Verifica se il thread è ancora in esecuzione |
| `getName()` | Restituisce il nome del thread |

### Ciclo di Vita di un Thread

```
NEW --> RUNNABLE --> RUNNING --> TERMINATED
            ^           |
            |           v
            +-- BLOCKED/WAITING
```

---

## Esercizi

### Esercizio 1.1: Il Tuo Primo Thread
Apri `Esercizio01_PrimoThread.java` e completa i TODO.

### Esercizio 1.2: Thread Multipli
Apri `Esercizio02_ThreadMultipli.java` e osserva l'interleaving.

### Esercizio 1.3: Join e Sincronizzazione Base
Apri `Esercizio03_Join.java` e usa join() per coordinare i thread.

---

## Domande di Riflessione

1. Cosa succede se chiami `run()` invece di `start()`?
2. Perché l'output dei thread appare in ordine diverso ogni volta?
3. Qual è la differenza tra `sleep()` e `join()`?
4. Perché è preferibile implementare `Runnable` invece di estendere `Thread`?

---

## Da Ricordare

- Usa sempre `start()` per avviare un thread, mai `run()` direttamente
- L'ordine di esecuzione dei thread NON è garantito
- `join()` permette di attendere che un thread finisca
- `sleep()` può lanciare `InterruptedException`
