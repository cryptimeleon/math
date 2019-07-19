package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;
import de.upb.crypto.math.swante.powproducts.MyArrayPowProductWithFixedBases;
import de.upb.crypto.math.swante.powproducts.MyFastPowProductWithoutCaching;
import de.upb.crypto.math.swante.powproducts.MySimpleInterleavingPowProduct;
import de.upb.crypto.math.swante.powproducts.MySimultaneousSlidingWindowPowProduct;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.myAssert;
import static de.upb.crypto.math.swante.misc.pln;

public class ThesisMultiExpo {
    public static void main(String[] args) {
        pln("=========================");
        if (args.length == 0) {
            args = "128 projective 10 100 1 1 True".split(" ");
        }
        pln(args);
        int bitLength = Integer.parseInt(args[0]);
        MyShortFormWeierstrassCurveParameters parameters = misc.createBnWeierstrassCurveGroupParams(bitLength);
        
        Zp zp = new Zp(parameters.p);
        MyShortFormWeierstrassCurve curve = new MyProjectiveCurve(parameters);
        if (args[1].equals("jacobi")) {
            curve = new MyJacobiCurve(parameters);
        }
//        AbstractEllipticCurvePoint g = curve.getGenerator();
//        AbstractEllipticCurvePoint zero = curve.getNeutralElement();
//        myAssert(g.pow(curve.size()).equals(zero));
        int numBases = Integer.parseInt(args[2]);
        int numIterations = Integer.parseInt(args[3]);
        int windowSize = Integer.parseInt(args[4]);
        int m = (1 << windowSize) - 1;
        int algo = Integer.parseInt(args[5]);
        boolean cacheSmallPowers = false;
        if (args[6].equals("True")) {
            cacheSmallPowers = true;
        }
        Zp.ZpElement[] exponentsZp = misc.createRandomZpValues(zp, numBases);
        BigInteger[] exponents = new BigInteger[numBases];
        for (int i = 0; i < numBases; i++) {
            exponents[i] = exponentsZp[i].getInteger();
        }
        AbstractEllipticCurvePoint[] bases = misc.createRandomCurvePoints(curve, numBases);
        MyArrayPowProductWithFixedBases myPpe = null;
        if (cacheSmallPowers) { // cache small powers for algorithms where caching makes sense
            if (algo == 3) { // simultaneous sliding window
                myPpe = new MySimultaneousSlidingWindowPowProduct(bases, windowSize);
            } else if (algo == 4) { // interleaved sliding window
                myPpe = new MySimpleInterleavingPowProduct(bases, windowSize);
            }
        }
        double startMillis = System.nanoTime() / 1.0e6;
        double originalStartMillis = startMillis;
        for (int iter = -numIterations; iter < numIterations; iter++) {
            if (iter == 0) { // start timing only after warmup phase
                startMillis = System.nanoTime() / 1.0e6;
            }
            if (algo == 0) { // old version with HashMap
                PowProductExpression originalPpe = new PowProductExpression(curve);
                for (int b = 0; b < numBases; b++) {
                    GroupElement base = bases[b];
                    BigInteger e = exponents[b];
                    originalPpe.op(base, e);
                }
                originalPpe.evaluate();
            } else if (algo == 1) { // slow array version without sharing squarings
                myPpe = new MyArrayPowProductWithFixedBases(bases);
            } else if (algo == 2) { // fast array version with sharing squarings
                myPpe = new MyFastPowProductWithoutCaching(bases);
            } else if (algo == 3) { // simultaneous sliding window method
                if (!cacheSmallPowers) {
                    myPpe = new MySimultaneousSlidingWindowPowProduct(bases, windowSize);
                }
            } else if (algo == 4) { // interleaved sliding window method
                if (!cacheSmallPowers) {
                    myPpe = new MySimpleInterleavingPowProduct(bases, windowSize);
                }
            }
            if (algo != 0) {
                myPpe.evaluate(exponents);
            }
            if (System.nanoTime() / 1.0e6 - originalStartMillis > 15*1000) { // exit early if this run will take too long
                pln("Result: too much");
                return;
            }
        }
        double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
        pln("Result: " + elapsedMillis);
    }
}
