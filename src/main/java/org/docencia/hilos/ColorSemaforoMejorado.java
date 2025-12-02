package com.docencia.semaforo;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Variante del semáforo que garantiza estrictamente el orden
 * ROJO → VERDE → AMBAR usando tres semáforos que coordinan el turno
 * correspondiente a cada color.
 */
public class ColorSemaforoMejorado implements Runnable {

    private final Semaphore turnoRojo = new Semaphore(1);
    private final Semaphore turnoVerde = new Semaphore(0);
    private final Semaphore turnoAmbar = new Semaphore(0);
    private final long rojoMillis;
    private final long verdeMillis;
    private final long ambarMillis;
    private final Consumer<LightColor> onColorChange;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Thread runner;

    public ColorSemaforoMejorado() {
        this(ColorSemaforo::printToConsole,
            LightColor.ROJO.getDurationMillis(),
            LightColor.VERDE.getDurationMillis(),
            LightColor.AMBAR.getDurationMillis());
    }

    public ColorSemaforoMejorado(long rojoMillis, long verdeMillis, long ambarMillis) {
        this(ColorSemaforo::printToConsole, rojoMillis, verdeMillis, ambarMillis);
    }

    ColorSemaforoMejorado(Consumer<LightColor> onColorChange, long rojoMillis, long verdeMillis, long ambarMillis) {
        this.onColorChange = Objects.requireNonNull(onColorChange, "onColorChange");
        this.rojoMillis = Math.max(0, rojoMillis);
        this.verdeMillis = Math.max(0, verdeMillis);
        this.ambarMillis = Math.max(0, ambarMillis);
    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            while (running.get()) {
                if (!mostrarColor(LightColor.ROJO, rojoMillis, turnoRojo, turnoVerde)) {
                    break;
                }
                if (!mostrarColor(LightColor.VERDE, verdeMillis, turnoVerde, turnoAmbar)) {
                    break;
                }
                if (!mostrarColor(LightColor.AMBAR, ambarMillis, turnoAmbar, turnoRojo)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
            liberarTodos();
        }
    }

    private boolean mostrarColor(LightColor color, long millis, Semaphore miTurno, Semaphore siguienteTurno)
        throws InterruptedException {
        miTurno.acquire();
        if (!running.get()) {
            siguienteTurno.release();
            return false;
        }
        onColorChange.accept(color);
        if (millis > 0) {
            Thread.sleep(millis);
        }
        siguienteTurno.release();
        return running.get();
    }

    private void liberarTodos() {
        turnoRojo.release();
        turnoVerde.release();
        turnoAmbar.release();
    }

    public void stopSimulation() {
        running.set(false);
        liberarTodos();
        Thread activeRunner = runner;
        if (activeRunner != null) {
            activeRunner.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) throws InterruptedException {
        ColorSemaforoMejorado simulacion = new ColorSemaforoMejorado();
        Thread hilo = new Thread(simulacion, "SemaforoMejorado");
        hilo.start();
        Thread.sleep(Duration.ofSeconds(20).toMillis());
        simulacion.stopSimulation();
        hilo.join();
    }
}
