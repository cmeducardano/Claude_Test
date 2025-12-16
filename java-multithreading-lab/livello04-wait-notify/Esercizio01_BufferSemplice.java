/**
 * LIVELLO 4 - Esercizio 1: Il Buffer Semplice
 *
 * Obiettivo: Comprendere il funzionamento di wait() e notify()
 *            con un buffer che può contenere UN solo elemento.
 *
 * Il Producer deve aspettare se il buffer è pieno.
 * Il Consumer deve aspettare se il buffer è vuoto.
 */
public class Esercizio01_BufferSemplice {

    static class Buffer {
        private Integer valore = null;  // null = buffer vuoto

        // TODO 1: Implementa il metodo put()
        // - Deve essere synchronized
        // - Se il buffer è pieno (valore != null), aspetta con wait()
        // - Quando c'è spazio, inserisce il valore
        // - Notifica i thread in attesa con notify()

        public synchronized void put(int v) throws InterruptedException {
            // Aspetta finché il buffer è pieno
            // RICORDA: usa while, non if!
            /*
            while (???) {
                wait();
            }
            */

            // Inserisci il valore
            System.out.println("[PUT] Inserisco: " + v);
            // valore = ???;

            // Notifica un consumer in attesa
            // ???();
        }

        // TODO 2: Implementa il metodo get()
        // - Deve essere synchronized
        // - Se il buffer è vuoto (valore == null), aspetta con wait()
        // - Quando c'è un valore, lo preleva e svuota il buffer
        // - Notifica i thread in attesa con notify()

        public synchronized int get() throws InterruptedException {
            // Aspetta finché il buffer è vuoto
            /*
            while (???) {
                wait();
            }
            */

            // Preleva il valore
            // int v = valore;
            // valore = ???;
            // System.out.println("[GET] Prelevo: " + v);

            // Notifica un producer in attesa
            // ???();

            // return v;
            return 0; // Placeholder - rimuovi dopo aver implementato
        }
    }

    // Producer che inserisce numeri da 1 a N
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
                    Thread.sleep(100); // Simula tempo di produzione
                }
                System.out.println("[Producer] Ho finito di produrre!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Consumer che preleva numeri
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
                    Thread.sleep(150); // Simula tempo di consumo
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
        System.out.println("=== Esercizio 1: Buffer Semplice ===\n");

        Buffer buffer = new Buffer();
        final int N = 10;

        Producer producer = new Producer(buffer, N);
        Consumer consumer = new Consumer(buffer, N);

        Thread tProd = new Thread(producer);
        Thread tCons = new Thread(consumer);

        System.out.println("Avvio Producer e Consumer...\n");

        tProd.start();
        tCons.start();

        tProd.join();
        tCons.join();

        System.out.println("\n--- Fine ---");

        // Verifica: la somma dei numeri da 1 a 10 è 55
        int sommaAttesa = N * (N + 1) / 2;
        System.out.println("Somma calcolata: " + consumer.getSomma());
        System.out.println("Somma attesa:    " + sommaAttesa);

        if (consumer.getSomma() == sommaAttesa) {
            System.out.println("Tutti i numeri sono stati trasferiti correttamente!");
        } else {
            System.out.println("ERRORE: Alcuni numeri sono andati persi!");
        }

        // TODO 3: Dopo aver implementato put() e get(), esegui il programma.
        // Osserva come PUT e GET si alternano.


        // TODO 4: Cosa succede se il Consumer è più lento del Producer?
        // (Aumenta lo sleep del Consumer a 300ms)
        // Osserva come il Producer aspetta.


        // TODO 5: E se il Producer è più lento del Consumer?
        // (Aumenta lo sleep del Producer a 300ms)
        // Osserva come il Consumer aspetta.
    }
}

/*
 * VISUALIZZAZIONE DEL FLUSSO:
 *
 * Producer                      Buffer                     Consumer
 * ────────                      ──────                     ────────
 * put(1)  ─────────────────►   [1]
 *                                          get() ◄──────  riceve 1
 * put(2)  ─────────────────►   [2]
 *                                          get() ◄──────  riceve 2
 * put(3)
 *   └─► buffer pieno, wait()
 *                                          get() ◄──────  riceve 3
 *   └─► notify(), continua
 * put(4)  ─────────────────►   [4]
 * ...
 *
 * Nota: il Consumer è più lento, quindi il Producer a volte aspetta.
 */
