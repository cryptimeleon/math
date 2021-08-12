package org.cryptimeleon.math.structures.groups.elliptic.nopairing;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;

/**
 * An implementation of the secp256k1 curve with lazy evaluation of group operations.
 * <p>
 * The curve is defined in Weierstrass short form \(y^2 = x^3 + b\) over a field \(\mathbb{F}_p\).
 * Specific parameters are taken from <a href="https://www.secg.org/sec2-v2.pdf">here</a>.
 */
public class Secp256k1 extends LazyGroup {
    public Secp256k1() {
        super(new Secp256k1Impl());
    }

    public Secp256k1(int exponentiationWindowSize, int precomputationWindowSize) {
        super(new Secp256k1Impl(), exponentiationWindowSize, precomputationWindowSize);
    }

    public Secp256k1(Representation repr) {
        super(repr);
    }

    public Secp256k1(Representation repr, int exponentiationWindowSize, int precomputationWindowSize) {
        super(repr, exponentiationWindowSize, precomputationWindowSize);
    }
}
