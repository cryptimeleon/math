package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.expressions.PowProductExpression;

public class BatchApplyPairingElement extends LazyGroupElement {
    /**
     * A PairingProductExpression whose operands are LazyGroupElements
     */
    protected PairingProductExpression expr;

    public BatchApplyPairingElement(LazyGroup group, PairingProductExpression expr) {
        super(group);
        this.expr = expr;
    }

    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        if (value != null) {
            prod.op(value);
        } else {
            expr.forEach((pair, x) -> {
                PowProductExpression gExpr = new PowProductExpression(group.associatedPairing.g1.baseGroup);
                ((LazyGroupElement) pair.getG()).putProduct(gExpr, null);
                PowProductExpression hExpr = new PowProductExpression(group.associatedPairing.g2.baseGroup);
                ((LazyGroupElement) pair.getH()).putProduct(hExpr, null);
                pairingProd.op(gExpr, hExpr, x);
            });
        }
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        expr.forEach((pair, x) -> {
            PowProductExpression gExpr = new PowProductExpression(group.associatedPairing.g1.baseGroup);
            ((LazyGroupElement) pair.getG()).putProductBasedOnLeafs(gExpr, null);
            PowProductExpression hExpr = new PowProductExpression(group.associatedPairing.g2.baseGroup);
            ((LazyGroupElement) pair.getH()).putProductBasedOnLeafs(hExpr, null);
            pairingProd.op(gExpr, hExpr, x);
        });
    }
}
