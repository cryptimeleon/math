package de.upb.crypto.math.serialization.annotations.internal;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.RepresentationRestorer;

import java.util.function.Function;

/**
 * Interface for classes that can serialize and deserialize specific types of objects.
 */
public interface RepresentationHandler {
    /**
     * Deserializes the given representation using the given representation restorers.
     * @param repr the representation to deserialize
     * @param getRegisteredRestorer maps representation restorer names to the restorers
     * @return the deserialized object
     */
    Object deserializeFromRepresentation(Representation repr,
                                         Function<String, RepresentationRestorer> getRegisteredRestorer);

    /**
     * Serializes (creates a representation of) the given object.
     * @param object the object to serialize
     * @return the corresponding representation
     */
    Representation serializeToRepresentation(Object object);
}
