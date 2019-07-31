package de.upb.crypto.math.swante.util;

/**
 * Some global flags I used for testing certain functionalities.
 */
public class MyGlobals {
    
    public static boolean useCurvePointNormalizationPowOptimization = false;
    
    public static int curvePointNormalizationOptimizationThreshold = 50; // optimization is only used when bit length of exponent exceeds this value
}
