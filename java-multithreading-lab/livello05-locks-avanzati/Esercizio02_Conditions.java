/**
 * LIVELLO 5 - Esercizio 2: Condizioni Multiple
 *
 * Obiettivo: Usare Condition per implementare un buffer con
 *            condizioni separate per "non pieno" e "non vuoto".
 *
 * Questo offre più efficienza di notifyAll() perché possiamo
 * svegliare SOLO i thread interessati alla condizione specifica.
 */
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.LinkedList;
import java.util.Queue;

public class Esercizio02_Conditions {

    static class BufferConCondizioni<T> {
        private Queue<T> coda = new LinkedList<>();
        private final int capacita;

        private ReentrantLock lock = new ReentrantLock();
        // Condizione: "il buffer non è pieno" (per i producer)
        private Condition nonPieno = lock.newCondition();
        // Condizione: "il buffer non è vuoto" (per i consumer)
        private Condition nonVuoto = lock.newCondition();

        public BufferConCondizioni(int capacita) {
            this.capacita = capacita;
        }

        public void put(T elemento) throws InterruptedException {
            lock.lock();
            try {
                // Aspetta finché il buffer è pieno
                while (coda.size() >= capacita) {
                    System.out.println("[PUT] Buffer pieno, aspetto su 'nonPieno'...");
                    nonPieno.await();  // Equivalente di wait()
                }

                coda.add(elemento);
                System.out.println("[PUT] " + elemento + " aggiunto. Size: " + coda.size());

                // Segnala ai consumer che ora c'è qualcosa
                // TODO 1: Usa il metodo signal() della condizione corretta
                nonVuoto.signal();  // Equivalente di notify()

            } finally {
                lock.unlock();
            }
        }

        public T get() throws InterruptedException {
            lock.lock();
            try {
                // TODO 2: Aspetta finché il buffer è vuoto
                // Usa la condizione 'nonVuoto' e il suo metodo await()
                while (coda.isEmpty()) {
                    System.out.println("[GET] Buffer vuoto, aspetto su 'nonVuoto'...");
                    nonVuoto.await();
                }

                T elemento = coda.poll();
                System.out.println("[GET] " + elemento + " prelevato. Size: " + coda.size());

                // TODO 3: Segnala ai producer che ora c'è spazio
                nonPieno.signal();

                return elemento;

            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return coda.size();
            } finally {
                lock.unlock();
            }
        }
    }

    static class Producer implements Runnable {
        private BufferConCondizioni<Integer> buffer;
        private int id;
        private int count;

        public Producer(BufferConCondizioni<Integer> buffer, int id, int count) {
            this.buffer = buffer;
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= count; i++) {
                    int valore = id * 100 + i;  // Es: Producer 1 produce 101, 102, ...
                    buffer.put(valore);
                    Thread.sleep((long) (Math.random() * 100));
                }
                System.out.println("Producer-" + id + " finito!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private BufferConCondizioni<Integer> buffer;
        private int id;
        private int count;
        private int somma = 0;

        public Consumer(BufferConCondizioni<Integer> buffer, int id, int count) {
            this.buffer = buffer;
            this.id = id;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    Integer valore = buffer.get();
                    somma += valore;
                    Thread.sleep((long) (Math.random() * 150));
                }
                System.out.println("Consumer-" + id + " finito! Somma: " + somma);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public int getSomma() {
            return somma;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 2: Buffer con Condizioni ===\n");

        BufferConCondizioni<Integer> buffer = new BufferConCondizioni<>(3);

        // 2 producer che producono 10 elementi ciascuno
        Producer p1 = new Producer(buffer, 1, 10);
        Producer p2 = new Producer(buffer, 2, 10);

        // 2 consumer che consumano 10 elementi ciascuno
        Consumer c1 = new Consumer(buffer, 1, 10);
        Consumer c2 = new Consumer(buffer, 2, 10);

        Thread tp1 = new Thread(p1);
        Thread tp2 = new Thread(p2);
        Thread tc1 = new Thread(c1);
        Thread tc2 = new Thread(c2);

        tp1.start();
        tp2.start();
        tc1.start();
        tc2.start();

        tp1.join();
        tp2.join();
        tc1.join();
        tc2.join();

        System.out.println("\n--- Fine ---");
        System.out.println("Buffer finale: " + buffer.size() + " elementi");

        // TODO 4: Verifica che tutti gli elementi siano stati consumati
        // (buffer.size() dovrebbe essere 0)

        // TODO 5: Qual è il vantaggio di avere due Condition separate
        // rispetto a un singolo notifyAll()?
        // HINT: Con notifyAll() sveglieresti TUTTI i thread,
        // anche quelli che non possono procedere.

        // TODO 6: Quando useresti signalAll() invece di signal()?
    }
}

/*
 * CONFRONTO: wait/notify vs Condition
 *
 * Con synchronized + wait/notify:
 * - Un solo "canale" di attesa per oggetto
 * - notifyAll() sveglia TUTTI (inefficiente)
 *
 * synchronized(lock) {
 *     while (pieno) lock.wait();
 *     // produce
 *     lock.notifyAll();  // Sveglia producer E consumer!
 * }
 *
 * Con ReentrantLock + Condition:
 * - Più condizioni per lock
 * - signal() su condizione specifica
 *
 * lock.lock();
 * try {
 *     while (pieno) nonPieno.await();  // Solo producer aspettano qui
 *     // produce
 *     nonVuoto.signal();  // Sveglia SOLO un consumer!
 * } finally {
 *     lock.unlock();
 * }
 *
 * Risultato: meno context switch, più efficienza!
 */
