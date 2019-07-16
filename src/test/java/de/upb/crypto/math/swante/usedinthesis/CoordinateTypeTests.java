package de.upb.crypto.math.swante.usedinthesis;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;
import de.upb.crypto.math.swante.util.MyMetric;
import org.junit.Test;

import static de.upb.crypto.math.swante.misc.pln;

public class CoordinateTypeTests {
    
    int numSuperIterations = 10000;
    int numWarmUpIterations = numSuperIterations;
    int numPoints = 1000;
    
    @Test
    public void testCoordinateTypes() {
        pln("=========================");
                MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp192r1CurveParameters();
//        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        Zp zp = new Zp(parameters.p);
        
//        MyAffineCurve curve = new MyAffineCurve(parameters);
//        MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
        MyJacobiCurve curve = new MyJacobiCurve(parameters);
        pln("Running coordinate type performance tests for curve: " + curve.toString());
        AbstractEllipticCurvePoint[] A = misc.createRandomCurvePoints(curve, numPoints);
        AbstractEllipticCurvePoint[] B = misc.createRandomCurvePoints(curve, numPoints);
        AbstractEllipticCurvePoint[] C = misc.createRandomCurvePoints(curve, numPoints);
        for (int i = 0; i < numPoints; i++) {
            C[i] = C[i].normalize();
        }
        for (int iMeta = 1; iMeta <= 1; iMeta++) {
            pln("meta iteration " + iMeta);
            MyMetric metric = new MyMetric("curve metric");
            for (int iter = -numWarmUpIterations; iter < numSuperIterations; iter++) {
                double startMillis = System.nanoTime() / 1.0e6;
                for (int i = 0; i < numPoints; i++) {
                    A[i].add(B[i]);
//                    A[i].square();
//                    A[i].addAssumingZ2IsOne(C[i]);
                }
                double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
                if (iter >= 0) {
                    metric.add(elapsedMillis);
                }
                if (iter <= -numWarmUpIterations+10 || iter >= 0 && iter < 10 || iter % 200 == 0) {
                    pln(String.format("iteration=%d, #values=%d, time=%.3f ms", iter, numPoints, elapsedMillis));
                }
            }
            pln(metric);
        }
    }
    
    
    
}
