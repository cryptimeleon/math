package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.MyProjectiveCurve;
import de.upb.crypto.math.swante.MyProjectiveTriple;
import de.upb.crypto.math.swante.MyShortFormWeierstrassCurve;
import de.upb.crypto.math.swante.MySingleExponentiationAlgorithms;
import de.upb.crypto.math.swante.util.MyGlobals;
import de.upb.crypto.math.swante.util.MyMetric;
import de.upb.crypto.math.swante.util.MyShortFormWeierstrassCurveParameters;
import de.upb.crypto.math.swante.util.MyUtil;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.util.MyUtil.pln;

public class ThesisBasicFieldOperations {
    public static void main(String[] args) {
        pln("=========================");
        if (args.length == 0) {
            args = "1 9 100 1000".split(" ");
        }
        pln(args);
        int operation = Integer.parseInt(args[0]);
        int bitLengthPower = Integer.parseInt(args[1]);
        int numValues = Integer.parseInt(args[2]);
        int numIterations = Integer.parseInt(args[3]);
        int bitLength = 1 << bitLengthPower;
        pln("=========================");
        BigInteger p = MyUtil.createPrimeWithGivenBitLength(bitLength);
        Zp zp = new Zp(p);
        Zp.ZpElement[] A = MyUtil.createRandomZpValues(zp, numValues);
        Zp.ZpElement[] B = MyUtil.createRandomZpValues(zp, numValues);
        
        double startMillis = System.nanoTime() / 1.0e6;
        for (int iter = -numIterations; iter < numIterations; iter++) {
            if (iter == 0) { // start timing only after warmup phase
                startMillis = System.nanoTime() / 1.0e6;
            }
            for (int i = 0; i < numValues; i++) {
                if (operation == 1) { // addition
                    A[i].add(B[i]);
                } else if (operation == 2) { // subtraction
                    A[i].sub(B[i]);
                } else if (operation == 3) { // square
                    A[i].square();
                } else if (operation == 4) { // multiplication
                    A[i].mul(B[i]);
                } else { // inversion
                    A[i].inv();
                }
            }
        }
        double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
        pln("Result: " + elapsedMillis);
    }
}
