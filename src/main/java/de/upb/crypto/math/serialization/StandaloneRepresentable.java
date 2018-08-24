package de.upb.crypto.math.serialization;

/**
 * Marker interface for classes that possess a constructor with Representation argument (and only a single argument)
 * that is able to reconstruct the object from its Representation.
 * This means that for any class C that implements StandaloneRepresentable and object c of C, it holds that
 * new C(c.getRepresentation()).equals(c).
 * <p>
 * For this to be possible, the Representation supplied by getRepresentation() must contain
 * all information necessary to recreate the StandaloneRepresentable.
 */
public interface StandaloneRepresentable extends Representable {

}
