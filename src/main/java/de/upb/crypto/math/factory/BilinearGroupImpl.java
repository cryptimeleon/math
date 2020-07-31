package de.upb.crypto.math.factory;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.zn.HashIntoZn;
import de.upb.crypto.math.structures.zn.Zn;

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
}
