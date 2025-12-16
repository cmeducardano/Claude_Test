# Livello 2: Race Conditions

## Obiettivo
Scoprire i problemi che si verificano quando più thread accedono simultaneamente
a dati condivisi senza sincronizzazione.

---

## Concetti Chiave

### Cos'è una Race Condition?
Una **race condition** (condizione di gara) si verifica quando:
1. Due o più thread accedono a dati condivisi
2. Almeno uno dei thread modifica i dati
3. L'accesso non è sincronizzato

Il risultato dipende dall'**ordine di esecuzione** (scheduling) dei thread,
che è imprevedibile!

### Esempio Classico: Il Contatore

```java
class Contatore {
    private int valore = 0;

    public void incrementa() {
        valore++;  // ATTENZIONE: non è atomico!
    }
}
```

L'operazione `valore++` sembra semplice, ma in realtà sono TRE operazioni:
1. **LEGGI** il valore dalla memoria
2. **INCREMENTA** il valore
3. **SCRIVI** il nuovo valore in memoria

### Cosa Può Andare Storto?

```
Thread A                    Thread B
────────                    ────────
LEGGI valore (= 0)
                            LEGGI valore (= 0)
INCREMENTA (0 → 1)
                            INCREMENTA (0 → 1)
SCRIVI valore (= 1)
                            SCRIVI valore (= 1)

Risultato finale: 1 (invece di 2!)
```

Questo problema si chiama **Lost Update** (aggiornamento perso).

### Sezione Critica
La porzione di codice che accede a risorse condivise si chiama **sezione critica**.
Deve essere protetta per evitare race condition.

---

## Esercizi

### Esercizio 2.1: Scoprire la Race Condition
Apri `Esercizio01_ContatoreRotto.java` e osserva il problema.

### Esercizio 2.2: Il Conto Bancario
Apri `Esercizio02_ContoBancario.java` - simulazione di prelievi e depositi.

### Esercizio 2.3: La Biglietteria
Apri `Esercizio03_Biglietteria.java` - vendita concorrente di biglietti.

---

## Domande di Riflessione

1. Perché il contatore non raggiunge mai 20000?
2. Il bug si manifesta sempre? Perché a volte sì e a volte no?
3. Cosa succederebbe con un solo thread?
4. Come potresti "forzare" la manifestazione del bug?

---

## Da Ricordare

- Le race condition sono **bug intermittenti**: difficili da riprodurre!
- Il fatto che il codice "funzioni" non significa che sia corretto
- Ogni operazione "composta" su dati condivisi è potenzialmente pericolosa
- **Mai assumere** un ordine di esecuzione tra thread
