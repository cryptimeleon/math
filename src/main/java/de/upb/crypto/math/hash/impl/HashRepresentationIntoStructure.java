package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.serialization.*;
import de.upb.crypto.math.serialization.converter.JSONConverter;

import java.math.BigInteger;

/**
 * Utility class to make it easier to hash Representations to a structure.
 * <p>
 * This can be useful, for example, for protocols that employ hashes from a
 * sequence of group elements to a structure. Since the Element -> Representation
 * mapping is injective, it suffices to hash the sequence of Representations.
 */
public class HashRepresentationIntoStructure {
    protected HashIntoStructure hash;

    /**
     * Constructs this helper using baseHash for the actual hashing of
     * the Representation
     */
    public HashRepresentationIntoStructure(HashIntoStructure baseHash) {
        hash = baseHash;
    }

    /**
     * Hashes a Representation by hashing the corresponding JSON string
     */
    public Element apply(Representation repr) {
        JSONConverter converter = new JSONConverter();

        String argument = converter.serialize(repr);
        return hash.hashIntoStructure(argument);
    }

    /**
     * Hashes a list of objects. Types are checked at runtime.
     * Valid types are: Representations, Representables, Strings, Booleans, BigIntegers, and byte arrays.
     * Two Representables with different type but equal Representation will be mapped to the same value
     * (this should not be a problem since the order and types of elements you put here are probably fixed throughout the protocol)
     */
    public Element apply(Object... obj) {
        ListRepresentation repr = new ListRepresentation();
        for (Object o : obj) {
            if (o instanceof Representation)
                repr.put((Representation) o);
            else if (o instanceof Representable)
                repr.put(((Representable) o).getRepresentation());
            else if (o instanceof String)
                repr.put(new StringRepresentation((String) o));
            else if (o instanceof Boolean)
                repr.put(new BooleanRepresentation((Boolean) o));
            else if (o instanceof BigInteger)
                repr.put(new BigIntegerRepresentation((BigInteger) o));
            else if (o instanceof byte[])
                repr.put(new ByteArrayRepresentation((byte[]) o));
            else
                throw new RuntimeException("Don't know how to hash " + o.getClass());
        }

        if (repr.size() <= 1)
            return apply(repr.get(0));

        return apply(repr);
    }
}
