package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;
import de.upb.crypto.math.swante.util.MyMetric;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;

/**
 * Simply a class to make profiling with VisualVM easier.
 */
public class EcPointProfiling {
    public static void main(String[] args) {
        misc.sleep(1.0);
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        MyShortFormWeierstrassCurve curve = new MyJacobiCurve(parameters);
        pln("=========================");
        pln("Running performance tests for curve: " + curve.toString());
        AbstractEllipticCurvePoint g = curve.generator;
        int numSuperIterations = 5;
        MyMetric normalMetric = new MyMetric("Without pre-normalization");
        MyMetric optimizedMetric = new MyMetric("With pre-normalization");
        for (int superIt = 0; superIt < numSuperIterations; superIt++) {
            int numPowerIterations = 2000;
            AbstractEllipticCurvePoint tmp = g;
            BigInteger exponent = ((Zp.ZpElement) g.getX()).getInteger();
            int windowSize = 3;
            int m = (1 << windowSize) - 1;
            MyGlobals.useCurvePointNormalizationPowOptimization = false;
            misc.tick();
            for (int i = 0; i < numPowerIterations; i++) {
                tmp = curve.getUniformlyRandomElement();
                tmp = tmp.prepareForPow(exponent);
                tmp = (AbstractEllipticCurvePoint) MyExponentiationAlgorithms.defaultPowImplementation(tmp, exponent);
                tmp = tmp.normalize();
            }
            double elapsed = misc.tick();
            normalMetric.add(elapsed);
            pln(String.format("time for %d pow G.x computations (and one normalization after each pow), without normalization optimization: %.1f ms", numPowerIterations, elapsed));
            MyGlobals.useCurvePointNormalizationPowOptimization = !MyGlobals.useCurvePointNormalizationPowOptimization;
            misc.tick();
            for (int i = 0; i < numPowerIterations; i++) {
                tmp = curve.getUniformlyRandomElement();
                tmp = tmp.prepareForPow(exponent);
                tmp = (AbstractEllipticCurvePoint) MyExponentiationAlgorithms.defaultPowImplementation(tmp, exponent);
                tmp = tmp.normalize();
            }
            elapsed = misc.tick();
            optimizedMetric.add(elapsed);
            pln(String.format("time for %d pow G.x computations (and one normalization after each pow), with normalization optimization: %.1f ms", numPowerIterations, elapsed));
        }
        pln(normalMetric);
        pln(optimizedMetric);
    }
}
