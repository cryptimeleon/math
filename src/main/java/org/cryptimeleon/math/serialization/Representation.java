package org.cryptimeleon.math.serialization;

import org.cryptimeleon.math.serialization.converter.Converter;

import java.io.Serializable;

/**
 * Represents an object serialized via our representation framework.
 * <p>
 * The general idea for serialization of objects here is to have a structured intermediate format,
 * so that you can convert between {@code Java object <-> Representation <-> Serialized format}.
 * <p>
 * This conversion is done by a {@link Converter} class (the specific subclass depends on the chosen serialized format).
 * <p>
 * A {@code Representation} is a structured tree of (other) representations.
 * This allows one to universally represent structured objects.
 * Leaves are (boxed) primitives such as {@code BigInteger}, {@code String}, or {@code byte[]}.
 */
public abstract class Representation implements Serializable {
    private static final long serialVersionUID = -2800343381809567714L;

    /**
     * Typecasts this object to a {@link MapRepresentation}.
     */
    public MapRepresentation map() {
        return (MapRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link BigIntegerRepresentation}.
     */
    public BigIntegerRepresentation bigInt() {
        return (BigIntegerRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link ByteArrayRepresentation}.
     */
    public ByteArrayRepresentation bytes() {
        return (ByteArrayRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link ListRepresentation}.
     */
    public ListRepresentation list() {
        return (ListRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link ObjectRepresentation}.
     */
    public ObjectRepresentation obj() {
        return (ObjectRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link RepresentableRepresentation}.
     */
    public RepresentableRepresentation repr() {
        return (RepresentableRepresentation) this;
    }

    /**
     * Typecasts this object to a {@link StringRepresentation}.
     */
    public StringRepresentation str() {
        return (StringRepresentation) this;
    }
}
