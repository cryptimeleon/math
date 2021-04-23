package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.basic.BasicBilinearGroup;

/**
 * A type 1 supersingular bilinear group where operations are evaluated naively, that is, not lazily.
 *
 * @see BarretoNaehrigBilinearGroup for the version with lazy evaluation
 */
public class BarretoNaehrigBasicBilinearGroup extends BasicBilinearGroup {

    public BarretoNaehrigBasicBilinearGroup(int securityParameter) {
        super(new BarretoNaehrigBilinearGroupImpl(securityParameter));
    }

    public BarretoNaehrigBasicBilinearGroup(String spec) {
        super(new BarretoNaehrigBilinearGroupImpl(spec));
    }

    public BarretoNaehrigBasicBilinearGroup(BarretoNaehrigParameterSpec spec) {
        super(new BarretoNaehrigBilinearGroupImpl(spec));
    }

    public BarretoNaehrigBasicBilinearGroup(Representation repr) {
        super(repr);
    }
}
