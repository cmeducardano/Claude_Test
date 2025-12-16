/**
 * SOLUZIONE - Livello 1, Esercizio 1: Il Tuo Primo Thread
 */
public class Soluzione_L1_PrimoThread {

    // SOLUZIONE TODO 1 e 2: Classe che estende Thread
    static class MioThread extends Thread {

        private String messaggio;

        public MioThread(String messaggio) {
            this.messaggio = messaggio;
        }

        @Override
        public void run() {
            // Stampa il nome del thread e il messaggio
            System.out.println(Thread.currentThread().getName() + ": " + messaggio);

            // Conteggio da 1 a 5 con pausa
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + " - conteggio: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 1: Il Tuo Primo Thread ===\n");
        System.out.println("Thread principale: " + Thread.currentThread().getName());

        // SOLUZIONE TODO 3: Crea istanza
        MioThread thread = new MioThread("Ciao dal thread!");

        // SOLUZIONE TODO 4: Dai un nome
        thread.setName("Worker-1");

        // SOLUZIONE TODO 5: Avvia con start()
        thread.start();

        System.out.println("\nIl main continua la sua esecuzione...");

        for (int i = 1; i <= 3; i++) {
            System.out.println("Main - conteggio: " + i);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nMain terminato.");

        // RISPOSTA ALLA DOMANDA:
        // Il programma NON termina subito dopo "Main terminato".
        // La JVM aspetta che TUTTI i thread (non daemon) finiscano.
        // Quindi il thread Worker-1 continuerà fino al suo termine.
    }
}
