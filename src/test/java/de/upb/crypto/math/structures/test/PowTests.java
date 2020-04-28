package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class PowTests {

    @Test
    public void testPowZnNotInvertible() {
        // Tests a problem in that WNAF is chosen for exponentiation but one of the power is not actually invertible.
        Zn zn = new Zn(BigInteger.valueOf(100));
        GroupElement two = zn.createZnElement(BigInteger.valueOf(4)).toUnitGroupElement();
        Zn.ZnElement three = zn.createZnElement(BigInteger.valueOf(57));
        two.pow(three.getInteger());
    }
}
