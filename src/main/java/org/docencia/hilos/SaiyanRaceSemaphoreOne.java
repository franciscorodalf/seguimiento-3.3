package org.docencia.hilos;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Simula la carrera entre Goku y Vegeta utilizando un único semáforo binario
 * para garantizar el acceso exclusivo a la sección crítica donde se actualiza
 * la distancia recorrida. Solo un hilo puede escribir a la vez.
 */
public class SaiyanRaceSemaphoreOne implements Runnable {

    private static final int GOAL = 100;
    private static final Semaphore semaphore = new Semaphore(1, true);
    private static final AtomicBoolean winnerDeclared = new AtomicBoolean(false);
    private static final AtomicReference<String> winnerName = new AtomicReference<>();

    private final String name;
    private final IntSupplier stepSupplier;
    private final LongSupplier pauseSupplier;
    private final Consumer<String> progressLogger;
    private int distance = 0;

    public SaiyanRaceSemaphoreOne(String name) {
        this(
                name,
                () -> ThreadLocalRandom.current().nextInt(1, 11),
                () -> ThreadLocalRandom.current().nextLong(200, 401),
                System.out::println);
    }

    SaiyanRaceSemaphoreOne(
            String name,
            IntSupplier stepSupplier,
            LongSupplier pauseSupplier,
            Consumer<String> progressLogger) {
        this.name = Objects.requireNonNull(name, "name");
        this.stepSupplier = Objects.requireNonNull(stepSupplier, "stepSupplier");
        this.pauseSupplier = Objects.requireNonNull(pauseSupplier, "pauseSupplier");
        this.progressLogger = Objects.requireNonNull(progressLogger, "progressLogger");
    }

    @Override
    public void run() {
        try {
            while (!winnerDeclared.get() && distance < GOAL) {
                semaphore.acquire();
                try {
                    if (winnerDeclared.get()) {
                        break;
                    }
                    int step = Math.max(1, stepSupplier.getAsInt());
                    distance += step;
                    progressLogger
                            .accept(name + " avanzó " + step + " metros. Distancia total: " + distance + " metros.");
                    if (distance >= GOAL && winnerDeclared.compareAndSet(false, true)) {
                        winnerName.set(name);
                        progressLogger.accept(name + " ha ganado la carrera!");
                    }
                } finally {
                    semaphore.release();
                }
                sleepSafely();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sleepSafely() throws InterruptedException {
        long pause = Math.max(0, pauseSupplier.getAsLong());
        if (pause == 0) {
            return;
        }
        Thread.sleep(pause);
    }

    public static void resetRace() {
        winnerDeclared.set(false);
        winnerName.set(null);
    }

    public static String getWinnerName() {
        return winnerName.get();
    }

    public static boolean isWinnerDeclared() {
        return winnerDeclared.get();
    }

    public static void main(String[] args) throws InterruptedException {
        resetRace();
        Thread goku = new Thread(new SaiyanRaceSemaphoreOne("Goku"));
        Thread vegeta = new Thread(new SaiyanRaceSemaphoreOne("Vegeta"));

        goku.start();
        vegeta.start();

        goku.join();
        vegeta.join();
    }
}
