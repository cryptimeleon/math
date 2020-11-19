package de.upb.crypto.math.factory;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.zn.HashIntoZn;
import de.upb.crypto.math.structures.zn.Zn;

/**
 * Parameters for a pairing group setting.
 */
public interface BilinearGroup extends StandaloneRepresentable {
    /**
     * The types of a {@link BilinearGroup}.
     * <p>
     * The types have the following properties:
     * <ul>
     * <li>TYPE_1: G1 = G2
     * <li>TYPE_2: G1 != G2 and there exists a computable isomorphism G2 -> G1
     * <li>TYPE_3: G1 != G2 and we assume there is no efficiently computable isomorphism  G2 -> G1
     * </ul>
     */
    enum Type {
        TYPE_1,
        TYPE_2,
        TYPE_3
    }

    Group getG1();

    Group getG2();

    Group getGT();

    /**
     * Returns the {@link BilinearMap} belonging to this {@code BilinearGroup} containing the pairing operation.
     */
    BilinearMap getBilinearMap();

    GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoG1() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoG2() throws UnsupportedOperationException;

    HashIntoStructure getHashIntoGT() throws UnsupportedOperationException;

    /**
     * Returns a hash into Zn, where n is the common group exponent of G1, G2 and GT.
     * In the case where |G1| = |G2| = p is prime, there the exponent n = p.
     *
     * @throws UnsupportedOperationException if this factory does not support a hash into exponents or G1, G2, GT don't
     *                                       have the same group exponent
     */
    default HashIntoZn getHashIntoZGroupExponent() throws UnsupportedOperationException {
        return new HashIntoZn(getZn());
    }

    default Zn getZn() {
        return getG1().getZn();
    }
}
