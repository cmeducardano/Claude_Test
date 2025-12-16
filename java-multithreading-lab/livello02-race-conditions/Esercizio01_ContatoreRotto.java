/**
 * LIVELLO 2 - Esercizio 1: Il Contatore Rotto
 *
 * Obiettivo: Osservare una race condition in azione.
 *
 * Questo programma crea due thread che incrementano lo stesso contatore
 * 10.000 volte ciascuno. Il risultato atteso è 20.000, ma...
 *
 * ESEGUI QUESTO PROGRAMMA 5-10 VOLTE E ANNOTA I RISULTATI!
 */
public class Esercizio01_ContatoreRotto {

    // Contatore condiviso tra i thread
    static class Contatore {
        private int valore = 0;

        // Questo metodo NON è thread-safe!
        public void incrementa() {
            valore++;
        }

        public int getValore() {
            return valore;
        }
    }

    // Task che incrementa il contatore N volte
    static class TaskIncremento implements Runnable {
        private Contatore contatore;
        private int numeroIncrementi;
        private String nome;

        public TaskIncremento(Contatore contatore, int numeroIncrementi, String nome) {
            this.contatore = contatore;
            this.numeroIncrementi = numeroIncrementi;
            this.nome = nome;
        }

        @Override
        public void run() {
            System.out.println(nome + " inizia gli incrementi...");

            for (int i = 0; i < numeroIncrementi; i++) {
                contatore.incrementa();
            }

            System.out.println(nome + " ha completato " + numeroIncrementi + " incrementi.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 1: Il Contatore Rotto ===\n");

        final int INCREMENTI_PER_THREAD = 10000;
        Contatore contatore = new Contatore();

        // Creiamo due thread che condividono lo stesso contatore
        Thread t1 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD, "Thread-A"));
        Thread t2 = new Thread(new TaskIncremento(contatore, INCREMENTI_PER_THREAD, "Thread-B"));

        System.out.println("Valore iniziale: " + contatore.getValore());
        System.out.println("Valore atteso finale: " + (INCREMENTI_PER_THREAD * 2));
        System.out.println("\nAvvio dei thread...\n");

        // Avviamo i thread
        t1.start();
        t2.start();

        // Aspettiamo che entrambi finiscano
        t1.join();
        t2.join();

        // Verifichiamo il risultato
        int risultato = contatore.getValore();
        int atteso = INCREMENTI_PER_THREAD * 2;

        System.out.println("\n========== RISULTATO ==========");
        System.out.println("Valore finale:    " + risultato);
        System.out.println("Valore atteso:    " + atteso);
        System.out.println("Differenza:       " + (atteso - risultato));
        System.out.println("================================\n");

        if (risultato == atteso) {
            System.out.println("Wow, sei stato fortunato! Ma esegui di nuovo...");
        } else {
            System.out.println("RACE CONDITION RILEVATA!");
            System.out.println("Sono stati persi " + (atteso - risultato) + " incrementi.");
        }

        // TODO 1: Esegui questo programma almeno 5 volte
        // Annota qui sotto i risultati che ottieni:
        //
        // Esecuzione 1: _______
        // Esecuzione 2: _______
        // Esecuzione 3: _______
        // Esecuzione 4: _______
        // Esecuzione 5: _______
        //
        // I risultati sono sempre uguali? Perché?


        // TODO 2: Prova ad aumentare INCREMENTI_PER_THREAD a 100000 o 1000000
        // Il problema diventa più evidente? Perché?


        // TODO 3: Nel metodo incrementa(), aggiungi un Thread.sleep(0) dopo valore++
        // (anche sleep(0) causa un cambio di contesto!)
        // Cosa succede ai risultati?
    }
}

/*
 * SPIEGAZIONE DELLA RACE CONDITION:
 *
 * L'operazione valore++ non è ATOMICA. Si scompone in:
 *
 * 1. int temp = valore;    // LETTURA
 * 2. temp = temp + 1;      // INCREMENTO
 * 3. valore = temp;        // SCRITTURA
 *
 * Se due thread eseguono queste operazioni in modo "intrecciato":
 *
 * Thread A                    Thread B
 * ────────────────────        ────────────────────
 * temp_A = valore (0)
 *                             temp_B = valore (0)
 * temp_A = 0 + 1 = 1
 *                             temp_B = 0 + 1 = 1
 * valore = 1
 *                             valore = 1
 *
 * Risultato: valore = 1, ma dovrebbe essere 2!
 *
 * Nel prossimo livello vedremo come risolvere questo problema.
 */
