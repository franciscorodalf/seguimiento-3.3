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
 * Variante de la carrera en la que se fuerza la alternancia de turnos entre
 * Goku y Vegeta utilizando dos semáforos. Cada corredor solo avanza cuando
 * recibe su turno.
 */
public class SaiyanRaceSemaphore implements Runnable {

    private static final int GOAL = 100;
    private static final Semaphore turnGoku = new Semaphore(1);
    private static final Semaphore turnVegeta = new Semaphore(0);
    private static final AtomicBoolean winnerDeclared = new AtomicBoolean(false);
    private static final AtomicReference<String> winnerName = new AtomicReference<>();

    private final String name;
    private final IntSupplier stepSupplier;
    private final LongSupplier pauseSupplier;
    private final Consumer<String> progressLogger;
    private final boolean isGoku;
    private int distance = 0;

    public SaiyanRaceSemaphore(String name) {
        this(
                name,
                () -> ThreadLocalRandom.current().nextInt(1, 11),
                () -> ThreadLocalRandom.current().nextLong(200, 401),
                System.out::println);
    }

    SaiyanRaceSemaphore(
            String name,
            IntSupplier stepSupplier,
            LongSupplier pauseSupplier,
            Consumer<String> progressLogger) {
        this.name = Objects.requireNonNull(name, "name");
        this.stepSupplier = Objects.requireNonNull(stepSupplier, "stepSupplier");
        this.pauseSupplier = Objects.requireNonNull(pauseSupplier, "pauseSupplier");
        this.progressLogger = Objects.requireNonNull(progressLogger, "progressLogger");
        this.isGoku = "Goku".equalsIgnoreCase(name);
    }

    @Override
    public void run() {
        Semaphore myTurn = isGoku ? turnGoku : turnVegeta;
        Semaphore otherTurn = isGoku ? turnVegeta : turnGoku;
        try {
            while (!winnerDeclared.get() && distance < GOAL) {
                myTurn.acquire();
                if (winnerDeclared.get()) {
                    otherTurn.release();
                    break;
                }
                int step = Math.max(1, stepSupplier.getAsInt());
                distance += step;
                progressLogger.accept(name + " avanzó " + step + " metros. Distancia total: " + distance + " metros.");
                if (distance >= GOAL && winnerDeclared.compareAndSet(false, true)) {
                    winnerName.set(name);
                    progressLogger.accept(name + " ha ganado la carrera!");
                }
                otherTurn.release();
                sleepSafely();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            otherTurn.release();
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
        turnGoku.drainPermits();
        turnVegeta.drainPermits();
        turnGoku.release(1);
        turnVegeta.release(0);
    }

    public static String getWinnerName() {
        return winnerName.get();
    }

    public static boolean isWinnerDeclared() {
        return winnerDeclared.get();
    }

    public static void main(String[] args) throws InterruptedException {
        resetRace();
        Thread goku = new Thread(new SaiyanRaceSemaphore("Goku"));
        Thread vegeta = new Thread(new SaiyanRaceSemaphore("Vegeta"));

        goku.start();
        vegeta.start();

        goku.join();
        vegeta.join();
    }
}
