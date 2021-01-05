package de.upb.crypto.math.pairings.type1.supersingular;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * Offers a less verbose way to instantiate a Supersingular bilinear group which uses lazy evaluation.
 * <p>
 * Essentially just a {@link LazyBilinearGroup} wrapper around {@link SupersingularTateGroupImpl}.
 *
 * @see SupersingularTateGroupImpl
 *
 * @author Raphael Heitjohann
 */
public class SupersingularBilinearGroup extends LazyBilinearGroup {

    public SupersingularBilinearGroup(int securityParameter) {
        super(new SupersingularTateGroupImpl(securityParameter));
    }

    public SupersingularBilinearGroup(Representation repr) {
        super(repr);
    }
}
