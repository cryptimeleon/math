package de.upb.crypto.math.pairings.type3.bn;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

/**
 * Offers a less verbose way to instantiate a Barreto-Naehrig bilinear group which uses lazy evaluation.
 * <p>
 * Essentially just a {@link LazyBilinearGroup} wrapper around {@link BarretoNaehrigBilinearGroupImpl}.
 *
 * @see BarretoNaehrigTargetGroupImpl
 *
 * @author Raphael Heitjohann
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
