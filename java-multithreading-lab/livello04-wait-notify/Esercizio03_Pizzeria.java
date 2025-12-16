/**
 * LIVELLO 4 - Esercizio 3: La Pizzeria
 *
 * Obiettivo: Simulare una pizzeria con pizzaioli (producer) e
 *            camerieri (consumer) che condividono un banco pizze.
 *
 * Scenario realistico:
 * - I pizzaioli preparano pizze e le mettono sul banco
 * - Il banco ha spazio limitato (MAX pizze)
 * - I camerieri prendono le pizze dal banco e le servono
 * - Se il banco è pieno, i pizzaioli aspettano
 * - Se il banco è vuoto, i camerieri aspettano
 */
import java.util.LinkedList;
import java.util.Queue;

public class Esercizio03_Pizzeria {

    // Rappresenta una pizza
    static class Pizza {
        private String tipo;
        private String chef;
        private int numero;

        public Pizza(String tipo, String chef, int numero) {
            this.tipo = tipo;
            this.chef = chef;
            this.numero = numero;
        }

        @Override
        public String toString() {
            return tipo + " #" + numero + " (di " + chef + ")";
        }
    }

    // Il banco dove si appoggiano le pizze pronte
    static class BancoPizze {
        private Queue<Pizza> pizze = new LinkedList<>();
        private final int capacitaMax;
        private boolean pizzeriaAperta = true;

        public BancoPizze(int capacita) {
            this.capacitaMax = capacita;
        }

        // Chiamato dai pizzaioli per aggiungere una pizza
        public synchronized void aggiungiPizza(Pizza pizza) throws InterruptedException {
            while (pizze.size() >= capacitaMax && pizzeriaAperta) {
                System.out.println("  [BANCO] Pieno! " + pizza.chef + " aspetta...");
                wait();
            }

            if (!pizzeriaAperta) return;

            pizze.add(pizza);
            System.out.println("+ [BANCO] Aggiunta: " + pizza +
                    " (pizze sul banco: " + pizze.size() + ")");

            notifyAll();
        }

        // Chiamato dai camerieri per prendere una pizza
        public synchronized Pizza prendiPizza(String cameriere) throws InterruptedException {
            // TODO 1: Implementa l'attesa corretta
            // Il cameriere deve aspettare se:
            // - Il banco è vuoto E la pizzeria è ancora aperta
            // Usa while con la condizione corretta

            while (pizze.isEmpty() && pizzeriaAperta) {
                System.out.println("  [BANCO] Vuoto! " + cameriere + " aspetta...");
                wait();
            }

            // Se la pizzeria ha chiuso e non ci sono pizze, ritorna null
            if (pizze.isEmpty()) {
                return null;
            }

            Pizza pizza = pizze.poll();
            System.out.println("- [BANCO] " + cameriere + " prende: " + pizza +
                    " (pizze sul banco: " + pizze.size() + ")");

            // TODO 2: Notifica i pizzaioli che ora c'è spazio
            notifyAll();

            return pizza;
        }

        // Chiamato quando la pizzeria chiude
        public synchronized void chiudi() {
            pizzeriaAperta = false;
            notifyAll(); // Sveglia tutti per far terminare
        }

        public synchronized boolean isAperta() {
            return pizzeriaAperta || !pizze.isEmpty();
        }
    }

    // Pizzaiolo (Producer)
    static class Pizzaiolo implements Runnable {
        private String nome;
        private BancoPizze banco;
        private String[] menu = {"Margherita", "Marinara", "Diavola", "Capricciosa", "4 Stagioni"};
        private int pizzeProdotte = 0;
        private int pizzeDaProdurre;

        public Pizzaiolo(String nome, BancoPizze banco, int pizzeDaProdurre) {
            this.nome = nome;
            this.banco = banco;
            this.pizzeDaProdurre = pizzeDaProdurre;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= pizzeDaProdurre; i++) {
                    // Tempo di preparazione pizza (1-3 secondi)
                    Thread.sleep((long) (Math.random() * 2000 + 1000));

                    // Scegli un tipo di pizza casuale
                    String tipo = menu[(int) (Math.random() * menu.length)];
                    Pizza pizza = new Pizza(tipo, nome, i);

                    banco.aggiungiPizza(pizza);
                    pizzeProdotte++;
                }

