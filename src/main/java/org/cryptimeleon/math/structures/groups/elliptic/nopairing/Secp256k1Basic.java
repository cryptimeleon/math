package org.cryptimeleon.math.structures.groups.elliptic.nopairing;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.basic.BasicGroup;

/**
 * An implementation of the secp256k1 curve with naive (non-lazy) evaluation of group operations.
 * <p>
 * The curve is defined in Weierstrass short form \(y^2 = x^3 + b\) over a field \(\mathbb{F}_p\).
 * Specific parameters are taken from <a href="https://www.secg.org/sec2-v2.pdf">here</a>.
 */
public class Secp256k1Basic extends BasicGroup {
    public Secp256k1Basic() {
        super(new Secp256k1Impl());
    }

    public Secp256k1Basic(Representation repr) {
        super(repr);
    }
}
