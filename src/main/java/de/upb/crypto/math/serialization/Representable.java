package de.upb.crypto.math.serialization;

/**
 * Common interface for all objects that can be represented (and thereby serialized) using the Representation framework.
 * This allows us to serialize objects in an interoperable way (in contrast to Java's Serializable), e.g. to use
 * outside of Java programs.
 * <p>
 * A special case of Representable is StandaloneRepresentable, which allows recreating the Representable through a
 * standard interface
 * (NOT-standalone Representables may need some specific non-standard means of recreating, e.g., Elements of
 * Structures via structure.getElement(repr)). This also ensures that the resulting object belongs to the expected group.
 */
public interface Representable {
    /**
     * A String, uniquely identifying the kind of object encoded in the representation (not the object itself).
     * By convention, this is the fully qualified class name of the object.
     * @Deprecated not needed: has never been overwritten and is not used anywhere anymore. Will be removed in next major release.
     */
    @Deprecated
    default String getRepresentedTypeName() {
        return this.getClass().getName();
    }

    /**
     * The representation of this object. Used for serialization.
     * A convenient way to implement this is using @link {@link de.upb.crypto.math.serialization.annotations.v2.ReprUtil}
     *
     * @return a Representation or null if an equal object can be recreated without any information.
     * @see Representation
     */
    Representation getRepresentation();
}
