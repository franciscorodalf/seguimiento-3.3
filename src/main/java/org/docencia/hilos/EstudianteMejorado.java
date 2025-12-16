package org.docencia.hilos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Versión mejorada que indica qué equipo concreto se ha utilizado. El número
 * de equipo se obtiene a partir de los permisos disponibles en el semáforo.
 */
public class EstudianteMejorado extends Estudiante {

    public EstudianteMejorado(String nombre, Semaphore semaforo) {
        super(nombre, semaforo);
    }

    EstudianteMejorado(
        String nombre,
        Semaphore semaforo,
        LongSupplier usoMillisSupplier,
        Consumer<String> logger,
        Consumer<Boolean> activityMonitor
    ) {
        super(nombre, semaforo, usoMillisSupplier, logger, activityMonitor);
    }

    @Override
    protected void logInicio(int equipoId) {
        publish("El estudiante " + nombre + " ha comenzado a utilizar el equipo " + equipoId);
    }

    @Override
    protected void logFin(int equipoId) {
        publish("El estudiante " + nombre + " ha finalizado con el equipo " + equipoId);
    }

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaforo = new Semaphore(4);
        List<Thread> estudiantes = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            EstudianteMejorado estudiante = new EstudianteMejorado(String.valueOf(i), semaforo);
            estudiantes.add(estudiante);
            estudiante.start();
        }
        for (Thread estudiante : estudiantes) {
            estudiante.join();
        }
    }
}
