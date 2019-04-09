package de.upb.crypto.math.swante.profiling;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.*;

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
        int numIterations = 50000;
        int numPowerIterations = 2000;
        misc.tick();
        AbstractEllipticCurvePoint tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(tmp);
        }
        tmp = tmp.normalize();
        double elapsed = misc.tick();
        pln(String.format("time for %d point doubles (and one final normalization): %.1f ms", numIterations, elapsed));
        misc.tick();
        tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(g);
        }
        tmp = tmp.normalize();
        elapsed = misc.tick();
        pln(String.format("time for %d point additions (and one final normalization): %.1f ms", numIterations, elapsed));
        tmp = g;
        BigInteger exponent = ((Zp.ZpElement) g.getX()).getInteger();
        int windowSize = 3;
        int m = (1 << windowSize)-1;
        MyGlobals.useCurvePointNormalizationPowOptimization = false;
        misc.tick();
        for (int i = 0; i < numPowerIterations; i++) {
            tmp = tmp.prepareForPow(exponent);
            tmp = (AbstractEllipticCurvePoint) MyExponentiationAlgorithms.defaultPowImplementation(tmp, exponent);
            tmp = tmp.normalize();
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow), without normalization optimization: %.1f ms", numPowerIterations, elapsed));
        MyGlobals.useCurvePointNormalizationPowOptimization = !MyGlobals.useCurvePointNormalizationPowOptimization;
        misc.tick();
        for (int i = 0; i < numPowerIterations; i++) {
            tmp = tmp.prepareForPow(exponent);
            tmp = (AbstractEllipticCurvePoint) MyExponentiationAlgorithms.defaultPowImplementation(tmp, exponent);
            tmp = tmp.normalize();
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow), with normalization optimization: %.1f ms", numPowerIterations, elapsed));
    }
}
