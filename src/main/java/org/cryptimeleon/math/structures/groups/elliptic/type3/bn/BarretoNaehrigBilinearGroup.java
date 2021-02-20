package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * Offers a less verbose way to instantiate a Barreto-Naehrig bilinear group which uses lazy evaluation.
 * <p>
 * Essentially just a {@link LazyBilinearGroup} wrapper around {@link BarretoNaehrigBilinearGroupImpl}.
 *
 * @see BarretoNaehrigTargetGroupImpl
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
