package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;

import static de.upb.crypto.math.factory.BilinearGroup.Type.TYPE_1;
import static de.upb.crypto.math.factory.BilinearGroup.Type.TYPE_2;

public class CountingBilinearGroup implements BilinearGroup {

    @Represented
    private BilinearMap bilinearMap;

    @Represented
    private boolean wantHashes;

    @Represented
    private BilinearGroup.Type pairingType;

    public CountingBilinearGroup(BilinearGroup.Type pairingType, BigInteger size, boolean wantHashes, PairingExpGroup pairingExpGroup) {
        bilinearMap = new CountingBilinearMap(pairingType, size, pairingExpGroup);
        this.wantHashes = wantHashes;
        this.pairingType = pairingType;
    }

    public CountingBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }


    @Override
    public Group getG1() {
        return bilinearMap.getG1();
    }

    @Override
    public Group getG2() {
        return bilinearMap.getG2();
    }

    @Override
    public Group getGT() {
        return bilinearMap.getGT();
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilinearMap;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (pairingType != TYPE_1 && pairingType != TYPE_2)
            throw new UnsupportedOperationException("Didn't require existence of a group homomorphism");
        return new CountingIsomorphism((CountingGroup) getG2(), (CountingGroup) getG1());
    }

    @Override
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new CountingHashIntoStructure((CountingGroup) getG1());
    }

    @Override
    public HashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new CountingHashIntoStructure((CountingGroup) getG2());
    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new CountingHashIntoStructure((CountingGroup) getGT());
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
