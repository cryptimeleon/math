package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;

public class BarretoNaehrigGroup2Element extends BarretoNaehrigSourceGroupElement {
    /**
     * Construct point with given x and y coordinate.
     *
     * @param curve - curve of point
     * @param x     - x coordinate of point
     * @param y     - y coordinate of point
     */
    public BarretoNaehrigGroup2Element(BarretoNaehrigGroup2 curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public BarretoNaehrigGroup2Element(BarretoNaehrigGroup2 curve) {
        super(curve);
    }
}
