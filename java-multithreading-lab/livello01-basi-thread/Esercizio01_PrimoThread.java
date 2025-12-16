/**
 * LIVELLO 1 - Esercizio 1: Il Tuo Primo Thread
 *
 * Obiettivo: Creare e avviare un semplice thread che stampa un messaggio.
 *
 * Istruzioni:
 * 1. Completa la classe MioThread estendendo Thread
 * 2. Implementa il metodo run() per stampare un messaggio
 * 3. Nel main, crea e avvia il thread
 */
public class Esercizio01_PrimoThread {

    // TODO 1: Completa questa classe facendola estendere Thread
    static class MioThread /* extends ??? */ {

        private String messaggio;

        public MioThread(String messaggio) {
            this.messaggio = messaggio;
        }

        // TODO 2: Sovrascrivi il metodo run()
        // Il metodo deve stampare:
        // - Il nome del thread (usa Thread.currentThread().getName())
        // - Il messaggio passato al costruttore
        // - Un conteggio da 1 a 5 con una pausa di 500ms tra ogni numero

        /*
        @Override
        public void run() {
            // Il tuo codice qui
        }
        */
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 1: Il Tuo Primo Thread ===\n");
        System.out.println("Thread principale: " + Thread.currentThread().getName());

        // TODO 3: Crea un'istanza di MioThread con il messaggio "Ciao dal thread!"
        // MioThread thread = ...

        // TODO 4: Dai un nome al thread usando setName("Worker-1")
        // thread.setName(...);

        // TODO 5: Avvia il thread (ricorda: start(), non run()!)
        // thread.???();

        System.out.println("\nIl main continua la sua esecuzione...");

        // Osserva: il main e il thread eseguono in parallelo!
        for (int i = 1; i <= 3; i++) {
            System.out.println("Main - conteggio: " + i);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nMain terminato.");

        // DOMANDA: Il programma termina subito dopo "Main terminato"?
        // Oppure aspetta che il thread Worker-1 finisca?
    }
}

/*
 * OUTPUT ATTESO (l'ordine può variare!):
 *
 * === Esercizio 1: Il Tuo Primo Thread ===
 *
 * Thread principale: main
 *
 * Il main continua la sua esecuzione...
 * Main - conteggio: 1
 * Worker-1: Ciao dal thread!
 * Worker-1 - conteggio: 1
 * Main - conteggio: 2
 * Worker-1 - conteggio: 2
 * Main - conteggio: 3
 * Worker-1 - conteggio: 3
 *
 * Main terminato.
 * Worker-1 - conteggio: 4
 * Worker-1 - conteggio: 5
 */
