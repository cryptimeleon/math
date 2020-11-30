package de.upb.crypto.math.random.interfaces;

import de.upb.crypto.math.random.SimpleRandomGenerator;

/**
 * A static way to obtain random generators. Supplies a {@link SimpleRandomGenerator} by default.
 * <p>
 * Use via {@code RandomSupplier.getRnd()} or {@code RandomSupplier.instance().get()}.
 * <p>
 * If your application should use some non-default source of randomness, you can set it statically via {@code set()}.
 */
public class RandomGeneratorSupplier {
    private static RandomGeneratorSupplier instance = null;

    /**
     * Retrieves the singleton instance of this {@code RandomGeneratorSupplier}.
     */
    public static RandomGeneratorSupplier instance() {
        if (instance != null)
            return instance;

        synchronized (RandomGeneratorSupplier.class) {
            if (instance == null)
                instance = new RandomGeneratorSupplier();
        }

        return instance;
    }

    /**
     * Create a new instance of the random generator offered by this supplier.
     * @return a new {@code RandomGenerator} instance
     */
    public static RandomGenerator getRnd() {
        return instance().get();
    }

    private RandomGenerator rnd = null;

    private RandomGeneratorSupplier() {
        this.rnd = new SimpleRandomGenerator();
    }

    /**
     * Retrieves the currently stored {@code RandomGenerator} instance.
     */
    public RandomGenerator get() {
        return rnd;
    }

    /**
     * Sets the currently stored {@code RandomGenerator} instance.
     * @param rnd the new random generator
     */
    public void set(RandomGenerator rnd) {
        this.rnd = rnd;
    }
}
