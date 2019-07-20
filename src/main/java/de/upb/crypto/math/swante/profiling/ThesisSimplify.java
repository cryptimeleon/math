package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;
import de.upb.crypto.math.swante.powproducts.MyArrayPowProductWithFixedBases;
import de.upb.crypto.math.swante.powproducts.MyFastPowProductWithoutCaching;
import de.upb.crypto.math.swante.powproducts.MySimpleInterleavingPowProduct;
import de.upb.crypto.math.swante.powproducts.MySimultaneousSlidingWindowPowProduct;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;

public class ThesisSimplify {
    public static void main(String[] args) {
        pln("=========================");
        if (args.length == 0) {
            args = "128 10 100 1".split(" ");
        }
        pln(args);
        int bitLength = Integer.parseInt(args[0]);
        MyShortFormWeierstrassCurveParameters parameters = misc.createBnWeierstrassCurveGroupParams(bitLength);
        BigInteger p = parameters.p;
        Zp zp = new Zp(p);
        MyShortFormWeierstrassCurve curve = new MyProjectiveCurve(parameters);
        int numBases = Integer.parseInt(args[1]);
        int numIterations = Integer.parseInt(args[2]);
        int algo = Integer.parseInt(args[3]);
        Zp.ZpElement[] exponentsZp = misc.createRandomZpValues(zp, numBases);
        BigInteger[] exponents = new BigInteger[numBases];
        for (int i = 0; i < numBases; i++) {
            exponents[i] = exponentsZp[i].getInteger();
        }
        AbstractEllipticCurvePoint[] bases = misc.createRandomCurvePoints(curve, numBases);
        MyProjectiveTriple[] basesSimple = new MyProjectiveTriple[numBases];
        for (int i = 0; i < numBases; i++) {
            basesSimple[i] = new MyProjectiveTriple(((Zp.ZpElement) bases[i].getX()).getInteger(), ((Zp.ZpElement) bases[i].getY()).getInteger(), ((Zp.ZpElement) bases[i].getZ()).getInteger());
        }
        double startMillis = System.nanoTime() / 1.0e6;
        for (int iter = -numIterations; iter < numIterations; iter++) {
            if (iter == 0) { // start timing only after warmup phase
                startMillis = System.nanoTime() / 1.0e6;
            }
            
        }
        double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
        pln("Result: " + elapsedMillis);
    }
}
