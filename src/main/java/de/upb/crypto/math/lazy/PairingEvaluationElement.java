package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;

/**
 * Holds the value of a pairing of two LazyGroupElements.
 * <p>
 * This counts as a "leaf" since it is the smallest unit that {@link PairingProductExpression}s handle.
 */
public class PairingEvaluationElement extends LeafElement {
    protected LazyGroupElement lhs, rhs;

    public PairingEvaluationElement(LazyGroup group, LazyGroupElement lhs, LazyGroupElement rhs) {
        super(group);
        this.lhs = lhs;
        this.rhs = rhs;
    }


    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        if (value != null) {
            prod.op(value);
        } else {
            PowProductExpression lhsExpr = new PowProductExpression(group.associatedPairing.g1.baseGroup);
            PowProductExpression rhsExpr = new PowProductExpression(group.associatedPairing.g2.baseGroup);
            lhs.putProduct(lhsExpr, null); //these live in a source group, so no pairingProd parameter needed
            rhs.putProduct(rhsExpr, null);

            pairingProd.op(lhsExpr, rhsExpr);
        }
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        PowProductExpression lhsExpr = new PowProductExpression(group.associatedPairing.g1.baseGroup);
        PowProductExpression rhsExpr = new PowProductExpression(group.associatedPairing.g2.baseGroup);
        lhs.putProductBasedOnLeafs(lhsExpr, null); //these live in a source group, so no pairingProd parameter needed
        rhs.putProductBasedOnLeafs(rhsExpr, null);

        pairingProd.op(lhsExpr, rhsExpr);
    }
}
