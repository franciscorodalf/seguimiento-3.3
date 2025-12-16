package org.docencia.hilos;

/**
 * Representa los colores de un semáforo tradicional junto con la duración
 * predeterminada de cada uno de ellos en milisegundos.
 */
public enum LightColor {
    ROJO(3_000),
    VERDE(3_000),
    AMBAR(1_000);

    private final long durationMillis;

    LightColor(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
