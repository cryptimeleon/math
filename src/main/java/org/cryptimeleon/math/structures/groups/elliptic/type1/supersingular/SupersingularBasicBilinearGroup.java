package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.basic.BasicBilinearGroup;

/**
 * A type 1 supersingular bilinear group where operations are evaluated naively, that is, not lazily.
 *
 * @see SupersingularBilinearGroup for the version with lazy evaluation
 */
public class SupersingularBasicBilinearGroup extends BasicBilinearGroup {

    public SupersingularBasicBilinearGroup(int securityParameter) {
        super(new SupersingularTateGroupImpl(securityParameter));
    }

    public SupersingularBasicBilinearGroup(Representation repr) {
        super(repr);
    }
}
