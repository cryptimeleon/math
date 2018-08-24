package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;

public class BatchOpGroupElement extends LazyGroupElement {
    /**
     * A PowProductExpression over LazyGroupElements
     */
    protected PowProductExpression expr;

    public BatchOpGroupElement(LazyGroup group, PowProductExpression expr) {
        super(group);
        this.expr = new PowProductExpression(expr);
    }

    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        if (value != null) {
            prod.op(value);
        } else {
            expr.forEach((g, x) -> {
                PowProductExpression innerPowProd = emptyPowProductExpression();
                PairingProductExpression innerPairProd = emptyPairingProductExpression();

                ((LazyGroupElement) g).putProduct(innerPowProd, innerPairProd);
                innerPowProd.pow(x);
                prod.op(innerPowProd);

                if (pairingProd != null) {
                    innerPairProd.pow(x);
                    pairingProd.op(innerPairProd);
                }
            });
        }
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        expr.forEach((g, x) -> {
            PowProductExpression innerPowProd = emptyPowProductExpression();
            PairingProductExpression innerPairProd = emptyPairingProductExpression();

            ((LazyGroupElement) g).putProductBasedOnLeafs(innerPowProd, innerPairProd);
            innerPowProd.pow(x);
            prod.op(innerPowProd);

            if (pairingProd != null) {
                innerPairProd.pow(x);
                pairingProd.op(innerPairProd);
            }
        });
    }
}
