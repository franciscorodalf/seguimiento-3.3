package org.docencia.hilos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
 * @author franciscorodalf
 * @version 1.0
 * 
 * 1. Diferencia entre execute(Runnable) y submit(Callable<V>)
 *  execute lo usas cuando la tarea solo tiene que "hacer cosas" y no devolver nada
 *  submit se usa cuando necesitas que la tarea te devuelva un resultado y como salida te da un Future
 * 
 * 2. Cómo se pueden lanzar muchos cálculos en paralelo y recogerlos todos
 *  se guardan todos los Futures en una lista y luego se recorren todos los futures 
 *  para recoger el resultado de cada uno
 * 
 * 3. Probar a cambiar la probabilidad de crítico
 *    Si pones la probabilidad a 1.0 todos los golpes serán críticos 
 *    Si la pones a 0.0, nunca habrá críticos
 */

public class CalculadoraDanoCritico {

    static class Ataque {
        final String atacante;
        final int danoBase;
        final double probCritico;
        final double multiplicadorCritico;

        Ataque(String atacante, int danoBase, double probCritico, double multiplicadorCritico) {
            this.atacante = atacante;
            this.danoBase = danoBase;
            this.probCritico = probCritico;
            this.multiplicadorCritico = multiplicadorCritico;
        }
    }

    static class TareaCalcularDano implements Callable<Integer> {
        private final Ataque ataque;

        TareaCalcularDano(Ataque ataque) {
            this.ataque = ataque;
        }

        @Override
        public Integer call() throws Exception {
            String hilo = Thread.currentThread().getName();
            System.out.println("[" + hilo + "] Calculando daño para " + ataque.atacante);

            boolean esCritico = Math.random() < ataque.probCritico;
            double multiplicador = esCritico ? ataque.multiplicadorCritico : 1.0;

            Thread.sleep(500 + (int) (Math.random() * 500));

            int danoFinal = (int) (ataque.danoBase * multiplicador);
            System.out.println("[" + hilo + "] " + ataque.atacante +
                    (esCritico ? " ¡CRÍTICO!" : " golpe normal") +
                    " -> daño: " + danoFinal);

            return danoFinal;
        }
    }

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<Integer>> futuros = new ArrayList<>();

        Ataque[] ataques = {
                new Ataque("Mago del Fuego", 120, 0.30, 2.5),
                new Ataque("Guerrero", 150, 0.15, 2.0),
                new Ataque("Pícaro", 90, 0.50, 3.0),
                new Ataque("Arquera Élfica", 110, 0.35, 2.2),
                new Ataque("Invocador", 80, 0.40, 2.8),
                new Ataque("Paladín", 130, 0.10, 1.8),
                new Ataque("Bárbaro", 160, 0.20, 2.1),
                new Ataque("Nigromante", 100, 0.25, 2.3),
        };

        System.out.println("=== Iniciando Calculadora de Daño (Raid DPS) ===");

        for (Ataque ataque : ataques) {
            Future<Integer> futuro = pool.submit(new TareaCalcularDano(ataque));
            futuros.add(futuro);
        }

        int totalRaid = 0;
        for (int i = 0; i < ataques.length; i++) {
            try {
                int dano = futuros.get(i).get();
                totalRaid += dano;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Lectura de resultado interrumpida");
            } catch (ExecutionException e) {
                System.out.println("Error calculando daño: " + e.getCause());
            }
        }

        System.out.println("Daño total de la raid: " + totalRaid);
        pool.shutdown();
    }
}
