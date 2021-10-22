package org.cryptimeleon.math.structures.groups.elliptic.nopairing;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.lazy.HashIntoLazyGroup;

public class HashIntoSecp256k1 extends HashIntoLazyGroup {
    public HashIntoSecp256k1() {
        super(new Secp256k1Impl.HashIntoSecp256k1(), new Secp256k1());
    }

    public HashIntoSecp256k1(Representation repr) {
        super(repr);
    }
}
