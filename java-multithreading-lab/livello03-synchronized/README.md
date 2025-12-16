# Livello 3: Sincronizzazione con synchronized

## Obiettivo
Imparare a proteggere le sezioni critiche usando la keyword `synchronized`
per eliminare le race condition.

---

## Concetti Chiave

### Monitor e Lock Intrinseco
Ogni oggetto Java ha un **lock intrinseco** (o monitor lock).
Quando un thread entra in un blocco `synchronized`, acquisisce il lock.
Gli altri thread devono attendere che il lock venga rilasciato.

### Due Modi di Usare synchronized

#### 1. Metodo Sincronizzato
```java
public synchronized void metodoSicuro() {
    // L'intero metodo è protetto
    // Il lock è sull'oggetto this
}

public static synchronized void metodoStatico() {
    // Il lock è sull'oggetto Class
}
```

#### 2. Blocco Sincronizzato
```java
public void metodo() {
    // Codice non sincronizzato...

    synchronized(this) {
        // Solo questa parte è protetta
    }

    // Altro codice non sincronizzato...
}

// Oppure su un oggetto specifico
private final Object lock = new Object();

public void metodo() {
    synchronized(lock) {
        // Sezione critica
    }
}
```

### Quale Scegliere?

| Metodo Sincronizzato | Blocco Sincronizzato |
|---------------------|----------------------|
| Più semplice | Più flessibile |
| Protegge tutto il metodo | Protegge solo il necessario |
| Meno performante | Più performante |
| Lock implicito (this) | Lock esplicito |

### Regole Importanti

1. **Stessa risorsa, stesso lock**: tutti i thread che accedono alla stessa
   risorsa condivisa devono sincronizzarsi sullo stesso oggetto lock.

2. **Minimizza la sezione critica**: tieni sincronizzato solo il codice
   strettamente necessario per massimizzare il parallelismo.

3. **Evita lock su oggetti pubblici**: preferisci oggetti lock privati
   per evitare interferenze esterne.

```java
// MALE: lock su this (pubblico)
public synchronized void metodo() { ... }

// BENE: lock privato
private final Object lock = new Object();
public void metodo() {
    synchronized(lock) { ... }
}
```

### Rientranza (Reentrancy)

I lock Java sono **rientranti**: un thread che possiede già un lock può
acquisirlo di nuovo senza bloccarsi.

```java
public synchronized void a() {
    b(); // OK! Stesso thread, stesso lock
}

public synchronized void b() {
    // Funziona senza deadlock
}
```

---

## Esercizi

### Esercizio 3.1: Riparare il Contatore
Apri `Esercizio01_ContatoreRiparato.java` - risolvi la race condition del Livello 2.

### Esercizio 3.2: Il Conto Bancario Sicuro
Apri `Esercizio02_ContoBancarioSicuro.java` - sincronizza le operazioni bancarie.

### Esercizio 3.3: Blocchi Sincronizzati
Apri `Esercizio03_BlocchiSync.java` - pratica con blocchi sincronizzati.

---

## Domande di Riflessione

1. Perché "synchronized" risolve la race condition?
2. Cosa succederebbe se sincronizzassimo solo il metodo `preleva()` ma non `deposita()`?
3. Qual è lo svantaggio di usare `synchronized` su ogni metodo?
4. Come si può verificare che non ci siano più race condition?

---

## Da Ricordare

- `synchronized` garantisce **mutua esclusione**: un solo thread alla volta
- Il lock viene rilasciato automaticamente alla fine del blocco (anche con eccezioni)
- Troppa sincronizzazione = poca concorrenza = prestazioni peggiori
- Sincronizzazione mancante = bug, sincronizzazione eccessiva = bottleneck
