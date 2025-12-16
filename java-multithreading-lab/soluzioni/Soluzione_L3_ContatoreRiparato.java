/**
 * SOLUZIONE - Livello 3, Esercizio 1: Contatore Riparato
 */
public class Soluzione_L3_ContatoreRiparato {

    // SOLUZIONE: Metodo synchronized
    static class ContatoreSyncMetodo {
        private int valore = 0;

        // TODO 1 SOLUZIONE: Aggiungi synchronized
        public synchronized void incrementa() {
            valore++;
        }

        // TODO 2 SOLUZIONE: Sì, anche getValore() dovrebbe essere synchronized
        // per garantire la visibilità delle modifiche tra thread.
        // Senza synchronized, un thread potrebbe leggere un valore "stale"
        // dalla cache del processore.
        public synchronized int getValore() {
            return valore;
        }
    }

    // SOLUZIONE: Blocco synchronized
    static class ContatoreSyncBlocco {
        private int valore = 0;
        private final Object lock = new Object();

        public void incrementa() {
            // TODO 3 SOLUZIONE: Blocco synchronized
            synchronized (lock) {
                valore++;
            }
        }

        public int getValore() {
            synchronized (lock) {
                return valore;
            }
        }
    }

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
        System.out.println("=== SOLUZIONE: Il Contatore Riparato ===\n");

        final int INCREMENTI_PER_THREAD = 100000;

        ContatoreSyncMetodo contatore = new ContatoreSyncMetodo();

        Thread t1 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD));
        Thread t2 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD));

        long inizio = System.currentTimeMillis();

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        long fine = System.currentTimeMillis();

        int risultato = contatore.getValore();
        int atteso = INCREMENTI_PER_THREAD * 2;

        System.out.println("Risultato: " + risultato);
        System.out.println("Atteso:    " + atteso);
        System.out.println("Tempo:     " + (fine - inizio) + " ms");

        if (risultato == atteso) {
            System.out.println("\nTEST PASSATO! La race condition è stata risolta.");
        } else {
            System.out.println("\nERRORE! Qualcosa non va.");
        }
    }
}
