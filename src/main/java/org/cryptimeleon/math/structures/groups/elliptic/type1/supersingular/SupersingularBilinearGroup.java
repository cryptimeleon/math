package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * A type 1 supersingular bilinear group where operations are evaluated lazily.
 * <p>
 * Due to lazy evaluation, this group is more efficient than its non-lazy counterpart
 * {@link SupersingularBasicBilinearGroup}.
 *
 * @see SupersingularBasicBilinearGroup for the version without lazy evaluation
 */
public class SupersingularBilinearGroup extends LazyBilinearGroup {

    public SupersingularBilinearGroup(int securityParameter) {
        super(new SupersingularTateGroupImpl(securityParameter));
    }

    public SupersingularBilinearGroup(Representation repr) {
        super(repr);
    }
}
