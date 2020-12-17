package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

/**
 * Parameters for a pairing group setting.
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
    
    Integer getSecurityLevel();
    
    BilinearGroup.Type getPairingType();
}
