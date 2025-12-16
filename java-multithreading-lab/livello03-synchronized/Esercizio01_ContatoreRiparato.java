/**
 * LIVELLO 3 - Esercizio 1: Riparare il Contatore
 *
 * Obiettivo: Usare synchronized per risolvere la race condition
 *            del contatore visto nel Livello 2.
 *
 * Ricordi? Il contatore perdeva incrementi perché valore++ non è atomico.
 * Ora lo ripareremo!
 */
public class Esercizio01_ContatoreRiparato {

    // Versione 1: Metodo synchronized
    static class ContatoreSyncMetodo {
        private int valore = 0;

        // TODO 1: Aggiungi la keyword "synchronized" a questo metodo
        public void incrementa() {
            valore++;
        }

        // TODO 2: Anche getValore() dovrebbe essere synchronized?
        // Cosa succede se non lo è? Prova a ragionare...
        public int getValore() {
            return valore;
        }
    }

    // Versione 2: Blocco synchronized
    static class ContatoreSyncBlocco {
        private int valore = 0;
        private final Object lock = new Object();

        public void incrementa() {
            // TODO 3: Aggiungi un blocco synchronized intorno a valore++
            // Usa l'oggetto "lock" come monitor
            // synchronized(???) {
            //     ???
            // }
            valore++;
        }

        public int getValore() {
            synchronized (lock) {
                return valore;
            }
        }
    }

    // Task di incremento (uguale al Livello 2)
    static class TaskIncremento implements Runnable {
        private ContatoreSyncMetodo contatore;
        private int numeroIncrementi;

        public TaskIncremento(ContatoreSyncMetodo contatore, int numeroIncrementi) {
            this.contatore = contatore;
            this.numeroIncrementi = numeroIncrementi;
        }

        @Override
        public void run() {
            for (int i = 0; i < numeroIncrementi; i++) {
                contatore.incrementa();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 1: Il Contatore Riparato ===\n");

        final int INCREMENTI_PER_THREAD = 100000;
        final int NUM_ESECUZIONI = 5;

        System.out.println("Eseguiamo " + NUM_ESECUZIONI + " test con " +
                (INCREMENTI_PER_THREAD * 2) + " incrementi totali ciascuno.\n");

        boolean tuttiCorretti = true;

        for (int test = 1; test <= NUM_ESECUZIONI; test++) {
            ContatoreSyncMetodo contatore = new ContatoreSyncMetodo();

            Thread t1 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD));
            Thread t2 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD));

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            int risultato = contatore.getValore();
            int atteso = INCREMENTI_PER_THREAD * 2;
            boolean corretto = (risultato == atteso);

            System.out.printf("Test %d: %d/%d %s%n",
                    test, risultato, atteso,
                    corretto ? "OK" : "ERRORE!");

            if (!corretto) tuttiCorretti = false;
        }

        System.out.println();
        if (tuttiCorretti) {
            System.out.println("Tutti i test sono passati!");
            System.out.println("La race condition è stata risolta.");
        } else {
            System.out.println("Alcuni test sono falliti!");
            System.out.println("Hai aggiunto synchronized correttamente?");
        }

        // TODO 4: Dopo aver completato i TODO sopra, esegui il programma.
        // Tutti i test dovrebbero passare ora!

        // TODO 5: Prova a rimuovere synchronized e verifica che il bug riappare.

        // TODO 6: Misura le prestazioni: quanto tempo ci vuole con synchronized
        // rispetto a senza? (Aggiungi System.currentTimeMillis() prima e dopo)
        // Il costo della sincronizzazione è visibile?
    }
}

/*
 * COSA FA synchronized?
 *
 * Prima (senza synchronized):
 * Thread A                    Thread B
 * ────────                    ────────
 * temp = valore (0)
 *                             temp = valore (0)
 * temp = temp + 1
 *                             temp = temp + 1
 * valore = 1
 *                             valore = 1  ← PERSO!
 *
 * Dopo (con synchronized):
 * Thread A                    Thread B
 * ────────                    ────────
 * acquista lock
 * temp = valore (0)           [in attesa del lock...]
 * temp = temp + 1             [in attesa del lock...]
 * valore = 1                  [in attesa del lock...]
 * rilascia lock
 *                             acquista lock
 *                             temp = valore (1)
 *                             temp = temp + 1
 *                             valore = 2  ← CORRETTO!
 *                             rilascia lock
 */
