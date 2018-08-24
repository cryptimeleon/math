package de.upb.crypto.math.serialization;

import java.io.Serializable;

/**
 * Base class for the representation framework.
 * The general idea for serialization of objects here is to have a structured intermediate format "Representation",
 * so that you can convert between:
 * Actual Java object <-> Representation <-> Serialized format.
 * This conversion is done by a Converter class (the specific subclass depends on the chosen serialized format).
 * <p>
 * A Representation is a structured tree of (other) Representations. This allows to universally represent structured
 * objects.
 * Leaves are primitives such as BigInteger, Strings, or byte arrays.
 */
public abstract class Representation implements Serializable {
    private static final long serialVersionUID = -2800343381809567714L;

    /**
     * Shorthand for typecast
     */
    public MapRepresentation map() {
        return (MapRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public BigIntegerRepresentation bigInt() {
        return (BigIntegerRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public BooleanRepresentation bool() {
        return (BooleanRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public ByteArrayRepresentation bytes() {
        return (ByteArrayRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public ListRepresentation list() {
        return (ListRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public ObjectRepresentation obj() {
        return (ObjectRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public RepresentableRepresentation repr() {
        return (RepresentableRepresentation) this;
    }

    /**
     * Shorthand for typecast
     */
    public StringRepresentation str() {
        return (StringRepresentation) this;
    }
}
