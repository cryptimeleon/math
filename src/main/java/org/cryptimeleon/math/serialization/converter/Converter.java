package org.cryptimeleon.math.serialization.converter;

import org.cryptimeleon.math.serialization.Representation;

/**
 * Base class for converting between {@link Representation} objects and some serialization format.
 *
 * @param <T> the serialization format type so that the converter converts between T and {@code Representation}
 */
public abstract class Converter<T> {

    /**
     * Serializes the given representation to the type supported by this converter.
     * @param r the representation to serialize
     * @return the serialized object
     */
    public abstract T serialize(Representation r);

    /**
     * Deserializes the given object to its representation.
     * @param s the object to deserialize
     * @return the result of deserialization
     */
    public abstract Representation deserialize(T s);
}
