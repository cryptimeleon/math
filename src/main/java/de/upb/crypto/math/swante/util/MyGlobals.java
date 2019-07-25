package de.upb.crypto.math.swante.util;

public class MyGlobals {
    
    public static boolean useCurvePointNormalizationPowOptimization = false;
    public static boolean skipModOperationIfPossible = true;
    
    public static int curvePointNormalizationOptimizationThreshold = 50; // optimization is only used when bit length of exponent exceeds this value
}
