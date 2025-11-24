package org.docencia.hilos;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * @author franciscorodalf
 * @version 1.0
 * 
 * RESPUESTAS A LAS PREGUNTAS:
 * 
 * 1. Diferencia entre schedule(...) y scheduleAtFixedRate(...)
 *  schedule se ejecuta una sola vez cuando pasa el tiempo 
 *  scheduleAtFixedRate es como una alarma que se repite
 * 
 * 2. ¿Cómo se comporta el sistema si la tarea tarda más que el período?
 *  El sistema no lanzaria la siguiente tarea hasta que termines la actual.
 *  Se pierde el ritmo exacto, pero se evita que se acumulen mil tareas a la vez y explote todo.
 * 
 * 3. Probar a cambiar el período y la duración del sleep del main
 *   Si bajas el periodo, salen enemigos mucho más rápido.
 *   El sleep del main es simplemente cuánto tiempo dejas el servidor encendido antes de apagarlo.
 */

public class SpawnsMundoAbierto {

    static class SpawnTarea implements Runnable {

        private final String[] zonas = {
                "Bosque Maldito",
                "Ruinas Antiguas",
                "Pantano Radiactivo",
                "Ciudad Cibernética",
                "Templo Prohibido"
        };

        private final String[] enemigos = {
                "Slime Mutante",
                "Esqueleto Guerrero",
                "Mecha-Dragón",
                "Bandido del Desierto",
                "Lich Supremo"
        };

        @Override
        public void run() {
            String hilo = Thread.currentThread().getName();
            String zona = zonas[(int) (Math.random() * zonas.length)];
            String enemigo = enemigos[(int) (Math.random() * enemigos.length)];

            System.out.println("[" + LocalTime.now() + "][" + hilo + "] Spawn de " +
                    enemigo + " en " + zona);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        System.out.println("=== Iniciando Sistema de Spawns ===");

        scheduler.scheduleAtFixedRate(new SpawnTarea(), 0, 2, TimeUnit.SECONDS);

        Thread.sleep(12000);

        System.out.println("Deteniendo spawns...");
        scheduler.shutdown();
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Forzando parada de spawns.");
            scheduler.shutdownNow();
        }
        System.out.println("Servidor de mundo abierto detenido.");
    }
}
