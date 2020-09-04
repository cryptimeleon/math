package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;

import java.math.BigInteger;

public class CountingBilinearGroup implements BilinearGroup {

    @Represented
    private LazyBilinearGroup totalBilGroup;

    @Represented
    private LazyBilinearGroup expMultiExpBilGroup;

    private CountingBilinearMap bilMap;

    public CountingBilinearGroup(BilinearGroup.Type pairingType, BigInteger size, boolean wantHashes) {
        totalBilGroup = new LazyBilinearGroup(new DebugBilinearGroupImpl(pairingType, size, wantHashes, false, false));
        expMultiExpBilGroup = new LazyBilinearGroup(new DebugBilinearGroupImpl(pairingType, size, wantHashes, true, true));
        init();
    }

    public CountingBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }


    private void init() {
        bilMap = new CountingBilinearMap(totalBilGroup.getBilinearMap(), expMultiExpBilGroup.getBilinearMap());
    }

    @Override
    public Group getG1() {
        return new CountingGroup(totalBilGroup.getG1(), expMultiExpBilGroup.getG1());
    }

    @Override
    public Group getG2() {
        return new CountingGroup(totalBilGroup.getG2(), expMultiExpBilGroup.getG2());
    }

    @Override
    public Group getGT() {
        return new CountingGroup(totalBilGroup.getGT(), expMultiExpBilGroup.getGT());
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilMap;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return new CountingHomomorphism(
                totalBilGroup.getHomomorphismG2toG1(),
                expMultiExpBilGroup.getHomomorphismG2toG1()
        );
    }

    @Override
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoG1(), expMultiExpBilGroup.getHashIntoG1());
    }

    @Override
    public HashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoG2(), expMultiExpBilGroup.getHashIntoG2());

    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoGT(), expMultiExpBilGroup.getHashIntoGT());

    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
