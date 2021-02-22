package org.cryptimeleon.math.serialization;

/**
 * Marker interface for classes that possess a constructor with exactly one {@code Representation} argument
 * that is able to reconstruct the object from its {@code Representation} (so without any other help).
 * <p>
 * This means that for any class {@code C} that implements {@code StandaloneRepresentable} and
 * {@code C} instance {@code c}, it holds that {@code new C(c.getRepresentation()).equals(c)}.
 * <p>
 * For this to be possible, the {@code Representation} supplied by {@code getRepresentation()} must contain
 * all information necessary to recreate the {@code StandaloneRepresentable}.
 */
public interface StandaloneRepresentable extends Representable {

}
