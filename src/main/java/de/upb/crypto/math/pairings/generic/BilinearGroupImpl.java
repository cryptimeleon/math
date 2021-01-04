package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * A concrete implementation of a bilinear group.
 * <p>
 * Usually not used directly, but instead wrapped in a {@link BilinearGroup} instance.
 * This allows for either plain evaluation via {@link BasicBilinearGroup} or lazy evaluation via 
 * {@link LazyBilinearGroup}.
 */
public interface BilinearGroupImpl extends StandaloneRepresentable {
    /**
     * Returns the source group G1 implementation associated with this bilinear group implementation.
     */
    GroupImpl getG1();

    /**
     * Returns the source group G2 implementation associated with this bilinear group implementation.
     */
    GroupImpl getG2();

    /**
     * Returns the target group G2 implementation associated with this bilinear group implementation.
     */
    GroupImpl getGT();

    /**
     * Returns the {@link BilinearMapImpl} (contains the pairing operation) belonging to this {@code BilinearGroupImpl}.
     */
    BilinearMapImpl getBilinearMap();

    /**
     * Retrieves the homomorphism implementation from G2 to G1 if it exists.
     * @throws UnsupportedOperationException if no such homomorphism exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    GroupHomomorphismImpl getHomomorphismG2toG1() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function implementation that maps byte arrays to G1.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function implementation that maps byte arrays to G2.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException;

    /**
     * Retrieves a hash function implementation that maps byte arrays to GT.
     * @throws UnsupportedOperationException if no such hash function exists or the bilinear group is not configured
     *                                       to support such functionality
     */
    HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException;
    
    Integer getSecurityLevel();
    
    BilinearGroup.Type getPairingType();
}
