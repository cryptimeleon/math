package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;

public class BarretoNaehrigGroup2ElementImpl extends BarretoNaehrigSourceGroupElementImpl {
    /**
     * Construct point on given curve with given x- and y-coordinates.
     *
     * @param curve curve of point
     * @param x x-coordinate of point
     * @param y y-coordinate of point
     */
    public BarretoNaehrigGroup2ElementImpl(BarretoNaehrigGroup2Impl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public BarretoNaehrigGroup2ElementImpl(BarretoNaehrigGroup2Impl curve) {
        super(curve);
    }
}
