package de.upb.crypto.math.test;

import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class SetAccessibleTest {

    public static void main(String[] args) {
        Zp zp = new Zp(BigInteger.valueOf(101));
        SetAccessibleTestClass testClass = new SetAccessibleTestClass(zp.asUnitGroup(), zp.asUnitGroup().getUniformlyRandomElement());
        Representation repr = testClass.getRepresentation();
        System.out.println("Got representation. Reconstructing.");
        SetAccessibleTestClass testClass2 = new SetAccessibleTestClass(repr);
    }

}
