package com.docencia.semaforo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Representa a un estudiante que necesita utilizar uno de los equipos del
 * laboratorio. El acceso está controlado mediante un semáforo con 4 permisos.
 */
public class Estudiante extends Thread {

    private static final long MIN_USO_MS = 3_000;
    private static final long MAX_USO_MS = 5_000;

    protected final String nombre;
    private final Semaphore semaforo;
    private final LongSupplier usoMillisSupplier;
    private final Consumer<String> logger;
    private final Consumer<Boolean> activityMonitor;

    public Estudiante(String nombre, Semaphore semaforo) {
        this(nombre, semaforo, Estudiante::duracionAleatoria, System.out::println, estado -> {});
    }

    Estudiante(
        String nombre,
        Semaphore semaforo,
        LongSupplier usoMillisSupplier,
        Consumer<String> logger,
        Consumer<Boolean> activityMonitor
    ) {
        super("Estudiante-" + nombre);
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.semaforo = Objects.requireNonNull(semaforo, "semaforo");
        this.usoMillisSupplier = Objects.requireNonNull(usoMillisSupplier, "usoMillisSupplier");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.activityMonitor = Objects.requireNonNull(activityMonitor, "activityMonitor");
    }

    @Override
    public void run() {
        boolean acquired = false;
        int equipoId = -1;
        try {
            semaforo.acquire();
            acquired = true;
            equipoId = semaforo.availablePermits() + 1;
            activityMonitor.accept(true);
            logInicio(equipoId);
            long uso = Math.max(0, usoMillisSupplier.getAsLong());
            if (uso > 0) {
                Thread.sleep(uso);
            }
            logFin(equipoId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquired) {
                activityMonitor.accept(false);
                semaforo.release();
            }
        }
    }

    protected void logInicio(int equipoId) {
        publish("El estudiante " + nombre + " ha comenzado a utilizar el equipo");
    }

    protected void logFin(int equipoId) {
        publish("El estudiante " + nombre + " ha finalizado con el equipo");
    }

    protected void publish(String message) {
        logger.accept(message);
    }

    private static long duracionAleatoria() {
        return ThreadLocalRandom.current().nextLong(MIN_USO_MS, MAX_USO_MS + 1);
    }

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaforo = new Semaphore(4);
        List<Thread> estudiantes = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Estudiante estudiante = new Estudiante(String.valueOf(i), semaforo);
            estudiantes.add(estudiante);
            estudiante.start();
        }
        for (Thread estudiante : estudiantes) {
            estudiante.join();
        }
    }
}
