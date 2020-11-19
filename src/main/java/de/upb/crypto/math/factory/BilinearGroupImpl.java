package de.upb.crypto.math.factory;

import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
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
    GroupImpl getG1();

    GroupImpl getG2();

    GroupImpl getGT();

    BilinearMapImpl getBilinearMap();

    GroupHomomorphismImpl getHomomorphismG2toG1() throws UnsupportedOperationException;

    HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException;

    HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException;

    HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException;
}
