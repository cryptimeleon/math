package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * A type 1 supersingular bilinear group where operations are evaluated lazily.
 *
 * @see BarretoNaehrigBasicBilinearGroup for the version without lazy evaluation
 */
public class BarretoNaehrigBilinearGroup extends LazyBilinearGroup {

    public BarretoNaehrigBilinearGroup(int securityParameter) {
        super(new BarretoNaehrigBilinearGroupImpl(securityParameter));
    }

    public BarretoNaehrigBilinearGroup(String spec) {
        super(new BarretoNaehrigBilinearGroupImpl(spec));
    }

    public BarretoNaehrigBilinearGroup(BarretoNaehrigParameterSpec spec) {
        super(new BarretoNaehrigBilinearGroupImpl(spec));
    }

    public BarretoNaehrigBilinearGroup(Representation repr) {
        super(repr);
    }
}
