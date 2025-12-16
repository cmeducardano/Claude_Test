/**
 * LIVELLO 4 - Esercizio 2: Producer-Consumer Completo
 *
 * Obiettivo: Implementare il pattern Producer-Consumer con un buffer
 *            di dimensione limitata (bounded buffer).
 *
 * Il buffer può contenere MAX elementi.
 * - Producer aspetta se il buffer è pieno
 * - Consumer aspetta se il buffer è vuoto
 */
import java.util.LinkedList;
import java.util.Queue;

public class Esercizio02_ProducerConsumer {

    static class BoundedBuffer<T> {
        private Queue<T> coda;
        private int capacita;

        public BoundedBuffer(int capacita) {
            this.capacita = capacita;
            this.coda = new LinkedList<>();
        }

        // TODO 1: Implementa put() con wait/notify
        public synchronized void put(T elemento) throws InterruptedException {
            // Aspetta finché il buffer è pieno
            while (coda.size() >= capacita) {
                System.out.println("[PUT] Buffer pieno, aspetto...");
                wait();
            }

            // Inserisci l'elemento
            coda.add(elemento);
            System.out.println("[PUT] Inserito: " + elemento +
                    " (dim: " + coda.size() + "/" + capacita + ")");

            // TODO 2: Notifica i thread in attesa
            // Usa notifyAll() perché potrebbero esserci più consumer
            // ???();
        }

        // TODO 3: Implementa get() con wait/notify
        public synchronized T get() throws InterruptedException {
            // Aspetta finché il buffer è vuoto
            while (coda.isEmpty()) {
                System.out.println("[GET] Buffer vuoto, aspetto...");
                wait();
            }

            // Preleva l'elemento
            T elemento = coda.poll();
            System.out.println("[GET] Prelevato: " + elemento +
                    " (dim: " + coda.size() + "/" + capacita + ")");

            // TODO 4: Notifica i thread in attesa
            // ???();

            return elemento;
        }

        public synchronized int size() {
            return coda.size();
        }
    }

    // Producer che produce elementi
    static class Producer implements Runnable {
        private BoundedBuffer<String> buffer;
        private String nome;
        private int numElementi;

        public Producer(BoundedBuffer<String> buffer, String nome, int numElementi) {
            this.buffer = buffer;
            this.nome = nome;
            this.numElementi = numElementi;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= numElementi; i++) {
                    String elemento = nome + "-Item" + i;
                    buffer.put(elemento);

                    // Tempo variabile di produzione
                    Thread.sleep((long) (Math.random() * 200 + 50));
                }
                System.out.println("[" + nome + "] Produzione completata!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Consumer che consuma elementi
    static class Consumer implements Runnable {
        private BoundedBuffer<String> buffer;
        private String nome;
        private int numElementi;
        private int consumati = 0;

        public Consumer(BoundedBuffer<String> buffer, String nome, int numElementi) {
            this.buffer = buffer;
            this.nome = nome;
            this.numElementi = numElementi;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < numElementi; i++) {
                    String elemento = buffer.get();
                    consumati++;

                    // Tempo variabile di consumo
                    Thread.sleep((long) (Math.random() * 300 + 100));
                }
                System.out.println("[" + nome + "] Consumo completato! Tot: " + consumati);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public int getConsumati() {
            return consumati;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 2: Producer-Consumer con Buffer Limitato ===\n");

        // Buffer con capacità 5
        BoundedBuffer<String> buffer = new BoundedBuffer<>(5);

        // 2 Producer che producono 10 elementi ciascuno
        Producer p1 = new Producer(buffer, "P1", 10);
        Producer p2 = new Producer(buffer, "P2", 10);

        // 2 Consumer che consumano 10 elementi ciascuno
        Consumer c1 = new Consumer(buffer, "C1", 10);
        Consumer c2 = new Consumer(buffer, "C2", 10);

        Thread tp1 = new Thread(p1);
        Thread tp2 = new Thread(p2);
        Thread tc1 = new Thread(c1);
        Thread tc2 = new Thread(c2);

        System.out.println("Avvio 2 Producer e 2 Consumer...\n");

        // Avvia tutti
        tp1.start();
        tp2.start();
        tc1.start();
        tc2.start();

        // Aspetta che tutti finiscano
        tp1.join();
        tp2.join();
        tc1.join();
        tc2.join();

        System.out.println("\n--- Riepilogo ---");
        System.out.println("Elementi prodotti: 20");
        System.out.println("Elementi consumati: " + (c1.getConsumati() + c2.getConsumati()));
        System.out.println("Elementi rimasti nel buffer: " + buffer.size());

        // TODO 5: Verifica che tutti i 20 elementi siano stati consumati.
        // Se qualcosa è rimasto nel buffer, c'è un problema!


        // TODO 6: Prova a cambiare il numero di Producer e Consumer:
        // - 3 Producer, 1 Consumer: cosa succede?
        // - 1 Producer, 3 Consumer: cosa succede?


        // TODO 7: Cambia la capacità del buffer a 1 o a 50.
        // Come cambia il comportamento?
    }
}

/*
 * PERCHÉ notifyAll() INVECE DI notify()?
 *
 * Scenario con notify():
 * - P1 produce, buffer pieno, P1 attende
 * - P2 produce, buffer pieno, P2 attende
 * - C1 consuma, chiama notify()
 * - notify() sveglia P1 (a caso)
 * - Ma C2 stava anche aspettando! E se c'era un solo elemento?
 *
 * Con notifyAll() svegli tutti, e il controllo while() farà
 * attendere di nuovo chi non può procedere.
 *
 * Regola pratica: usa notifyAll() quando ci sono multipli thread
 * in attesa su condizioni diverse.
 */
