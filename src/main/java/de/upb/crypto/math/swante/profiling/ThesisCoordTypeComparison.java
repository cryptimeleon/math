package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;

import static de.upb.crypto.math.swante.misc.pln;

public class ThesisCoordTypeComparison {
    public static void main(String[] args) {
        pln("=========================");
        if (args.length == 0) {
            args = "256 projective 10 100 1".split(" ");
        }
        pln(args);
        int bitLength = Integer.parseInt(args[0]);
        MyShortFormWeierstrassCurveParameters parameters = misc.createBnWeierstrassCurveGroupParams(bitLength);
        Zp zp = new Zp(parameters.p);
        MyShortFormWeierstrassCurve curve = new MyAffineCurve(parameters);
        if (args[1].equals("jacobi")) {
            curve = new MyJacobiCurve(parameters);
        } else if (args[1].equals("projective")) {
            curve = new MyProjectiveCurve(parameters);
        }
        int numPoints = Integer.parseInt(args[2]);
        int numIterations = Integer.parseInt(args[3]);
        int algo = Integer.parseInt(args[4]);
        AbstractEllipticCurvePoint[] A = misc.createRandomCurvePoints(curve, numPoints);
        AbstractEllipticCurvePoint[] B = misc.createRandomCurvePoints(curve, numPoints);
        AbstractEllipticCurvePoint[] C = misc.createRandomCurvePoints(curve, numPoints);
        for (int i = 0; i < numPoints; i++) {
            C[i] = C[i].normalize();
        }
        
        double startMillis = System.nanoTime() / 1.0e6;
        for (int iter = -numIterations; iter < numIterations; iter++) {
            if (iter < 0) { // start timing only after warmup phase
                startMillis = System.nanoTime() / 1.0e6;
            }
            for (int i = 0; i < numPoints; i++) {
                if (algo == 1) { // normal add
                    A[i].add(B[i]);
                } else if (algo == 2) { // square
                    A[i].square();
                } else { // mixed
                    A[i].addAssumingZ2IsOne(C[i]);
                }
            }
        }
        double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
        pln("Result: " + elapsedMillis);
    }
}
