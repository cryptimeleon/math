package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public interface ExponentExpr {
    BigInteger evaluate();
    Zn.ZnElement evaluateZn(Zn zn);
}
