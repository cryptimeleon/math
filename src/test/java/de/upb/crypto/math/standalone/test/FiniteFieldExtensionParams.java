package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class FiniteFieldExtensionParams {

    public static StandaloneTestParams get() {
        //Finite extension field F4 as F2[X]/(x^2+x+1)
        Zp f2 = new Zp(BigInteger.valueOf(2));
        FiniteFieldExtension f4 = new FiniteFieldExtension(f2, PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(1), f2.valueOf(1)));
        return new StandaloneTestParams(FiniteFieldExtension.class, f4);
    }
}
