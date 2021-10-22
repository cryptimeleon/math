package org.cryptimeleon.math.structures.groups.elliptic.nopairing;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.basic.HashIntoBasicGroup;
import org.cryptimeleon.math.structures.groups.lazy.HashIntoLazyGroup;

public class HashIntoSecp256k1Basic extends HashIntoBasicGroup {
    public HashIntoSecp256k1Basic() {
        super(new Secp256k1Impl.HashIntoSecp256k1(), new Secp256k1Basic());
    }

    public HashIntoSecp256k1Basic(Representation repr) {
        super(repr);
    }
}
