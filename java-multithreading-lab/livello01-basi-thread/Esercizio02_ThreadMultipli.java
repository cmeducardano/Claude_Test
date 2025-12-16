/**
 * LIVELLO 1 - Esercizio 2: Thread Multipli e Interleaving
 *
 * Obiettivo: Osservare come più thread eseguono in modo concorrente
 *            e come l'output si "intreccia" (interleaving).
 *
 * Istruzioni:
 * 1. Completa la classe Corridore implementando Runnable
 * 2. Crea e avvia 4 corridori
 * 3. Esegui il programma più volte e osserva l'output
 */
public class Esercizio02_ThreadMultipli {

    // TODO 1: Fai implementare l'interfaccia Runnable a questa classe
    static class Corridore /* implements ??? */ {

        private String nome;
        private int distanza;

        public Corridore(String nome, int distanza) {
            this.nome = nome;
            this.distanza = distanza;
        }

        // TODO 2: Implementa il metodo run()
        // Il corridore deve:
        // - Stampare "nome si prepara alla partenza"
        // - Per ogni metro (da 1 a distanza):
        //   - Stampare "nome: metro X di distanza"
        //   - Fare una pausa casuale tra 100 e 500 ms
        //     (usa: Thread.sleep((long)(Math.random() * 400 + 100)))
        // - Alla fine stampare "nome HA TAGLIATO IL TRAGUARDO!"

        /*
        @Override
        public void run() {
            // Il tuo codice qui
        }
        */
    }

    public static void main(String[] args) {
        System.out.println("=== Esercizio 2: La Gara dei Thread ===\n");
        System.out.println("Preparazione corridori...\n");

        // TODO 3: Crea 4 istanze di Corridore con nomi diversi
        // Tutti devono correre la stessa distanza (es: 5 metri)
        // Corridore c1 = new Corridore("Rossi", 5);
        // ...

        // TODO 4: Crea un Thread per ogni corridore
        // Thread t1 = new Thread(c1);
        // ...

        System.out.println("3... 2... 1... VIA!\n");

        // TODO 5: Avvia tutti i thread
        // t1.start();
        // ...

        // SUGGERIMENTO: Esegui questo programma 3-4 volte
        // e osserva come cambia l'ordine di arrivo!
    }
}

/*
 * OSSERVAZIONI DA FARE:
 *
 * 1. L'ordine in cui i corridori avanzano è prevedibile? Perché?
 *
 * 2. Il vincitore è sempre lo stesso? Perché?
 *
 * 3. Cosa succederebbe se non ci fosse il Thread.sleep()?
 *
 * 4. Cosa rappresenta il Thread.sleep() nella simulazione reale?
 *    (Pensa: operazioni I/O, calcoli, attesa risorse...)
 */
