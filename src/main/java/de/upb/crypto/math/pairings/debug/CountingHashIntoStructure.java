package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.HashIntoZn;

public class CountingHashIntoStructure implements HashIntoStructure {

    private CountingGroup group;
    private HashIntoZn hash;

    public CountingHashIntoStructure(CountingGroup group) {
        this.group = group;
        this.hash = new HashIntoZn(group.size());
    }

    public CountingHashIntoStructure(Representation repr) {
        this(new CountingGroup(repr));
    }

    @Override
    public Element hashIntoStructure(byte[] x) {
        return group.wrap(hash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return group.getRepresentation();
    }
}
