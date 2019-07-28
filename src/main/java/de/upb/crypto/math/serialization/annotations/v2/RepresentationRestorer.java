package de.upb.crypto.math.serialization.annotations.v2;

import de.upb.crypto.math.serialization.Representation;

import java.lang.reflect.Type;

/**
 * This class handles (recreation from) representation for some related classes
 * (e.g., a Group handles recreation of group elements from their representation).
 *
 * There is usually no need to override this (it should be a default method in whatever interface this is relevant for, e.g., Group).
 */
public interface RepresentationRestorer {
    /**
     * Takes a Representation and creates an Object of the given type from it.
     * @param type serves as an indicator on what type the Representation is supposed to be unpacked to.
     * @param repr the Representation to restore the object from.
     * @return the recreated object.
     * @throws IllegalArgumentException if unable to return the required type.
     */
    Object recreateFromRepresentation(Type type, Representation repr);
}
