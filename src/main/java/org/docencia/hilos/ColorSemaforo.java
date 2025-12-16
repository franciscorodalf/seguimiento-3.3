package org.docencia.hilos;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Simulación básica de un semáforo de tráfico que alterna los colores ROJO,
 * VERDE y AMBAR utilizando un único semáforo para sincronizar el cambio de
 * estado.
 */
public class ColorSemaforo implements Runnable {

    private final Semaphore semaphore = new Semaphore(1, true);
    private final long rojoMillis;
    private final long verdeMillis;
    private final long ambarMillis;
    private final Consumer<LightColor> onColorChange;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile LightColor currentColor = LightColor.ROJO;
    private volatile Thread runner;

    public ColorSemaforo() {
        this(ColorSemaforo::printToConsole,
            LightColor.ROJO.getDurationMillis(),
            LightColor.VERDE.getDurationMillis(),
            LightColor.AMBAR.getDurationMillis());
    }

    public ColorSemaforo(long rojoMillis, long verdeMillis, long ambarMillis) {
        this(ColorSemaforo::printToConsole, rojoMillis, verdeMillis, ambarMillis);
    }

    ColorSemaforo(Consumer<LightColor> onColorChange, long rojoMillis, long verdeMillis, long ambarMillis) {
        this.onColorChange = Objects.requireNonNull(onColorChange, "onColorChange");
        this.rojoMillis = Math.max(0, rojoMillis);
        this.verdeMillis = Math.max(0, verdeMillis);
        this.ambarMillis = Math.max(0, ambarMillis);
    }

    static void printToConsole(LightColor color) {
        System.out.println("Color actual: " + color);
    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            while (running.get()) {
                semaphore.acquire();
                try {
                    onColorChange.accept(currentColor);
                } finally {
                    semaphore.release();
                }
                sleepCurrentColor();
                avanzarColor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }

    private void sleepCurrentColor() throws InterruptedException {
        long millis = switch (currentColor) {
            case ROJO -> rojoMillis;
            case VERDE -> verdeMillis;
            case AMBAR -> ambarMillis;
        };
        if (millis <= 0) {
            return;
        }
        Thread.sleep(millis);
    }

    private void avanzarColor() {
        if (!running.get()) {
            return;
        }
        currentColor = switch (currentColor) {
            case ROJO -> LightColor.VERDE;
            case VERDE -> LightColor.AMBAR;
            case AMBAR -> LightColor.ROJO;
        };
    }

    public void stopSimulation() {
        running.set(false);
        Thread activeRunner = runner;
        if (activeRunner != null) {
            activeRunner.interrupt();
        }
    }

    public LightColor getCurrentColor() {
        return currentColor;
    }

    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) throws InterruptedException {
        ColorSemaforo colorSemaforo = new ColorSemaforo();
        Thread hilo = new Thread(colorSemaforo, "SimulacionSemaforo");
        hilo.start();
        Thread.sleep(Duration.ofSeconds(20).toMillis());
        colorSemaforo.stopSimulation();
        hilo.join();
    }
}
