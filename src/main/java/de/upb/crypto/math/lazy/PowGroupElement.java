package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.expressions.PowProductExpression;

import java.math.BigInteger;

public class PowGroupElement extends LazyGroupElement {
    protected LazyGroupElement g;
    protected BigInteger exponent;

    public PowGroupElement(LazyGroup group, LazyGroupElement g, BigInteger exponent) {
        super(group);
        this.g = g;
        this.exponent = exponent;
        g.registerReference();
        group.unregisterUncomputedRoot(g);
        group.registerUncomputedRoot(this);
    }

    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        if (value != null) {
            prod.op(value);
        } else {
            PowProductExpression tmpPowProd = emptyPowProductExpression();
            PairingProductExpression tmpPairProd = emptyPairingProductExpression();
            g.putProduct(tmpPowProd, tmpPairProd);

            tmpPowProd.pow(exponent);
            prod.op(tmpPowProd);

            if (pairingProd != null) {
                tmpPairProd.pow(exponent);
                pairingProd.op(tmpPairProd);
            }
        }
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        PowProductExpression tmpPowProd = emptyPowProductExpression();
        PairingProductExpression tmpPairProd = emptyPairingProductExpression();
        g.putProductBasedOnLeafs(tmpPowProd, tmpPairProd);

        tmpPowProd.pow(exponent);
        prod.op(tmpPowProd);

        if (pairingProd != null) {
            tmpPairProd.pow(exponent);
            pairingProd.op(tmpPairProd);
        }
    }
}
