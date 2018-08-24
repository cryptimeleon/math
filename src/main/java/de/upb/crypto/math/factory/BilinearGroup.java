package de.upb.crypto.math.factory;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

/**
 * Parameters for a pairing group setting.
 */
public interface BilinearGroup extends StandaloneRepresentable {
    /**
     * The three types of a {@link BilinearGroup}.
     * <p>
     * The types have the following properties:
     * TYPE_1: G1 = G2
     * TYPE_2: G1 != G2 and there exists a computable isomorphism G2 -> G1
     * TYPE_3: G1 != G2 and there exists no efficiently computable isomorphism  G2 -> G1
     */
    enum Type {
        TYPE_1,
        TYPE_2,
        TYPE_3
    }

    Group getG1();

    Group getG2();

    Group getGT();

    BilinearMap getBilinearMap();

    GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoG1() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoG2() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoGT() throws UnsupportedOperationException;

    /**
     * Returns a hash into Zn, where n is the common group exponent of G1, G2 and GT.
     * In the case where |G1| = |G2| = p is prime, there the exponent n = p.
     *
     * @throws UnsupportedOperationException if this factory does not support a hash into exponents or G1,G2,GT don't
     *                                       have the same group exponent
     */
    HashIntoStructure getHashIntoZGroupExponent() throws UnsupportedOperationException;
}
