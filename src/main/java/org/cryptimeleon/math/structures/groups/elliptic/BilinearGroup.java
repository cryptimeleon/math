package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.HashIntoGroup;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * A bilinear group containing the associated source and target groups as well as the bilinear pairing function.
 */
public interface BilinearGroup extends StandaloneRepresentable {
    /**
     * The types of a {@link BilinearGroup}.
     * <p>
     * The types have the following properties:
     * <ul>
     * <li>{@code TYPE_1}: {@code G1 = G2}
     * <li>{@code TYPE_2}: {@code G1 != G2} and there exists a computable isomorphism
     *             {@code G2 -> G1}
     * <li>{@code TYPE_3}: {@code G1 != G2} and we assume there is no efficiently computable isomorphism
     *             {@code G2 -> G1}
     * </ul>
     */
    enum Type {
        TYPE_1,
        TYPE_2,
        TYPE_3
    }

    /**
     * Returns the source group G1 associated with this bilinear group.
     */
    Group getG1();

    /**
     * Returns the source group G2 associated with this bilinear group.
     */
    Group getG2();

    /**
     * Returns the target group GT associated with this bilinear group.
     */
    Group getGT();

    /**
     * Returns the {@link BilinearMap} (contains the pairing operation) belonging to this {@code BilinearGroup}.
     */
    BilinearMap getBilinearMap();

    /**
     * Retrieves the homomorphism from G2 to G1 if it exists.
     * @throws UnsupportedOperationException if no such homomorphism exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function that maps byte arrays to G1.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroup getHashIntoG1() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function that maps byte arrays to G2.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroup getHashIntoG2() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function that maps byte arrays to GT.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroup getHashIntoGT() throws UnsupportedOperationException;
    
    Integer getSecurityLevel();
    
    BilinearGroup.Type getPairingType();

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

    /**
     * Retrieves the ring {@link Zn} where {@code n} is chosen to be the order of the pairing source group G1.
     */
    default Zn getZn() {
        return getG1().getZn();
    }

    /**
     * Retrieves the size of G1 (same as size of G2 and target group)
     */
    default BigInteger size() {
        return getG1().size();
    }
}
