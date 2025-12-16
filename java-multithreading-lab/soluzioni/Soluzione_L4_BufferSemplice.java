/**
 * SOLUZIONE - Livello 4, Esercizio 1: Buffer Semplice
 */
public class Soluzione_L4_BufferSemplice {

    static class Buffer {
        private Integer valore = null;

        // SOLUZIONE TODO 1: Metodo put() completo
        public synchronized void put(int v) throws InterruptedException {
            // Aspetta finché il buffer è pieno
            // USA WHILE, non IF! (per gestire spurious wakeup)
            while (valore != null) {
                wait();
            }

            // Inserisci il valore
            System.out.println("[PUT] Inserisco: " + v);
            valore = v;

            // Notifica un consumer in attesa
            notify();
        }

        // SOLUZIONE TODO 2: Metodo get() completo
        public synchronized int get() throws InterruptedException {
            // Aspetta finché il buffer è vuoto
            while (valore == null) {
                wait();
            }

            // Preleva il valore
            int v = valore;
            valore = null;  // Svuota il buffer
            System.out.println("[GET] Prelevo: " + v);

            // Notifica un producer in attesa
            notify();

            return v;
        }
    }

    static class Producer implements Runnable {
        private Buffer buffer;
        private int count;

        public Producer(Buffer buffer, int count) {
            this.buffer = buffer;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= count; i++) {
                    buffer.put(i);
                    Thread.sleep(100);
                }
                System.out.println("[Producer] Ho finito di produrre!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private Buffer buffer;
        private int count;
        private int somma = 0;

        public Consumer(Buffer buffer, int count) {
            this.buffer = buffer;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    int v = buffer.get();
                    somma += v;
                    Thread.sleep(150);
                }
                System.out.println("[Consumer] Ho finito! Somma: " + somma);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public int getSomma() {
            return somma;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== SOLUZIONE: Buffer Semplice ===\n");

        Buffer buffer = new Buffer();
        final int N = 10;

        Producer producer = new Producer(buffer, N);
        Consumer consumer = new Consumer(buffer, N);

        Thread tProd = new Thread(producer);
        Thread tCons = new Thread(consumer);

        tProd.start();
        tCons.start();

        tProd.join();
        tCons.join();

        System.out.println("\n--- Fine ---");

        int sommaAttesa = N * (N + 1) / 2;  // Formula di Gauss
        System.out.println("Somma calcolata: " + consumer.getSomma());
        System.out.println("Somma attesa:    " + sommaAttesa);

        if (consumer.getSomma() == sommaAttesa) {
            System.out.println("\nTEST PASSATO!");
        } else {
            System.out.println("\nERRORE!");
        }
    }
}

/*
 * SPIEGAZIONE DETTAGLIATA:
 *
 * put(v):
 * 1. Acquisisce il lock (synchronized)
 * 2. Se valore != null (buffer pieno), chiama wait()
 *    - wait() RILASCIA il lock e mette il thread in attesa
 * 3. Quando viene svegliato (notify), ricontrolla la condizione (while)
 * 4. Se valore == null (buffer vuoto), inserisce v
 * 5. Chiama notify() per svegliare un eventuale consumer in attesa
 * 6. Rilascia il lock (fine del metodo synchronized)
 *
 * get():
 * 1. Acquisisce il lock
 * 2. Se valore == null (buffer vuoto), chiama wait()
 * 3. Quando svegliato, ricontrolla (while)
 * 4. Se c'è un valore, lo preleva e imposta valore = null
 * 5. Chiama notify() per svegliare un eventuale producer
 * 6. Rilascia il lock
 *
 * PERCHÉ WHILE E NON IF?
 *
 * Scenario problematico con if:
 * - Consumer1 trova buffer vuoto, chiama wait()
 * - Consumer2 trova buffer vuoto, chiama wait()
 * - Producer inserisce un valore, chiama notify()
 * - Consumer1 si sveglia, prende il valore
 * - Consumer2 si sveglia anche lui (spurious wakeup o notify multiplo)
 * - Consumer2 NON ricontrolla la condizione (con if)
 * - Consumer2 prova a prelevare da un buffer vuoto! ERRORE!
 *
 * Con while:
 * - Consumer2 si sveglia, ricontrolla: valore == null? SÌ
 * - Torna a wait() -> Comportamento corretto!
 */
