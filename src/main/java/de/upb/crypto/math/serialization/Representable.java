package de.upb.crypto.math.serialization;

/**
 * Common interface for all objects that can be represented (and thereby serialized) using the Representation framework.
 * This allows us to serialize objects in an interoperable way (in contrast to Java's Serializable), e.g. to use
 * outside of Java programs.
 * <p>
 * A special case of Representable is StandaloneRepresentable, which allows recreating the Representable through a
 * standard interface
 * (NOT-standalone Representables may need some specific non-standard means of recreating, e.g., Elements of
 * Structures via structure.getElement(repr)).
 */
public interface Representable {
    /**
     * A String, uniquely identifying the kind of object encoded in the representation (not the object itself).
     * By convention, this is the fully qualified class name of the object.
     * If you change this, it will break the default handler in RepresentationToJavaObjectHelper and you will have to
     * add your special case to its code.
     */
    default String getRepresentedTypeName() {
        return this.getClass().getName();
    }

    /**
     * The representation of this object. Used for serialization
     *
     * @return a Representation or null if the representedTypeName suffices to instantiate an equal object again
     * @see Representation
     */
    Representation getRepresentation();


}