                System.out.println("*** " + nome + " ha finito! Pizze prodotte: " + pizzeProdotte);
            } catch (InterruptedException e) {
                System.out.println("*** " + nome + " interrotto!");
            }
        }

        public int getPizzeProdotte() {
            return pizzeProdotte;
        }
    }

    // Cameriere (Consumer)
    static class Cameriere implements Runnable {
        private String nome;
        private BancoPizze banco;
        private int pizzeServite = 0;

        public Cameriere(String nome, BancoPizze banco) {
            this.nome = nome;
            this.banco = banco;
        }

        @Override
        public void run() {
            try {
                while (banco.isAperta()) {
                    Pizza pizza = banco.prendiPizza(nome);

                    if (pizza != null) {
                        // Tempo per servire (500ms - 1.5s)
                        Thread.sleep((long) (Math.random() * 1000 + 500));
                        System.out.println("    [SERVITO] " + nome + " ha servito: " + pizza);
                        pizzeServite++;
                    }
                }

                System.out.println("*** " + nome + " ha finito! Pizze servite: " + pizzeServite);
            } catch (InterruptedException e) {
                System.out.println("*** " + nome + " interrotto!");
            }
        }

        public int getPizzeServite() {
            return pizzeServite;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Esercizio 3: La Pizzeria ===\n");
        System.out.println("La pizzeria apre!\n");

        // Banco con spazio per 3 pizze
        BancoPizze banco = new BancoPizze(3);

        // 2 pizzaioli che producono 5 pizze ciascuno
        Pizzaiolo mario = new Pizzaiolo("Mario", banco, 5);
        Pizzaiolo luigi = new Pizzaiolo("Luigi", banco, 5);

        // 3 camerieri
        Cameriere anna = new Cameriere("Anna", banco);
        Cameriere luca = new Cameriere("Luca", banco);
        Cameriere sara = new Cameriere("Sara", banco);

        Thread tMario = new Thread(mario);
        Thread tLuigi = new Thread(luigi);
        Thread tAnna = new Thread(anna);
        Thread tLuca = new Thread(luca);
        Thread tSara = new Thread(sara);

        // Avvia tutti
        tMario.start();
        tLuigi.start();
        tAnna.start();
        tLuca.start();
        tSara.start();

        // Aspetta che i pizzaioli finiscano
        tMario.join();
        tLuigi.join();

        // I pizzaioli hanno finito, aspetta che le ultime pizze siano servite
        Thread.sleep(2000);

        // Chiudi la pizzeria
        System.out.println("\n*** LA PIZZERIA CHIUDE! ***\n");
        banco.chiudi();

        // Aspetta che i camerieri finiscano
        tAnna.join();
        tLuca.join();
        tSara.join();

        // Riepilogo
        System.out.println("\n=== RIEPILOGO SERATA ===");
        int prodotte = mario.getPizzeProdotte() + luigi.getPizzeProdotte();
        int servite = anna.getPizzeServite() + luca.getPizzeServite() + sara.getPizzeServite();

        System.out.println("Pizze prodotte: " + prodotte);
        System.out.println("Pizze servite:  " + servite);

        if (prodotte == servite) {
            System.out.println("\nTutte le pizze sono state servite!");
        } else {
            System.out.println("\nATTENZIONE: " + (prodotte - servite) + " pizze non servite!");
        }

        // TODO 3: Modifica il numero di pizzaioli e camerieri.
        // - Più pizzaioli che camerieri: cosa succede al banco?
        // - Più camerieri che pizzaioli: cosa succede?


        // TODO 4: Cambia la capacità del banco a 1 o a 10.
        // Come influisce sul flusso di lavoro?


        // TODO 5: Aggiungi un "tipo speciale" di pizza che richiede
        // più tempo di preparazione. Come si comporta il sistema?
    }
}

/*
 * OSSERVAZIONI:
 *
 * 1. Il banco funge da "buffer" tra produttori e consumatori
 *
 * 2. La capacità del banco determina quanto "disaccoppiamento" c'è:
 *    - Banco grande: i pizzaioli raramente aspettano
 *    - Banco piccolo: più sincronizzazione tra pizzaioli e camerieri
 *
 * 3. Il numero relativo di produttori e consumatori influenza le attese:
 *    - Più pizzaioli → banco pieno più spesso
 *    - Più camerieri → banco vuoto più spesso
 *
 * 4. Il meccanismo di chiusura è importante:
 *    - I pizzaioli finiscono quando hanno prodotto le loro pizze
 *    - I camerieri finiscono quando la pizzeria chiude E il banco è vuoto
 */
