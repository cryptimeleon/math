package de.upb.crypto.math.random.interfaces;

import de.upb.crypto.math.random.SimpleRandomGenerator.SimpleRandomGenerator;

/**
 * A static way to obtain random generators.
 * Usage: RandomSupplier.getRnd() or RandomSupplier.instance().get()
 * <p>
 * If your application should use some non-default source of randomness, you can set it statically via set().
 */
public class RandomGeneratorSupplier {
    private static RandomGeneratorSupplier instance = null;

    public static RandomGeneratorSupplier instance() {
        if (instance != null)
            return instance;

        synchronized (RandomGeneratorSupplier.class) {
            if (instance == null)
                instance = new RandomGeneratorSupplier();
        }

        return instance;
    }

    public static RandomGenerator getRnd() {
        return instance().get();
    }

    private RandomGenerator rnd = null;

    private RandomGeneratorSupplier() {
        this.rnd = new SimpleRandomGenerator();
    }

    public RandomGenerator get() {
        return rnd;
    }

    public void set(RandomGenerator rnd) {
        this.rnd = rnd;
    }
}
