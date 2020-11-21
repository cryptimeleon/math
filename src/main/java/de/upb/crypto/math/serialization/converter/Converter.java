package de.upb.crypto.math.serialization.converter;

import de.upb.crypto.math.serialization.Representation;

/**
 * Base class for converting between {@link Representation} objects and some serialization format.
 *
 * @param <T> the serialization format type so that the converter converts between T and {@code Representation}
 */
public abstract class Converter<T> {
    public abstract T serialize(Representation r);

    public abstract Representation deserialize(T s);
}
