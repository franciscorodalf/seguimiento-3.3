package org.docencia.hilos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*
 * @author franciscorodalf
 * @version 1.0
 * 
 * RESPUESTAS A LAS PREGUNTAS:
 * 
 * 1. Solo se usan 3 hilos para atender a todos los jugadores. ¿Qué está sucediendo?
 *  El ExecutorService mantiene un pool de solo 3 hilos activos.
 *  Cuando llegan más de 3 tareas, las que no pueden ser atendidas inmediatamente se ponen en una cola de espera
 *  A medida que un hilo termina una tarea, toma la siguiente de la cola.
 * 
 * 2. Reutilización de hilos. ¿Qué significa esto?
 *  Significa que no se crean ni se destruyen hilos por cada jugador
 *  Los hilos del pool se mantienen vivos.
 *  Cuando terminan una tarea run(), no mueren y se quedan esperando o toman la siguiente tarea.
 * 
 * 3. ¿Qué pasa si cambias el tamaño del pool a 1? ¿Y a 10?
 *  Pool = 1: Las tareas se ejecutan secuencialmente, una detrás de otra. El tiempo total será la suma de todos los tiempos.
 *  Pool = 10: Como hay 10 jugadores, si el pool es de 10, todas las tareas podrían arrancar casi simultáneamente,
 *  reduciendo el tiempo total al de la tarea más lenta, pero consumiendo más recursos del sistema.
 */

public class ServidorMazmorras {

    static class PeticionMazmorra implements Runnable {
        private final String nombreJugador;
        private final String mazmorra;

        public PeticionMazmorra(String nombreJugador, String mazmorra) {
            this.nombreJugador = nombreJugador;
            this.mazmorra = mazmorra;
        }

        @Override
        public void run() {
            String hilo = Thread.currentThread().getName();
            System.out.println("[" + hilo + "] Preparando mazmorra '" + mazmorra +
                    "' para el jugador " + nombreJugador);
            try {
                Thread.sleep(1000 + (int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                System.out.println("[" + hilo + "] Petición de " + nombreJugador + " interrumpida");
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("[" + hilo + "] Mazmorra '" + mazmorra +
                    "' lista para " + nombreJugador + " 🎮");
        }
    }

    public static void main(String[] args) {

        ExecutorService gmBots = Executors.newFixedThreadPool(3);

        String[] jugadores = {
                "Link", "Zelda", "Geralt", "Yennefer", "Gandalf",
                "Frodo", "Aragorn", "Leia", "Luke", "DarthVader"
        };
        String[] mazmorras = {
                "Catacumbas de Hyrule", "Torre Oscura", "Moria",
                "Estrella de la Muerte", "Nido de Dragón"
        };

        System.out.println("=== Iniciando Servidor de Mazmorras ===");

        for (int i = 0; i < jugadores.length; i++) {
            String jugador = jugadores[i];
            String dungeon = mazmorras[i % mazmorras.length];
            gmBots.execute(new PeticionMazmorra(jugador, dungeon));
        }

        gmBots.shutdown();
        System.out.println("Servidor: todas las peticiones han sido enviadas a los GM bots.");

    }
}
